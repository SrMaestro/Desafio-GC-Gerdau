package com.Gerdau.Desafio_GC_Gerdau_App.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "dicionario_de_para")
@Getter
@Setter
@NoArgsConstructor
public class DicionarioDePara {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_normalizado", nullable = false)
    private String nomeNormalizado;

    @Column(name = "nome_original", nullable = false)
    private String nomeOriginal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "codigo_nbs", nullable = false)
    private CatNbs catNbs;

    @Enumerated(EnumType.STRING)
    @Column(name = "origem", nullable = false, length = 10)
    private OrigemDePara origem;

    private Float score;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private StatusDePara status = StatusDePara.PENDENTE;

    @Column(name = "confirmacoes", nullable = false)
    private Integer confirmacoes = 1;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "revisado_por", length = 100)
    private String revisadoPor;

    @Column(name = "revisado_em")
    private LocalDateTime revisadoEm;

    private String motivo;
}
