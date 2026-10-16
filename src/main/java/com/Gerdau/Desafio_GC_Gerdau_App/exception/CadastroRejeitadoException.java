package com.Gerdau.Desafio_GC_Gerdau_App.exception;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.AnaliseSimilaridade;

/** Cadastro não efetuado: duplicado (bloqueado) ou parecido com algo existente e sem confirmação. */
public class CadastroRejeitadoException extends RuntimeException {

    private final AnaliseSimilaridade analise;

    public CadastroRejeitadoException(AnaliseSimilaridade analise) {
        super(analise.bloqueado()
                ? "Serviço duplicado de " + analise.codigoGerado()
                : "Existem serviços parecidos; confirme para cadastrar mesmo assim");
        this.analise = analise;
    }

    public AnaliseSimilaridade getAnalise() {
        return analise;
    }
}
