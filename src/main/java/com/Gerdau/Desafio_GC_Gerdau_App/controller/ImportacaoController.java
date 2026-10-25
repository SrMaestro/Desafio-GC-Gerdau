package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.FiltroAprovacao;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.LinhaPlanilha;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ResumoAprovacao;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ResumoLote;
import com.Gerdau.Desafio_GC_Gerdau_App.exception.ArquivoInvalidoException;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DecisaoParecida;
import com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LinhaImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LoteImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.StatusLote;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.LinhaImportacaoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.LoteImportacaoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.service.AprovacaoImportacaoService;
import com.Gerdau.Desafio_GC_Gerdau_App.service.ConferenciaService;
import com.Gerdau.Desafio_GC_Gerdau_App.service.ImportacaoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * API da importação em massa. O usuário que age vem sempre da {@link Authentication}, nunca do corpo ou dos
 * parâmetros. Rotas sob {@code /aprovacao} são só do APROVADOR (ver {@code SecurityConfig}).
 */
@RestController
@RequestMapping("/api/importacoes")
public class ImportacaoController {

    static final int TAMANHO_PADRAO = 100;
    static final int TAMANHO_MAXIMO = 500;

    private final ImportacaoService importacaoService;
    private final ConferenciaService conferenciaService;
    private final AprovacaoImportacaoService aprovacaoService;
    private final LoteImportacaoRepository loteRepository;
    private final LinhaImportacaoRepository linhaRepository;

    public ImportacaoController(ImportacaoService importacaoService, ConferenciaService conferenciaService,
                                AprovacaoImportacaoService aprovacaoService, LoteImportacaoRepository loteRepository,
                                LinhaImportacaoRepository linhaRepository) {
        this.importacaoService = importacaoService;
        this.conferenciaService = conferenciaService;
        this.aprovacaoService = aprovacaoService;
        this.loteRepository = loteRepository;
        this.linhaRepository = linhaRepository;
    }

    // Respostas e corpos

    public record LoteCriado(Long id) {
    }

    public record LinhaResponse(Long id, int numero, String areaServico, String textoBreve, String abreviatura,
                                String complemento, String condicao, String cnae, String codigoNbs,
                                String criadorCodigo, String codigoGerado, SituacaoLinha situacao, EtapaLinha etapa,
                                Integer servicoParecidoId, Double score, String motivo, DecisaoParecida decisao,
                                String conferidoPor, String decididoPor) {
        static LinhaResponse de(LinhaImportacao l) {
            return new LinhaResponse(l.getId(), l.getNumero(), l.getAreaServico(), l.getTextoBreve(),
                    l.getAbreviatura(), l.getComplemento(), l.getCondicao(), l.getCnae(), l.getCodigoNbs(),
                    l.getCriadorCodigo(), l.getCodigoGerado(), l.getSituacao(), l.getEtapa(),
                    l.getServicoParecidoId(), l.getScore(), l.getMotivo(), l.getDecisao(), l.getConferidoPor(),
                    l.getDecididoPor());
        }
    }

    public record PaginaLinhas(List<LinhaResponse> linhas, long total, int pagina, int tamanho, int totalPaginas) {
    }

    public record LoteComEnviadas(Long id, String arquivo, String enviadoPor, LocalDateTime criadoEm, long enviadas,
                               long conflitos) {
    }

    public record LoteResumido(Long id, String arquivo, StatusLote status, int total, int processadas,
                               LocalDateTime criadoEm) {
    }

    public record CorrecaoRequest(String areaServico, String textoBreve, String abreviatura, String complemento,
                                  String condicao, String cnae, String codigoNbs, String criadorCodigo) {
    }

    public record DecisaoRequest(DecisaoParecida decisao) {
    }

    public record DecisaoEmBlocoRequest(double scoreMinimo, DecisaoParecida decisao) {
    }

    public record RejeicaoRequest(String motivo) {
    }

    public record RejeicaoEmBlocoRequest(Set<SituacaoLinha> situacoes, Double scoreMinimo, String motivo) {
    }

    // Importação e consulta

