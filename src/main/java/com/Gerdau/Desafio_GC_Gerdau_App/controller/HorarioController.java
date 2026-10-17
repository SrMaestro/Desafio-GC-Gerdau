package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.HorarioResponse;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatHorarioRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/horarios")
public class HorarioController {

    private final CatHorarioRepository repository;

    public HorarioController(CatHorarioRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<HorarioResponse> listar() {
        return repository.findAllByOrderByIdAsc().stream().map(HorarioResponse::de).toList();
    }
}
