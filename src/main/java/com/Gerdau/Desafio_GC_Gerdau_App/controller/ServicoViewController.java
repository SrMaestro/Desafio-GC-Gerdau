package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ServicoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ServicoViewController {

    private final ServicoRepository repository;

    public ServicoViewController(ServicoRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/")
    public String listar(@RequestParam(required = false) String area, Model model) {
        List<Servico> todos = repository.findAll();
        List<String> areas = todos.stream().map(Servico::getAreaServico).distinct().sorted().toList();
        List<Servico> servicos = (area == null || area.isBlank()) ? todos : repository.findByAreaServico(area);

        model.addAttribute("servicos", servicos);
        model.addAttribute("areas", areas);
        model.addAttribute("areaSelecionada", area);
        return "servicos";
    }

    @GetMapping("/ui/servicos/{id}")
    public String detalhe(@PathVariable Integer id, Model model) {
        model.addAttribute("servico", repository.findById(id).orElseThrow());
        return "servico-detalhe";
    }
}
