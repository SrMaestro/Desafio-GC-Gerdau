package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.repository.ConsultaCadastroRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

/** Histórico das tentativas de cadastro, para o aprovador conferir o que aconteceu em cada solicitação. */
@Controller
public class ConsultaViewController {

    private final ConsultaCadastroRepository repository;

    public ConsultaViewController(ConsultaCadastroRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/ui/consultas")
    public String listar(Model model) {
        model.addAttribute("consultas", repository.findTop200ByOrderByCriadoEmDesc());
        return "consultas";
    }

    @GetMapping("/ui/consultas/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("consulta", repository.findComSugestoesById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        return "consulta-detalhe";
    }
}
