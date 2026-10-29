package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatHorarioRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatNbsRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ServicoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.NovoServicoForm;
import com.Gerdau.Desafio_GC_Gerdau_App.exception.CadastroRejeitadoException;
import com.Gerdau.Desafio_GC_Gerdau_App.service.ResumoService;
import com.Gerdau.Desafio_GC_Gerdau_App.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
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
import java.util.Locale;
import java.util.NoSuchElementException;

@Controller
public class ServicoViewController {

    private final ServicoRepository repository;
    private final ServicoService service;
    private final CatNbsRepository catNbsRepository;
    private final CatHorarioRepository catHorarioRepository;
    private final ResumoService resumoService;

    public ServicoViewController(ServicoRepository repository, ServicoService service,
                                 CatNbsRepository catNbsRepository, CatHorarioRepository catHorarioRepository,
                                 ResumoService resumoService) {
        this.repository = repository;
        this.service = service;
        this.catNbsRepository = catNbsRepository;
        this.catHorarioRepository = catHorarioRepository;
        this.resumoService = resumoService;
    }

    @GetMapping("/")
    public String listar(@RequestParam(required = false) String area, Authentication authentication, Model model) {
        List<Servico> todos = repository.findAll();
        List<String> areas = todos.stream().map(Servico::getAreaServico).distinct().sorted().toList();
        List<Servico> servicos = (area == null || area.isBlank()) ? todos : repository.findByAreaServico(area);

        model.addAttribute("servicos", servicos);
        model.addAttribute("areas", areas);
        model.addAttribute("areaSelecionada", area);
        model.addAttribute("resumo", resumoService.resumo(ehAprovador(authentication)));
        return "servicos";
    }

    private boolean ehAprovador(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_APROVADOR".equals(a.getAuthority()));
    }

    @GetMapping("/ui/busca")
    public String busca() {
        return "busca";
    }

    @GetMapping("/ui/servicos/{id}")
    public String detalhe(@PathVariable Integer id, Model model) {
        model.addAttribute("servico", repository.findById(id).orElseThrow());
        return "servico-detalhe";
    }

    /** nbs e horario (sufixo) vêm da busca de NBS e pré-preenchem o formulário. */
    @GetMapping("/ui/servicos/novo")
    public String novo(@RequestParam(required = false) String nbs,
                       @RequestParam(required = false) String horario, Model model) {
        NovoServicoForm form = new NovoServicoForm();
        if (nbs != null && !nbs.isBlank()) {
            form.setCodigoNbs(nbs.trim());
        }
        if (horario != null && !horario.isBlank()) {
            catHorarioRepository.findBySufixoCodigoAndAtivoTrue(horario.trim().toUpperCase(Locale.ROOT))
                    .ifPresent(h -> form.setCondicao(h.getSufixoCodigo()));
        }
        model.addAttribute("form", form);
        return formulario(model);
    }

    @PostMapping("/ui/servicos")
    public String criar(@Valid @ModelAttribute("form") NovoServicoForm form, BindingResult binding,
                        @RequestParam(defaultValue = "false") boolean confirmar,
                        Authentication authentication, Model model, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return formulario(model);
        }
        try {
            Servico criado = service.cadastrar(form, confirmar, authentication.getName());
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

    /** O comprador escolheu reaproveitar um serviço sugerido: registra a decisão e mostra o serviço escolhido. */
    @PostMapping("/ui/servicos/reutilizar")
    public String reutilizar(@RequestParam String textoBreve, @RequestParam Integer servicoId,
                             Authentication authentication, RedirectAttributes redirect) {
        try {
            Servico escolhido = service.registrarReutilizacao(authentication.getName(), textoBreve, servicoId);
            redirect.addFlashAttribute("mensagem", "Código " + escolhido.getCodigoPadronizado()
                    + " reutilizado. Nenhum serviço novo foi cadastrado.");
            return "redirect:/ui/servicos/" + escolhido.getId();
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("erro", "Serviço não encontrado.");
            return "redirect:/ui/servicos/novo";
        }
    }

    private String formulario(Model model) {
        NovoServicoForm form = (NovoServicoForm) model.getAttribute("form");
        if (form != null && form.getCodigoNbs() != null && !form.getCodigoNbs().isBlank()) {
            catNbsRepository.findById(form.getCodigoNbs().trim())
                    .ifPresent(nbs -> model.addAttribute("nbsSelecionado", nbs));
        }
        model.addAttribute("condicoes", catHorarioRepository.findByAtivoTrueOrderByIdAsc());
        model.addAttribute("areas", repository.findAreas());
        return "servico-form";
    }
}
