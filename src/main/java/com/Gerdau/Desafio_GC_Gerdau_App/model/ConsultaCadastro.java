package com.Gerdau.Desafio_GC_Gerdau_App.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "consultas_cadastro")
@Getter
@Setter
@NoArgsConstructor
public class ConsultaCadastro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false, length = 100)
    private String usuario;

    @Column(name = "texto_breve", nullable = false)
    private String textoBreve;

    @Column(length = 50)
    private String condicao;

    @Column(length = 15)
    private String cnae;

    @Column(name = "codigo_gerado", length = 40)
    private String codigoGerado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResultadoConsulta resultado;

    @Enumerated(EnumType.STRING)
    @Column(length = 25)
    private DecisaoConsulta decisao;

    @Column(name = "servico_id")
    private Integer servicoId;

    @OneToMany(mappedBy = "consulta", cascade = CascadeType.ALL)
    @OrderBy("score DESC")
    private List<SugestaoConsulta> sugestoes = new ArrayList<>();
}
