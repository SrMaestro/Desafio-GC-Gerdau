package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SinonimoServico;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.SinonimoServicoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** Sinônimos aprovados: textos que já se sabe equivalerem a um serviço existente. */
@Service
public class SinonimoServicoService {

    private final SinonimoServicoRepository repository;

    public SinonimoServicoService(SinonimoServicoRepository repository) {
        this.repository = repository;
    }

    /** Serviço ligado ao texto (normalizado) para a condição e o CNAE, se houver sinônimo aprovado. */
    public Optional<Servico> reconhecer(String texto, String condicaoDescricao, String cnae) {
        if (texto == null || texto.isBlank()) {
            return Optional.empty();
        }
        return repository.findByTextoNormalizadoAndCondicaoAndCnae(
                DeParaService.normalizar(texto), condicaoDescricao, cnae)
                .map(SinonimoServico::getServico);
    }

    public SinonimoServico registrar(String texto, String condicaoDescricao, String cnae,
                                     Servico servico, String aprovadoPor, Long linhaId) {
        SinonimoServico s = new SinonimoServico();
        s.setTextoNormalizado(DeParaService.normalizar(texto));
        s.setTextoOriginal(texto);
        s.setCondicao(condicaoDescricao);
        s.setCnae(cnae);
        s.setServico(servico);
        s.setAprovadoPor(aprovadoPor);
        s.setLinhaId(linhaId);
        return repository.save(s);
    }
}
