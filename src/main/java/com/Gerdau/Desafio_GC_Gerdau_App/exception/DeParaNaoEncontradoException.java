package com.Gerdau.Desafio_GC_Gerdau_App.exception;

/** Mapeamento do dicionário De-Para inexistente. */
public class DeParaNaoEncontradoException extends IllegalArgumentException {

    public DeParaNaoEncontradoException(Long id) {
        super("Mapeamento não encontrado: " + id);
    }
}
