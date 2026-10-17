package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.SmartKey;
import com.Gerdau.Desafio_GC_Gerdau_App.service.SmartKeyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/smart-key")
public class SmartKeyController {

    private final SmartKeyService service;

    public SmartKeyController(SmartKeyService service) {
        this.service = service;
    }

    @GetMapping
    public SmartKey gerar(@RequestParam String nbs, @RequestParam String horario) {
        return service.gerar(nbs, horario);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalido(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", e.getMessage()));
    }
}
