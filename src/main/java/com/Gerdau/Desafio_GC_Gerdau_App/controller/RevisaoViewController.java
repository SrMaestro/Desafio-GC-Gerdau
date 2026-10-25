package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.DeParaResponse;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.GrupoRevisao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.CatNbs;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatNbsRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.service.DeParaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class RevisaoViewController {

    private final DeParaService service;
    private final CatNbsRepository catNbsRepository;

    public RevisaoViewController(DeParaService service, CatNbsRepository catNbsRepository) {
        this.service = service;
        this.catNbsRepository = catNbsRepository;
    }

    @GetMapping("/ui/revisao")
    public String revisao(Model model) {
        List<DeParaResponse> pendentes = service.listarFila().stream().map(DeParaResponse::de).toList();
        List<DeParaResponse> aprovados = service.listarAprovados().stream().map(DeParaResponse::de).toList();

        Map<String, List<DeParaResponse>> porNome = pendentes.stream()
                .collect(Collectors.groupingBy(DeParaResponse::nomeNormalizado, LinkedHashMap::new, Collectors.toList()));
        List<GrupoRevisao> grupos = porNome.entrySet().stream()
                .map(e -> new GrupoRevisao(e.getValue().get(0).nomeOriginal(), e.getValue()))
                .toList();

        List<String> codigos = java.util.stream.Stream.concat(pendentes.stream(), aprovados.stream())
                .map(DeParaResponse::codigoNbs).distinct().toList();
        Map<String, String> descricoes = catNbsRepository.findAllById(codigos).stream()
                .collect(Collectors.toMap(CatNbs::getCodigoNbs, CatNbs::getDescricaoAtividade));

        model.addAttribute("grupos", grupos);
        model.addAttribute("totalPendentes", pendentes.size());
        model.addAttribute("aprovados", aprovados);
        model.addAttribute("descricoes", descricoes);
        return "revisao";
    }

    @PostMapping("/ui/revisao/{id}/aprovar")
    public String aprovar(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        return executar(redirect, "Mapeamento aprovado.", () -> service.aprovar(id, principal.getName()));
    }

    @PostMapping("/ui/revisao/{id}/rejeitar")
    public String rejeitar(@PathVariable Long id, Principal principal,
                           @RequestParam String motivo, RedirectAttributes redirect) {
        return executar(redirect, "Mapeamento rejeitado.", () -> service.rejeitar(id, principal.getName(), motivo));
    }

    @PostMapping("/ui/revisao/{id}/revogar")
    public String revogar(@PathVariable Long id, Principal principal,
                          @RequestParam String motivo, RedirectAttributes redirect) {
        return executar(redirect, "Mapeamento revogado.", () -> service.revogar(id, principal.getName(), motivo));
    }

    private String executar(RedirectAttributes redirect, String sucesso, Runnable acao) {
        try {
            acao.run();
            redirect.addFlashAttribute("mensagem", sucesso);
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/ui/revisao";
    }
}
