package com.Gerdau.Desafio_GC_Gerdau_App.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cat_horarios")
@Getter
@Setter
@NoArgsConstructor
public class CatHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horario")
    private Integer id;

    @Column(name = "descricao_horario", nullable = false, length = 100)
    private String descricaoHorario;

    @Column(name = "sufixo_codigo", nullable = false, length = 10)
    private String sufixoCodigo;

    /** Condição aposentada deixa de ser oferecida, mas os serviços já cadastrados não mudam. */
    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;
}
