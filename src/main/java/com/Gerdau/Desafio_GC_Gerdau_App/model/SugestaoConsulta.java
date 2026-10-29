package com.Gerdau.Desafio_GC_Gerdau_App.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "consulta_cadastro_sugestoes")
@Getter
@Setter
@NoArgsConstructor
public class SugestaoConsulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consulta_id", nullable = false)
    private ConsultaCadastro consulta;

    @Column(name = "servico_id", nullable = false)
    private Integer servicoId;

    @Column(name = "codigo_padronizado", nullable = false, length = 40)
    private String codigoPadronizado;

    @Column(name = "texto_breve", nullable = false)
    private String textoBreve;

    @Column(nullable = false)
    private double score;
}
