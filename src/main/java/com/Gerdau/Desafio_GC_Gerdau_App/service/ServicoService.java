package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.AnaliseSimilaridade;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.AnaliseSimilaridade.SimilarDto;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.NovoServicoForm;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ServicoSimilar;
import com.Gerdau.Desafio_GC_Gerdau_App.exception.CadastroRejeitadoException;
import com.Gerdau.Desafio_GC_Gerdau_App.model.CatHorario;
import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatHorarioRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatNbsRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ServicoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
public class ServicoService {

    /** A partir daqui o serviço é sugerido como "parecido". */
    static final double LIMIAR_SUGESTAO = 0.4;
    /** A partir daqui, com mesma condição e CNAE, é tratado como duplicado e bloqueado. */
    static final double LIMIAR_BLOQUEIO = 0.8;
    static final int TAMANHO_MAX_CODIGO = 40;
    private static final int MAX_SUGESTOES = 5;
    private static final int TAMANHO_MIN_BUSCA = 3;

    private final ServicoRepository repository;
    private final CatNbsRepository catNbsRepository;
    private final CatHorarioRepository catHorarioRepository;
    private final SinonimoServicoService sinonimoServicoService;

    public ServicoService(ServicoRepository repository, CatNbsRepository catNbsRepository,
                          CatHorarioRepository catHorarioRepository,
                          SinonimoServicoService sinonimoServicoService) {
        this.sinonimoServicoService = sinonimoServicoService;
        this.repository = repository;
        this.catNbsRepository = catNbsRepository;
        this.catHorarioRepository = catHorarioRepository;
    }

    public String gerarCodigo(NovoServicoForm form) {
        return montarCodigo(form, condicaoAtiva(form.getCondicao()));
    }

    /** Regra única de montagem do código, também usada pela importação em massa. */
    static String montarCodigo(NovoServicoForm form, CatHorario condicao) {
        String complemento = form.getComplemento() == null ? "" : form.getComplemento();
        String codigo = form.getAbreviatura() + complemento + "_" + condicao.getSufixoCodigo() + form.getCnae();
        return codigo.toUpperCase(Locale.ROOT);
    }

    private CatHorario condicaoAtiva(String sufixo) {
        if (sufixo == null || sufixo.isBlank()) {
            throw new IllegalArgumentException("Informe a condição");
        }
        String informado = sufixo.trim().toUpperCase(Locale.ROOT);
        return catHorarioRepository.findBySufixoCodigoAndAtivoTrue(informado)
                .orElseThrow(() -> new IllegalArgumentException("Condição desconhecida: " + informado));
    }

    /** Serviços já cadastrados parecidos com o texto, para sugerir enquanto o comprador digita. */
    public List<SimilarDto> similares(String texto) {
        if (texto == null || texto.trim().length() < TAMANHO_MIN_BUSCA) {
            return List.of();
        }
        return repository.buscarSimilares(texto.trim(), LIMIAR_SUGESTAO, MAX_SUGESTOES).stream()
                .map(s -> new SimilarDto(s.getId(), s.getTextoBreve(), s.getCodigoPadronizado(), s.getScore()))
                .toList();
    }

    public AnaliseSimilaridade analisar(NovoServicoForm form) {
        CatHorario condicao = condicaoAtiva(form.getCondicao());
        String codigo = montarCodigo(form, condicao);
        boolean codigoExistente = repository.findByCodigoPadronizado(codigo).isPresent();

        // Sinônimo aprovado: o texto já é um serviço conhecido, bloqueia antes da similaridade
        Optional<Servico> sinonimo = sinonimoServicoService.reconhecer(
                form.getTextoBreve(), condicao.getDescricaoHorario(), form.getCnae());
        if (sinonimo.isPresent()) {
            Servico s = sinonimo.get();
            return new AnaliseSimilaridade(codigo, codigoExistente,
                    List.of(new SimilarDto(s.getId(), s.getTextoBreve(), s.getCodigoPadronizado(), 1.0)),
                    true, s.getCodigoPadronizado());
        }

        List<ServicoSimilar> encontrados = buscarParecidos(repository, form.getTextoBreve());

        boolean duplicadoPorTexto = duplicadoPorTexto(encontrados, condicao.getDescricaoHorario(), form.getCnae())
                .isPresent();

        List<SimilarDto> similares = encontrados.stream()
                .map(s -> new SimilarDto(s.getId(), s.getTextoBreve(), s.getCodigoPadronizado(), s.getScore()))
                .toList();

        return new AnaliseSimilaridade(codigo, codigoExistente, similares, codigoExistente || duplicadoPorTexto, null);
    }

