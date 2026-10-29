package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.AnaliseSimilaridade;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.AnaliseSimilaridade.SimilarDto;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.NovoServicoForm;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ServicoSimilar;
import com.Gerdau.Desafio_GC_Gerdau_App.exception.CadastroRejeitadoException;
import com.Gerdau.Desafio_GC_Gerdau_App.model.CatHorario;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DecisaoConsulta;
import com.Gerdau.Desafio_GC_Gerdau_App.model.ResultadoConsulta;
import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatHorarioRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatNbsRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ServicoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
public class ServicoService {

    private static final Logger log = LoggerFactory.getLogger(ServicoService.class);

    /** A partir daqui o serviço é sugerido como "parecido". */
    static final double LIMIAR_SUGESTAO = 0.4;
    /** A partir daqui, com mesma condição e CNAE, é tratado como duplicado e bloqueado. */
    static final double LIMIAR_BLOQUEIO = 0.8;
    static final int TAMANHO_MAX_CODIGO = 40;
    private static final int MAX_SUGESTOES = 5;
    private static final int TAMANHO_MIN_BUSCA = 3;
    private static final int TAMANHO_MIN_JUSTIFICATIVA = 10;

    private final ServicoRepository repository;
    private final CatNbsRepository catNbsRepository;
    private final CatHorarioRepository catHorarioRepository;
    private final SinonimoServicoService sinonimoServicoService;
    private final RegistroConsultaService registro;

    public ServicoService(ServicoRepository repository, CatNbsRepository catNbsRepository,
                          CatHorarioRepository catHorarioRepository,
                          SinonimoServicoService sinonimoServicoService, RegistroConsultaService registro) {
        this.sinonimoServicoService = sinonimoServicoService;
        this.registro = registro;
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
        // o bloqueio tem consulta própria: não pode depender de o duplicado estar entre as poucas sugestões
        Optional<ServicoSimilar> duplicado = buscarDuplicado(repository, form.getTextoBreve(),
                condicao.getDescricaoHorario(), form.getCnae());

        // o duplicado vai primeiro nas sugestões, para o comprador ver (e usar) o código que o bloqueou
        List<SimilarDto> similares = new ArrayList<>();
        duplicado.ifPresent(d -> similares.add(paraDto(d)));
        encontrados.stream()
                .filter(s -> duplicado.isEmpty() || !Objects.equals(duplicado.get().getId(), s.getId()))
                .forEach(s -> similares.add(paraDto(s)));

        return new AnaliseSimilaridade(codigo, codigoExistente, similares,
                codigoExistente || duplicado.isPresent(), null);
    }

    private static SimilarDto paraDto(ServicoSimilar s) {
        return new SimilarDto(s.getId(), s.getTextoBreve(), s.getCodigoPadronizado(), s.getScore());
    }

    /** Os serviços parecidos com o texto, como o cadastro individual consulta (também usado na importação). */
    static List<ServicoSimilar> buscarParecidos(ServicoRepository repository, String texto) {
        return repository.buscarSimilares(texto.trim(), LIMIAR_SUGESTAO, MAX_SUGESTOES);
    }

    /**
     * Regra de bloqueio do cadastro individual: o serviço de maior score que conta como duplicado (score a partir de
     * {@link #LIMIAR_BLOQUEIO}, mesma condição pela descrição e mesmo CNAE). Também usada na aprovação da importação.
     */
    static Optional<ServicoSimilar> buscarDuplicado(ServicoRepository repository, String texto,
                                                    String descricaoCondicao, String cnae) {
        return repository.buscarDuplicado(texto.trim(), LIMIAR_BLOQUEIO, descricaoCondicao, cnae);
    }