    @PostMapping
    public ResponseEntity<LoteCriado> enviar(@RequestParam("arquivo") MultipartFile arquivo,
                                             Authentication auth) throws IOException {
        if (arquivo.isEmpty()) {
            throw new ArquivoInvalidoException("O arquivo está vazio");
        }
        Long id = importacaoService.iniciar(arquivo.getOriginalFilename(), arquivo.getBytes(), auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(new LoteCriado(id));
    }

    /** Os lotes do próprio usuário (mais recentes primeiro, até 50); nunca os de outros. */
    @GetMapping
    public List<LoteResumido> meusLotes(Authentication auth) {
        return loteRepository.findTop50ByEnviadoPorOrderByCriadoEmDescIdDesc(auth.getName()).stream()
                .map(l -> new LoteResumido(l.getId(), l.getArquivoNome(), l.getStatus(), l.getTotalLinhas(),
                        l.getProcessadas(), l.getCriadoEm()))
                .toList();
    }

    @GetMapping("/{id}")
    public ResumoLote lote(@PathVariable Long id, Authentication auth) {
        LoteImportacao lote = loteVisivel(id, auth);
        return importacaoService.resumo(lote.getId());
    }

    @GetMapping("/{id}/linhas")
    public PaginaLinhas linhas(@PathVariable Long id,
                               @RequestParam(required = false) SituacaoLinha situacao,
                               @RequestParam(required = false) EtapaLinha etapa,
                               @RequestParam(required = false) Double scoreMinimo,
                               @RequestParam(defaultValue = "0") int pagina,
                               @RequestParam(defaultValue = "" + TAMANHO_PADRAO) int tamanho,
                               Authentication auth) {
        LoteImportacao lote = loteVisivel(id, auth);
        // Quem só enxerga o lote por ter linhas a decidir (aprovador de outro dono) vê apenas enviadas e em conflito
        EtapaLinha etapaEfetiva = ehDono(lote, auth) ? etapa : etapaVisivelAoAprovador(etapa);
        int tam = Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO);
        PageRequest pageable = PageRequest.of(Math.max(pagina, 0), tam, Sort.by("numero"));
        Page<LinhaImportacao> p;
        if (scoreMinimo != null && situacao != null && etapaEfetiva != null) {
            p = linhaRepository.findByLoteIdAndSituacaoAndEtapaAndScoreGreaterThanEqual(id, situacao, etapaEfetiva,
                    scoreMinimo, pageable);
        } else if (scoreMinimo != null && situacao != null) {
            p = linhaRepository.findByLoteIdAndSituacaoAndScoreGreaterThanEqual(id, situacao, scoreMinimo, pageable);
        } else if (situacao != null && etapaEfetiva != null) {
            p = linhaRepository.findByLoteIdAndSituacaoAndEtapa(id, situacao, etapaEfetiva, pageable);
        } else if (situacao != null) {
            p = linhaRepository.findByLoteIdAndSituacao(id, situacao, pageable);
        } else if (etapaEfetiva != null) {
            p = linhaRepository.findByLoteIdAndEtapa(id, etapaEfetiva, pageable);
        } else {
            p = linhaRepository.findByLoteId(id, pageable);
        }
        return new PaginaLinhas(p.getContent().stream().map(LinhaResponse::de).toList(), p.getTotalElements(),
                p.getNumber(), p.getSize(), p.getTotalPages());
    }

    // Conferência (dono do lote)

    @PutMapping("/linhas/{id}")
    public LinhaResponse corrigir(@PathVariable Long id, @RequestBody CorrecaoRequest c, Authentication auth) {
        LinhaPlanilha dados = new LinhaPlanilha(0, c.areaServico(), c.textoBreve(), c.abreviatura(), c.complemento(),
                c.condicao(), c.cnae(), c.codigoNbs(), c.criadorCodigo()); // o número é o da linha gravada
        return LinhaResponse.de(conferenciaService.corrigir(id, dados, auth.getName()));
    }

    @PostMapping("/linhas/{id}/decisao")
    public Map<String, Object> decidir(@PathVariable Long id, @RequestBody DecisaoRequest corpo, Authentication auth) {
        conferenciaService.decidir(id, corpo.decisao(), auth.getName());
        return Map.of("decisao", corpo.decisao());
    }

