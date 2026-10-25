package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.exception.ArquivoInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;
import java.util.NoSuchElementException;

/** Erros da API de importação em {@code {"erro": mensagem}}; vale só para o {@link ImportacaoController}. */
@RestControllerAdvice(assignableTypes = ImportacaoController.class)
public class ImportacaoExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String, String>> proibido(AccessDeniedException e) {
        return erro(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<Map<String, String>> naoEncontrado(NoSuchElementException e) {
        return erro(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Map<String, String>> conflito(IllegalStateException e) {
        return erro(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, ArquivoInvalidoException.class})
    ResponseEntity<Map<String, String>> invalido(RuntimeException e) {
        return erro(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<Map<String, String>> grande(MaxUploadSizeExceededException e) {
        return erro(HttpStatus.BAD_REQUEST, "O arquivo excede o tamanho máximo de 10 MB");
    }

    private static ResponseEntity<Map<String, String>> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("erro", mensagem == null ? status.getReasonPhrase() : mensagem));
    }
}
