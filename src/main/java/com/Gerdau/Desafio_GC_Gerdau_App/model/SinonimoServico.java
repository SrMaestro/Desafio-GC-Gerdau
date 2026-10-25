package com.Gerdau.Desafio_GC_Gerdau_App.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Sinônimo aprovado: texto (+ condição e CNAE) que equivale a um serviço existente. */
@Entity
@Table(name = "servico_sinonimo")
@Getter
@Setter
@NoArgsConstructor
public class SinonimoServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "texto_normalizado", nullable = false)
    private String textoNormalizado;

    /** Descrição da condição, como em servicos_mock.id1_condicao. */
    @Column(nullable = false, length = 50)
    private String condicao;

    @Column(nullable = false, length = 15)
    private String cnae;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @Column(name = "texto_original", nullable = false)
    private String textoOriginal;

    @Column(name = "aprovado_por", nullable = false, length = 100)
    private String aprovadoPor;

    @Column(name = "aprovado_em", insertable = false, updatable = false)
    private LocalDateTime aprovadoEm;

    @Column(name = "linha_id")
    private Long linhaId;
}
