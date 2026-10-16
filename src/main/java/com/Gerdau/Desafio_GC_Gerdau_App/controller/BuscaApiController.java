package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.BuscaResultado;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ConfirmacaoRequest;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.HorarioDto;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatHorarioRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.service.BuscaServicoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BuscaApiController {

    private final BuscaServicoService service;
    private final CatHorarioRepository horarioRepository;

    public BuscaApiController(BuscaServicoService service, CatHorarioRepository horarioRepository) {
        this.service = service;
        this.horarioRepository = horarioRepository;
    }

    @GetMapping("/nbs/buscar")
    public BuscaResultado buscar(@RequestParam String termo) {
        return service.buscar(termo);
    }

    @GetMapping("/horarios")
    public List<HorarioDto> horarios() {
        return horarioRepository.findAllByOrderByDescricaoHorarioAsc().stream()
                .map(h -> new HorarioDto(h.getDescricaoHorario(), h.getSufixoCodigo()))
                .toList();
    }

    @GetMapping("/smart-key")
    public Map<String, String> smartKey(@RequestParam String nbs, @RequestParam String horario) {
        return Map.of("chave", service.gerarSmartKey(nbs, horario));
    }

    @PostMapping("/nbs/confirmar")
    public Map<String, Boolean> confirmar(@RequestBody ConfirmacaoRequest req) {
        return Map.of("gravado", service.confirmar(req.termo(), req.codigoNbs(), req.score()));
    }
}
