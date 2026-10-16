package com.Gerdau.Desafio_GC_Gerdau_App.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cat_nbs")
@Getter
@Setter
@NoArgsConstructor
public class CatNbs {

    @Id
    @Column(name = "codigo_nbs", length = 20)
    private String codigoNbs;

    @Column(name = "descricao_atividade", nullable = false)
    private String descricaoAtividade;
}
