package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.model.LoteImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.LoteImportacaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

/**
 * Telas da importação em massa para quem cria os códigos. São só a casca: os dados da planilha vêm
 * da API ({@code /api/importacoes}) e são desenhados pelo {@code importacao.js}, nunca pelo servidor.
 */
@Controller
public class ImportacaoViewController {

    private final LoteImportacaoRepository loteRepository;

    public ImportacaoViewController(LoteImportacaoRepository loteRepository) {
        this.loteRepository = loteRepository;
    }

    @GetMapping("/ui/importacoes")
    public String lista() {
        return "importacoes";
    }

    /** Nestas telas só o dono do lote entra; qualquer outro recebe 404, como na API. */
    @GetMapping("/ui/importacoes/{id}")
    public String lote(@PathVariable Long id, Authentication auth, Model model) {
        LoteImportacao lote = loteRepository.findById(id)
                .filter(l -> auth.getName().equals(l.getEnviadoPor()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lote não encontrado"));
        model.addAttribute("loteId", lote.getId());
        return "importacao-lote";
    }
}
