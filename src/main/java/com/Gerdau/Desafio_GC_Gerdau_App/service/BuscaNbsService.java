package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.ResultadoBuscaNbs;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ResultadoBuscaNbs.Tipo;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.SugestaoNbs;
import com.Gerdau.Desafio_GC_Gerdau_App.model.StatusDePara;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatNbsRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.DicionarioDeParaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** Busca de NBS: primeiro o De-Para aprovado (exato), depois o catálogo por similaridade. */
@Service
public class BuscaNbsService {

    static final double LIMIAR_SUGESTAO = 0.2;
    static final int MAX_SUGESTOES = 5;
    static final int TAMANHO_MIN_TEXTO = 2;

    private final DicionarioDeParaRepository deParaRepository;
    private final CatNbsRepository catNbsRepository;

    public BuscaNbsService(DicionarioDeParaRepository deParaRepository, CatNbsRepository catNbsRepository) {
        this.deParaRepository = deParaRepository;
        this.catNbsRepository = catNbsRepository;
    }

    public ResultadoBuscaNbs buscar(String texto) {
        if (texto == null || texto.trim().length() < TAMANHO_MIN_TEXTO) {
            return new ResultadoBuscaNbs(Tipo.SUGESTOES, List.of());
        }
        String digitado = texto.trim();

        var aprovado = deParaRepository
                .findByNomeNormalizadoAndStatus(DeParaService.normalizar(digitado), StatusDePara.APROVADO)
                .flatMap(m -> catNbsRepository.findById(m.getCodigoNbs()));
        if (aprovado.isPresent()) {
            var nbs = aprovado.get();
            return new ResultadoBuscaNbs(Tipo.EXATO,
                    List.of(new SugestaoNbs(nbs.getCodigoNbs(), nbs.getDescricaoAtividade(), 1.0)));
        }

        List<SugestaoNbs> sugestoes = catNbsRepository.buscarSugestoes(digitado, LIMIAR_SUGESTAO, MAX_SUGESTOES)
                .stream()
                .map(s -> new SugestaoNbs(s.getCodigoNbs(), s.getDescricao(), s.getScore()))
                .toList();
        return new ResultadoBuscaNbs(Tipo.SUGESTOES, sugestoes);
    }
}
