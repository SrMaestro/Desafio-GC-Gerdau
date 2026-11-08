package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.service.MonitoramentoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Painel de qualidade da base para o aprovador (duplicatas possíveis, fila, tempo de aprovação). */
@Controller
public class MonitoramentoViewController {

    private final MonitoramentoService service;

    public MonitoramentoViewController(MonitoramentoService service) {
        this.service = service;
    }

    @GetMapping("/ui/monitoramento")
    public String painel(Model model) {
        model.addAttribute("painel", service.painel());
        return "monitoramento";
    }
}