    /** Os serviços parecidos com o texto, como o cadastro individual consulta (também usado na importação). */
    static List<ServicoSimilar> buscarParecidos(ServicoRepository repository, String texto) {
        return repository.buscarSimilares(texto.trim(), LIMIAR_SUGESTAO, MAX_SUGESTOES);
    }

    /**
     * Regra de bloqueio do cadastro individual: entre os parecidos, o de maior score que conta como duplicado
     * (score a partir de {@link #LIMIAR_BLOQUEIO}, mesma condição pela descrição e mesmo CNAE).
     */
    static Optional<ServicoSimilar> duplicadoPorTexto(List<ServicoSimilar> encontrados, String descricaoCondicao,
                                                      String cnae) {
        return encontrados.stream()
                .filter(s -> s.getScore() != null && s.getScore() >= LIMIAR_BLOQUEIO
                        && Objects.equals(s.getId1Condicao(), descricaoCondicao)
                        && Objects.equals(s.getId2Cnae(), cnae))
                .max(Comparator.comparingDouble(ServicoSimilar::getScore));
    }

    public Servico cadastrar(NovoServicoForm form, boolean confirmarMesmoAssim) {
        String codigoNbs = codigoNbs(form);
        if (codigoNbs != null && !catNbsRepository.existsById(codigoNbs)) {
            throw new IllegalArgumentException("Código NBS desconhecido: " + codigoNbs);
        }
        CatHorario condicao = condicaoAtiva(form.getCondicao());
        AnaliseSimilaridade analise = analisar(form);
        if (analise.codigoGerado().length() > TAMANHO_MAX_CODIGO) {
            throw new IllegalArgumentException(
                    "Código gerado excede " + TAMANHO_MAX_CODIGO + " caracteres: " + analise.codigoGerado());
        }
        if (analise.bloqueado() || (analise.requerConfirmacao() && !confirmarMesmoAssim)) {
            throw new CadastroRejeitadoException(analise);
        }

        Servico servico = novoServico(form, condicao, analise.codigoGerado());
        try {
            return repository.saveAndFlush(servico);
        } catch (DataIntegrityViolationException e) {
            // corrida: outro usuário criou o mesmo código entre a análise e o insert
            throw new CadastroRejeitadoException(new AnaliseSimilaridade(
                    analise.codigoGerado(), true, analise.similares(), true, analise.reconhecidoComo()));
        }
    }
    /**
     * Monta (sem gravar) o serviço como o cadastro grava: textos sem espaços nas pontas, abreviatura e complemento
     * em maiúsculas, condição pela descrição do catálogo. Também usado na aprovação da importação em massa.
     */
    static Servico novoServico(NovoServicoForm form, CatHorario condicao, String codigo) {
        Servico servico = new Servico();
        servico.setAreaServico(form.getAreaServico().trim());
        servico.setTextoBreve(form.getTextoBreve().trim());
        servico.setAbreviatura(form.getAbreviatura().toUpperCase(Locale.ROOT));
        servico.setComplemento(form.getComplemento() == null ? "" : form.getComplemento().toUpperCase(Locale.ROOT));
        servico.setId1Condicao(condicao.getDescricaoHorario());
        servico.setId2Cnae(form.getCnae());
        servico.setCodigoPadronizado(codigo);
        servico.setCodigoNbs(codigoNbs(form));
        servico.setCriadorCodigo(form.getCriadorCodigo() == null || form.getCriadorCodigo().isBlank()
                ? null : form.getCriadorCodigo().trim());
        return servico;
    }

    private static String codigoNbs(NovoServicoForm form) {
        return form.getCodigoNbs() == null || form.getCodigoNbs().isBlank() ? null : form.getCodigoNbs().trim();
    }
}