    @PostMapping("/{id}/decisao-em-bloco")
    public Map<String, Integer> decidirEmBloco(@PathVariable Long id, @RequestBody DecisaoEmBlocoRequest corpo,
                                               Authentication auth) {
        return Map.of("decididas",
                conferenciaService.decidirEmBloco(id, corpo.scoreMinimo(), corpo.decisao(), auth.getName()));
    }

    @PostMapping("/linhas/{id}/descartar")
    public Map<String, Object> descartar(@PathVariable Long id, Authentication auth) {
        conferenciaService.descartar(id, auth.getName());
        return Map.of("etapa", EtapaLinha.DESCARTADA);
    }

    @PostMapping("/{id}/enviar")
    public Map<String, Integer> enviarAoAprovador(@PathVariable Long id, Authentication auth) {
        return Map.of("enviadas", conferenciaService.enviarAoAprovador(id, auth.getName()));
    }

    // Aprovação (só APROVADOR)

    @GetMapping("/aprovacao/lotes")
    public List<LoteComEnviadas> lotesParaAprovar() {
        return linhaRepository.lotesComLinhasEnviadas().stream()
                .map(r -> new LoteComEnviadas((Long) r[0], (String) r[1], (String) r[2], (LocalDateTime) r[3],
                        ((Number) r[4]).longValue(), ((Number) r[5]).longValue()))
                .toList();
    }

    @PostMapping("/aprovacao/linhas/{id}/aprovar")
    public Map<String, Object> aprovar(@PathVariable Long id, Authentication auth) {
        return Map.of("etapa", aprovacaoService.aprovar(id, auth.getName()));
    }

    @PostMapping("/aprovacao/linhas/{id}/rejeitar")
    public Map<String, Object> rejeitar(@PathVariable Long id, @RequestBody RejeicaoRequest corpo, Authentication auth) {
        aprovacaoService.rejeitar(id, auth.getName(), corpo.motivo());
        return Map.of("etapa", EtapaLinha.REJEITADA);
    }

    @PostMapping("/aprovacao/{id}/aprovar-em-bloco")
    public ResumoAprovacao aprovarEmBloco(@PathVariable Long id, @RequestBody(required = false) FiltroAprovacao filtro,
                                          Authentication auth) {
        return aprovacaoService.aprovarEmBloco(id, filtro, auth.getName());
    }

    @PostMapping("/aprovacao/{id}/rejeitar-em-bloco")
    public Map<String, Integer> rejeitarEmBloco(@PathVariable Long id, @RequestBody RejeicaoEmBlocoRequest corpo,
                                                Authentication auth) {
        FiltroAprovacao filtro = new FiltroAprovacao(corpo.situacoes(), corpo.scoreMinimo());
        return Map.of("rejeitadas", aprovacaoService.rejeitarEmBloco(id, filtro, auth.getName(), corpo.motivo()));
    }

    // Posse

    /** Etapas que o aprovador enxerga em lote alheio: o que espera decisão e o que deu conflito. */
    static final Set<EtapaLinha> ETAPAS_DO_APROVADOR = Set.of(EtapaLinha.ENVIADA, EtapaLinha.CONFLITO);

    private static EtapaLinha etapaVisivelAoAprovador(EtapaLinha pedida) {
        return pedida != null && ETAPAS_DO_APROVADOR.contains(pedida) ? pedida : EtapaLinha.ENVIADA;
    }

    /** O dono vê o lote; o aprovador também vê o de outros enquanto houver linha enviada ou em conflito. Os demais: 404. */
    private LoteImportacao loteVisivel(Long id, Authentication auth) {
        LoteImportacao lote = loteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Lote de importação não encontrado: " + id));
        if (ehDono(lote, auth)) {
            return lote;
        }
        boolean aprovador = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_APROVADOR"));
        if (aprovador && linhaRepository.existsByLoteIdAndEtapaIn(id, ETAPAS_DO_APROVADOR)) {
            return lote;
        }
        throw new NoSuchElementException("Lote de importação não encontrado: " + id);
    }

    private static boolean ehDono(LoteImportacao lote, Authentication auth) {
        return auth.getName().equals(lote.getEnviadoPor());
    }
}
