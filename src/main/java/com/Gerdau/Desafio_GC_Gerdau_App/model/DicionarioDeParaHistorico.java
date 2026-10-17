package com.Gerdau.Desafio_GC_Gerdau_App.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "dicionario_de_para_historico")
@Getter
@Setter
@NoArgsConstructor
public class DicionarioDeParaHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "de_para_id", nullable = false)
    private Long deParaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "de_status", length = 10)
    private StatusDePara deStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "para_status", nullable = false, length = 10)
    private StatusDePara paraStatus;

    @Column(length = 100)
    private String usuario;

    private String motivo;

    @Column(name = "em", insertable = false, updatable = false)
    private LocalDateTime em;
}
