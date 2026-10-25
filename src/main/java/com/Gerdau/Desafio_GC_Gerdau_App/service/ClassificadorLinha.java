package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.LinhaPlanilha;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.NovoServicoForm;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ResultadoClassificacao;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ServicoSimilar;
import com.Gerdau.Desafio_GC_Gerdau_App.model.CatHorario;
import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatHorarioRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatNbsRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ServicoRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/** Decide a situação de cada linha da importação, sem gravar nada e sem lançar exceção por dado ruim. */
@Service
public class ClassificadorLinha {

    private static final int MAX_SIMILARES = 5;
    private static final Pattern ABREVIATURA = Pattern.compile("[A-Za-z0-9]*");
    private static final Pattern COMPLEMENTO = Pattern.compile("(_[A-Za-z0-9]*)?");
    private static final Pattern CNAE = Pattern.compile("\\d{7}");
    private static final Pattern CNAE_EXCEL = Pattern.compile("(\\d{7})\\.0+");

    private final ServicoRepository servicoRepository;
    private final CatHorarioRepository catHorarioRepository;
    private final CatNbsRepository catNbsRepository;
    private final SinonimoServicoService sinonimoServicoService;

    public ClassificadorLinha(ServicoRepository servicoRepository, CatHorarioRepository catHorarioRepository,
                              CatNbsRepository catNbsRepository, SinonimoServicoService sinonimoServicoService) {
        this.servicoRepository = servicoRepository;
        this.catHorarioRepository = catHorarioRepository;
        this.catNbsRepository = catNbsRepository;
        this.sinonimoServicoService = sinonimoServicoService;
    }

    /** Não altera {@code codigosJaVistos}: quem chama registra o código gerado depois. */
    public ResultadoClassificacao classificar(LinhaPlanilha linha, Set<String> codigosJaVistos) {
        try {
            return classificarLinha(linha, codigosJaVistos);
        } catch (RuntimeException e) {
            return erro(null, "Não foi possível analisar a linha: " + e.getMessage());
        }
    }

    private ResultadoClassificacao classificarLinha(LinhaPlanilha linha, Set<String> codigosJaVistos) {
        // 1. campos
        String erroCampo = validarTexto(linha);
        if (erroCampo != null) {
            return erro(null, erroCampo);
        }
        String cnae = normalizarCnae(linha.cnae());
        if (cnae == null) {
            return erro(null, "CNAE deve ter 7 dígitos: " + linha.cnae());
        }
        Optional<CatHorario> condicao = buscarCondicao(linha.condicao());
        if (condicao.isEmpty()) {
            return erro(null, "Condição desconhecida ou inativa: " + linha.condicao());
        }
        String nbs = linha.codigoNbs();
        if (nbs != null && !catNbsRepository.existsById(nbs)) {
            return erro(null, "Código NBS desconhecido: " + nbs);
        }

        NovoServicoForm form = new NovoServicoForm();
        form.setAbreviatura(linha.abreviatura());
        form.setComplemento(linha.complemento());
        form.setCnae(cnae);
        String codigo = ServicoService.montarCodigo(form, condicao.get());
        String descricaoCondicao = condicao.get().getDescricaoHorario();

        // 2. tamanho do código
        if (codigo.length() > ServicoService.TAMANHO_MAX_CODIGO) {
            return erro(codigo, "Código gerado excede " + ServicoService.TAMANHO_MAX_CODIGO
                    + " caracteres: " + codigo);
        }
        // 3. repetida no arquivo
        if (codigosJaVistos != null && codigosJaVistos.contains(codigo)) {
            return new ResultadoClassificacao(SituacaoLinha.REPETIDA, codigo, null, null,
                    "Código já aparece em outra linha da planilha");
        }
        // 4. sinônimo aprovado
        Optional<Servico> sinonimo = sinonimoServicoService.reconhecer(linha.textoBreve(), descricaoCondicao, cnae);
        if (sinonimo.isPresent()) {
            Servico s = sinonimo.get();
            return new ResultadoClassificacao(SituacaoLinha.EXISTENTE, codigo, s.getId(), 1.0,
                    "Sinônimo aprovado do serviço " + s.getCodigoPadronizado());
        }
        // 5. código idêntico
        Optional<Servico> igual = servicoRepository.findByCodigoPadronizado(codigo);
        if (igual.isPresent()) {
            return new ResultadoClassificacao(SituacaoLinha.EXISTENTE, codigo, igual.get().getId(), 1.0,
                    "Código já cadastrado");
        }
        // 6. similaridade
        List<ServicoSimilar> similares = servicoRepository.buscarSimilares(
                linha.textoBreve(), ServicoService.LIMIAR_SUGESTAO, MAX_SIMILARES);
        Optional<ServicoSimilar> maisParecido = similares == null ? Optional.empty()
                : similares.stream().filter(s -> s.getScore() != null)
                        .max(Comparator.comparingDouble(ServicoSimilar::getScore));
        if (maisParecido.isPresent()) {
            ServicoSimilar s = maisParecido.get();
            return new ResultadoClassificacao(SituacaoLinha.PARECIDA, codigo, s.getId(), s.getScore(),
                    "Parecido com " + s.getCodigoPadronizado() + " (" + s.getTextoBreve() + ")");
        }
        // 7. nova
        return new ResultadoClassificacao(SituacaoLinha.NOVA, codigo, null, null, null);
    }

