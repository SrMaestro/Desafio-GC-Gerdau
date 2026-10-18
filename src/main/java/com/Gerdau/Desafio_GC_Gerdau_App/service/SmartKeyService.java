package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.SmartKey;
import com.Gerdau.Desafio_GC_Gerdau_App.model.CatHorario;
import com.Gerdau.Desafio_GC_Gerdau_App.model.CatNbs;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatHorarioRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatNbsRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class SmartKeyService {

    private final CatNbsRepository catNbsRepository;
    private final CatHorarioRepository catHorarioRepository;

    public SmartKeyService(CatNbsRepository catNbsRepository, CatHorarioRepository catHorarioRepository) {
        this.catNbsRepository = catNbsRepository;
        this.catHorarioRepository = catHorarioRepository;
    }

    /** Monta a Smart Key só se o NBS e o horário existem nos catálogos. */
    public SmartKey gerar(String codigoNbs, String sufixo) {
        if (codigoNbs == null || codigoNbs.isBlank()) {
            throw new IllegalArgumentException("Informe o código NBS");
        }
        if (sufixo == null || sufixo.isBlank()) {
            throw new IllegalArgumentException("Informe o horário");
        }
        String nbsInformado = codigoNbs.trim();
        String sufixoInformado = sufixo.trim().toUpperCase(Locale.ROOT);

        CatNbs nbs = catNbsRepository.findById(nbsInformado)
                .orElseThrow(() -> new IllegalArgumentException("Código NBS desconhecido: " + nbsInformado));
        CatHorario horario = catHorarioRepository.findBySufixoCodigoAndAtivoTrue(sufixoInformado)
                .orElseThrow(() -> new IllegalArgumentException("Horário desconhecido: " + sufixoInformado));

        return new SmartKey(nbs.getCodigoNbs() + "-" + horario.getSufixoCodigo(), nbs.getCodigoNbs(),
                nbs.getDescricaoAtividade(), horario.getSufixoCodigo(), horario.getDescricaoHorario());
    }
}
