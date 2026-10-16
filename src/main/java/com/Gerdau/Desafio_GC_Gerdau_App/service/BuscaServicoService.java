package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.BuscaResultado;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.SugestaoDto;
import com.Gerdau.Desafio_GC_Gerdau_App.model.CatNbs;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DicionarioDePara;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatHorarioRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatNbsRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.DicionarioDeParaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class BuscaServicoService {

    private static final int LIMITE_CODIGO_SAP = 40;

    private final CatNbsRepository nbsRepository;
    private final CatHorarioRepository horarioRepository;
    private final DicionarioDeParaRepository deParaRepository;

    public BuscaServicoService(CatNbsRepository nbsRepository,
                               CatHorarioRepository horarioRepository,
                               DicionarioDeParaRepository deParaRepository) {
        this.nbsRepository = nbsRepository;
        this.horarioRepository = horarioRepository;
        this.deParaRepository = deParaRepository;
    }

    /** Fall-back semântico: 1) busca exata no De-Para, 2) busca fuzzy no catálogo NBS. */
    @Transactional(readOnly = true)
    public BuscaResultado buscar(String termo) {
        String normalizado = normalizar(termo);
        if (normalizado.isEmpty()) {
            return new BuscaResultado("FUZZY", List.of());
        }

        var exato = deParaRepository.findByNomeSujoDigitado(normalizado);
        if (exato.isPresent() && exato.get().getCodigoNbsOficial() != null) {
            DicionarioDePara dp = exato.get();
            CatNbs nbs = dp.getCodigoNbsOficial();
            double score = dp.getScoreConfianca() != null ? dp.getScoreConfianca() : 1.0;
            return new BuscaResultado("EXATA",
                    List.of(new SugestaoDto(nbs.getCodigoNbs(), nbs.getDescricaoAtividade(), score)));
        }

        List<SugestaoDto> sugestoes = nbsRepository.buscarSugestoesFuzzy(normalizado).stream()
                .map(s -> new SugestaoDto(s.getCodigoNbs(), s.getDescricaoAtividade(), s.getScore()))
                .toList();
        return new BuscaResultado("FUZZY", sugestoes);
    }

    /** Smart Key = código NBS + "-" + sufixo do horário (ex.: 1.01.01-HE). */
    @Transactional(readOnly = true)
    public String gerarSmartKey(String codigoNbs, String sufixoHorario) {
        if (!nbsRepository.existsById(codigoNbs)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Código NBS não encontrado: " + codigoNbs);
        }
        if (horarioRepository.findBySufixoCodigo(sufixoHorario).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Horário não encontrado: " + sufixoHorario);
        }
        String chave = codigoNbs + "-" + sufixoHorario;
        if (chave.length() > LIMITE_CODIGO_SAP) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Smart Key excede o limite de " + LIMITE_CODIGO_SAP + " caracteres");
        }
        return chave;
    }

    /** Retroalimentação: grava no De-Para o termo digitado que o usuário confirmou. Retorna true se gravou. */
    @Transactional
    public boolean confirmar(String termo, String codigoNbs, Double score) {
        String normalizado = normalizar(termo);
        if (normalizado.isEmpty() || codigoNbs == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "termo e codigoNbs são obrigatórios");
        }
        CatNbs nbs = nbsRepository.findById(codigoNbs)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Código NBS não encontrado: " + codigoNbs));
        if (deParaRepository.existsByNomeSujoDigitado(normalizado)) {
            return false;
        }
        DicionarioDePara dp = new DicionarioDePara();
        dp.setNomeSujoDigitado(normalizado);
        dp.setCodigoNbsOficial(nbs);
        dp.setScoreConfianca(score);
        deParaRepository.save(dp);
        return true;
    }

    private static String normalizar(String termo) {
        return termo == null ? "" : termo.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