    private static String validarTexto(LinhaPlanilha l) {
        if (vazio(l.areaServico())) return "Campo obrigatório: area_servico";
        if (l.areaServico().length() > 50) return "area_servico excede 50 caracteres";
        if (vazio(l.textoBreve())) return "Campo obrigatório: texto_breve";
        if (l.textoBreve().length() > 255) return "texto_breve excede 255 caracteres";
        if (vazio(l.abreviatura())) return "Campo obrigatório: abreviatura";
        if (l.abreviatura().length() > 10) return "abreviatura excede 10 caracteres";
        if (!ABREVIATURA.matcher(l.abreviatura()).matches()) return "abreviatura: use apenas letras e números";
        if (l.complemento() != null) {
            if (l.complemento().length() > 15) return "complemento excede 15 caracteres";
            if (!COMPLEMENTO.matcher(l.complemento()).matches()) return "complemento deve começar com _ (ex.: _CONCRE)";
        }
        if (vazio(l.condicao())) return "Campo obrigatório: condicao";
        if (vazio(l.cnae())) return "Campo obrigatório: cnae";
        if (l.codigoNbs() != null && l.codigoNbs().length() > 20) return "codigo_nbs excede 20 caracteres";
        if (l.criadorCodigo() != null && l.criadorCodigo().length() > 100) return "criador_codigo excede 100 caracteres";
        return null;
    }

    /** Aceita 7 dígitos, ou o que o Excel entrega de célula numérica ("4399103.0"). */
    private static String normalizarCnae(String cnae) {
        String c = cnae.trim();
        if (CNAE.matcher(c).matches()) {
            return c;
        }
        var m = CNAE_EXCEL.matcher(c);
        return m.matches() ? m.group(1) : null;
    }

    /** A condição vale pela descrição (sem diferenciar caixa e acento) ou pelo sufixo; só as ativas. */
    private Optional<CatHorario> buscarCondicao(String informada) {
        String sufixo = informada.trim().toUpperCase(Locale.ROOT);
        String descricao = DeParaService.normalizar(informada);
        List<CatHorario> ativas = catHorarioRepository.findByAtivoTrueOrderByIdAsc();
        if (ativas == null) {
            return Optional.empty();
        }
        return ativas.stream()
                .filter(h -> DeParaService.normalizar(h.getDescricaoHorario()).equals(descricao)
                        || h.getSufixoCodigo().equalsIgnoreCase(sufixo))
                .findFirst();
    }

    private static boolean vazio(String s) {
        return s == null || s.isBlank();
    }

    private static ResultadoClassificacao erro(String codigo, String motivo) {
        return new ResultadoClassificacao(SituacaoLinha.ERRO, codigo, null, null, motivo);
    }
}
