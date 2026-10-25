package com.Gerdau.Desafio_GC_Gerdau_App.exception;

/** Planilha de importação recusada por inteiro: formato, cabeçalho, tamanho ou conteúdo inválidos. */
public class ArquivoInvalidoException extends RuntimeException {

    public ArquivoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
