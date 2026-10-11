package com.Gerdau.Desafio_GC_Gerdau_App.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "servicos_mock")
@Getter
@Setter
@NoArgsConstructor
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servico")
    private Integer id;

    @Column(name = "area_servico", nullable = false, length = 50)
    private String areaServico;

    @Column(name = "texto_breve", nullable = false)
    private String textoBreve;

    @Column(name = "abreviatura", nullable = false, length = 10)
    private String abreviatura;

    @Column(name = "complemento", length = 15)
    private String complemento;

    @Column(name = "id1_condicao", nullable = false)
    private String id1Condicao;

    @Column(name = "id2_cnae", nullable = false, length = 15)
    private String id2Cnae;

    @Column(name = "codigo_padronizado", nullable = false, unique = true, length = 40)
    private String codigoPadronizado;

    @Column(name = "criador_codigo")
    private String criadorCodigo;

    @Column(name = "data_criacao", insertable = false, updatable = false)
    private LocalDateTime dataCriacao;
}