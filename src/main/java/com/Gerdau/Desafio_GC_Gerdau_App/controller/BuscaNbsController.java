package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.ResultadoBuscaNbs;
import com.Gerdau.Desafio_GC_Gerdau_App.service.BuscaNbsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nbs")
public class BuscaNbsController {

    private final BuscaNbsService service;

    public BuscaNbsController(BuscaNbsService service) {
        this.service = service;
    }

    @GetMapping("/busca")
    public ResultadoBuscaNbs buscar(@RequestParam String texto) {
        return service.buscar(texto);
    }
}
