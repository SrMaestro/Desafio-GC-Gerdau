package com.Gerdau.Desafio_GC_Gerdau_App.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "item")
@Getter
@Setter
@NoArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_produto", length = 30)
    private String codigoProduto;

    @Column(name = "codigo_servico", length = 30)
    private String codigoServico;

    @Column(name = "descricao_item", nullable = false, length = 255)
    private String descricaoItem;

    @Column(name = "unidade_medida", nullable = false, length = 10)
    private String unidadeMedida;
}