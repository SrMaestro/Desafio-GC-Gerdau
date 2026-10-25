package com.Gerdau.Desafio_GC_Gerdau_App.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "linhas_importacao")
@Getter
@Setter
@NoArgsConstructor
public class LinhaImportacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private LoteImportacao lote;

    /** Número da linha na planilha. */
    @Column(nullable = false)
    private int numero;

    @Column(name = "area_servico", length = 50)
    private String areaServico;

    @Column(name = "texto_breve")
    private String textoBreve;

    @Column(length = 10)
    private String abreviatura;

    @Column(length = 15)
    private String complemento;

    @Column(length = 50)
    private String condicao;

    @Column(length = 15)
    private String cnae;

    @Column(name = "codigo_nbs", length = 20)
    private String codigoNbs;

    @Column(name = "criador_codigo", length = 100)
    private String criadorCodigo;

    @Column(name = "codigo_gerado", length = 40)
    private String codigoGerado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private SituacaoLinha situacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EtapaLinha etapa;

    @Column(name = "servico_parecido_id")
    private Integer servicoParecidoId;

    private Double score;

    @Column(length = 500)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private DecisaoParecida decisao;

    @Column(name = "conferido_por", length = 100)
    private String conferidoPor;

    /** Quando o comprador decidiu por último (corrigir, decidir, descartar); nulo se não tocou na linha. */
    @Column(name = "conferido_em")
    private LocalDateTime conferidoEm;

    @Column(name = "enviado_em")
    private LocalDateTime enviadoEm;

    @Column(name = "decidido_por", length = 100)
    private String decididoPor;

    @Column(name = "decidido_em")
    private LocalDateTime decididoEm;

    @Column(name = "servico_criado_id")
    private Integer servicoCriadoId;
}
