package com.Gerdau.Desafio_GC_Gerdau_App.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dicionario_de_para")
@Getter
@Setter
@NoArgsConstructor
public class DicionarioDePara {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nome_sujo_digitado", nullable = false, unique = true)
    private String nomeSujoDigitado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_nbs_oficial")
    private CatNbs codigoNbsOficial;

    @Column(name = "score_confianca")
    private Double scoreConfianca;
}