    /** Cada tentativa que chega à análise fica registrada (ver {@link RegistroConsultaService}); erro de validação não. */
    public Servico cadastrar(NovoServicoForm form, boolean confirmarMesmoAssim, String usuario) {
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
        String contestacao = null;
        if (analise.bloqueado()) {
            // só o bloqueio por texto parecido pode ser contestado, e a justificativa é obrigatória
            contestacao = analise.contestavel() ? justificativaDaContestacao(form) : null;
            if (contestacao == null) {
                registrar(usuario, form, condicao, analise, ResultadoConsulta.BLOQUEADO, null, null, null);
                throw new CadastroRejeitadoException(analise);
            }
        } else if (analise.requerConfirmacao() && !confirmarMesmoAssim) {
            registrar(usuario, form, condicao, analise, ResultadoConsulta.REQUER_CONFIRMACAO, null, null, null);
            throw new CadastroRejeitadoException(analise);
        }

        Servico servico = novoServico(form, condicao, analise.codigoGerado());
        Servico salvo;
        try {
            salvo = repository.saveAndFlush(servico);
        } catch (DataIntegrityViolationException e) {
            // corrida: outro usuário criou o mesmo código entre a análise e o insert
            AnaliseSimilaridade bloqueio = new AnaliseSimilaridade(
                    analise.codigoGerado(), true, analise.similares(), true, analise.reconhecidoComo());
            registrar(usuario, form, condicao, bloqueio, ResultadoConsulta.BLOQUEADO, null, null, null);
            throw new CadastroRejeitadoException(bloqueio);
        }
        DecisaoConsulta decisao = contestacao != null ? DecisaoConsulta.CONTESTOU_BLOQUEIO
                : analise.requerConfirmacao() ? DecisaoConsulta.CONFIRMOU_MESMO_ASSIM
                : DecisaoConsulta.CADASTROU_NOVO;
        registrar(usuario, form, condicao, analise, ResultadoConsulta.CADASTRADO, decisao, salvo.getId(), contestacao);
        return salvo;
    }

    /** A justificativa sem espaços nas pontas; {@code null} se não veio; erro se veio curta demais para explicar algo. */
    private static String justificativaDaContestacao(NovoServicoForm form) {
        String texto = form.getJustificativaOutroServico() == null ? "" : form.getJustificativaOutroServico().trim();
        if (texto.isEmpty()) {
            return null;
        }
        if (texto.length() < TAMANHO_MIN_JUSTIFICATIVA) {
            throw new IllegalArgumentException("Explique em pelo menos " + TAMANHO_MIN_JUSTIFICATIVA
                    + " caracteres por que é outro serviço");
        }
        return texto;
    }

    /** O comprador escolheu reaproveitar um serviço sugerido em vez de cadastrar outro. */
    public Servico registrarReutilizacao(String usuario, String textoBreve, Integer servicoId) {
        Servico escolhido = repository.findById(servicoId).orElseThrow();
        String texto = textoBreve.trim();
        registrarSemFalhar(usuario, texto, null, null, null, ResultadoConsulta.SUGERIDO,
                DecisaoConsulta.REUTILIZOU_EXISTENTE, escolhido.getId(), similares(texto), null);
        return escolhido;
    }

    private void registrar(String usuario, NovoServicoForm form, CatHorario condicao, AnaliseSimilaridade analise,
                           ResultadoConsulta resultado, DecisaoConsulta decisao, Integer servicoId,
                           String justificativa) {
        registrarSemFalhar(usuario, form.getTextoBreve().trim(), condicao.getDescricaoHorario(), form.getCnae(),
                analise.codigoGerado(), resultado, decisao, servicoId, analise.similares(), justificativa);
    }

    /** O histórico é secundário: se não gravar, o cadastro segue e a falha vai para o log. */
    private void registrarSemFalhar(String usuario, String texto, String condicao, String cnae, String codigo,
                                    ResultadoConsulta resultado, DecisaoConsulta decisao, Integer servicoId,
                                    List<SimilarDto> sugestoes, String justificativa) {
        try {
            registro.registrar(usuario, texto, condicao, cnae, codigo, resultado, decisao, servicoId, sugestoes,
                    justificativa);
        } catch (RuntimeException e) {
            log.warn("Falha ao registrar a consulta de cadastro ({}): {}", resultado, e.toString());
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
