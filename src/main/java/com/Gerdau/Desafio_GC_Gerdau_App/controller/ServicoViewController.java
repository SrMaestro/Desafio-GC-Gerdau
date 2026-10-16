package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ServicoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.NovoServicoForm;
import com.Gerdau.Desafio_GC_Gerdau_App.exception.CadastroRejeitadoException;
import com.Gerdau.Desafio_GC_Gerdau_App.model.Condicao;
import com.Gerdau.Desafio_GC_Gerdau_App.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ServicoViewController {

    private final ServicoRepository repository;
    private final ServicoService service;

    public ServicoViewController(ServicoRepository repository, ServicoService service) {
        this.repository = repository;
        this.service = service;
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

    @GetMapping("/ui/servicos/novo")
    public String novo(Model model) {
        model.addAttribute("form", new NovoServicoForm());
        return formulario(model);
    }

    @PostMapping("/ui/servicos")
    public String criar(@Valid @ModelAttribute("form") NovoServicoForm form, BindingResult binding,
                        @RequestParam(defaultValue = "false") boolean confirmar,
                        Model model, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return formulario(model);
        }
        try {
            Servico criado = service.cadastrar(form, confirmar);
            redirect.addFlashAttribute("mensagem", "Serviço " + criado.getCodigoPadronizado() + " cadastrado.");
            return "redirect:/ui/servicos/" + criado.getId();
        } catch (CadastroRejeitadoException e) {
            model.addAttribute("analise", e.getAnalise());
            return formulario(model);
        } catch (IllegalArgumentException e) {
            binding.reject("codigo.invalido", e.getMessage());
            return formulario(model);
        }
    }

    private String formulario(Model model) {
        model.addAttribute("condicoes", Condicao.values());
        model.addAttribute("areas", repository.findAreas());
        return "servico-form";
    }
}
