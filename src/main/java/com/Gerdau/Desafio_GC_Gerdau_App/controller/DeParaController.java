package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.ConfirmacaoRequest;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ConfirmacaoResponse;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.DeParaResponse;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.RevisaoRequest;
import com.Gerdau.Desafio_GC_Gerdau_App.exception.DeParaNaoEncontradoException;
import com.Gerdau.Desafio_GC_Gerdau_App.service.DeParaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/de-para")
public class DeParaController {

    private final DeParaService service;

    public DeParaController(DeParaService service) {
        this.service = service;
    }

    @PostMapping("/confirmacoes")
    public ResponseEntity<ConfirmacaoResponse> confirmar(@Valid @RequestBody ConfirmacaoRequest request) {
        boolean registrado = service.confirmar(request.nomeDigitado(), request.codigoNbs(), request.score());
        return ResponseEntity.status(registrado ? HttpStatus.CREATED : HttpStatus.OK)
                .body(new ConfirmacaoResponse(registrado));
    }

    @GetMapping("/fila")
    public List<DeParaResponse> fila() {
        return service.listarFila().stream().map(DeParaResponse::de).toList();
    }

    @PostMapping("/{id}/aprovar")
    public DeParaResponse aprovar(@PathVariable Long id, @Valid @RequestBody RevisaoRequest request) {
        return DeParaResponse.de(service.aprovar(id, request.revisor()));
    }

    @PostMapping("/{id}/rejeitar")
    public DeParaResponse rejeitar(@PathVariable Long id, @Valid @RequestBody RevisaoRequest request) {
        return DeParaResponse.de(service.rejeitar(id, request.revisor(), request.motivo()));
    }

    @PostMapping("/{id}/revogar")
    public DeParaResponse revogar(@PathVariable Long id, @Valid @RequestBody RevisaoRequest request) {
        return DeParaResponse.de(service.revogar(id, request.revisor(), request.motivo()));
    }

    @ExceptionHandler(DeParaNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(DeParaNaoEncontradoException e) {
        return erro(HttpStatus.NOT_FOUND, e);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalido(IllegalArgumentException e) {
        return erro(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflito(IllegalStateException e) {
        return erro(HttpStatus.CONFLICT, e);
    }

    private ResponseEntity<Map<String, String>> erro(HttpStatus status, RuntimeException e) {
        return ResponseEntity.status(status).body(Map.of("erro", e.getMessage()));
    }
}
