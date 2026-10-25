package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Expõe o usuário logado às telas, para o cabeçalho (nome, Sair e link de Revisão). */
@ControllerAdvice
public class UsuarioLogadoAdvice {

    @ModelAttribute("usuarioLogado")
    String usuarioLogado(Authentication authentication) {
        return authentication == null ? null : authentication.getName();
    }

    @ModelAttribute("ehAprovador")
    boolean ehAprovador(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_APROVADOR".equals(a.getAuthority()));
    }
}
