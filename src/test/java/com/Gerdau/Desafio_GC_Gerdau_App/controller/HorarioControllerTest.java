package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.model.CatHorario;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatHorarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HorarioController.class)
class HorarioControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    CatHorarioRepository repository;

    private CatHorario horario(String sufixo, String descricao) {
        CatHorario h = new CatHorario();
        h.setSufixoCodigo(sufixo);
        h.setDescricaoHorario(descricao);
        return h;
    }

    @Test
    void listar_devolveSufixoEDescricaoNaOrdemDoCatalogo() throws Exception {
        when(repository.findByAtivoTrueOrderByIdAsc())
                .thenReturn(List.of(horario("HN", "Hora normal"), horario("HES", "Hora extra Seg a Sab")));

        mvc.perform(get("/api/horarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sufixo").value("HN"))
                .andExpect(jsonPath("$[0].descricao").value("Hora normal"))
                .andExpect(jsonPath("$[1].sufixo").value("HES"));
    }
}
