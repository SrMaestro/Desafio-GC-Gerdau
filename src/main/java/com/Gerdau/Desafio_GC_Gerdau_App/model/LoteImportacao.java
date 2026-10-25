package com.Gerdau.Desafio_GC_Gerdau_App.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "lotes_importacao")
@Getter
@Setter
@NoArgsConstructor
public class LoteImportacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "arquivo_nome", nullable = false)
    private String arquivoNome;

    @Column(name = "enviado_por", nullable = false, length = 100)
    private String enviadoPor;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusLote status = StatusLote.PROCESSANDO;

    @Column(name = "total_linhas", nullable = false)
    private int totalLinhas;

    @Column(nullable = false)
    private int processadas;

    @Column(name = "mensagem_erro", length = 500)
    private String mensagemErro;
}
