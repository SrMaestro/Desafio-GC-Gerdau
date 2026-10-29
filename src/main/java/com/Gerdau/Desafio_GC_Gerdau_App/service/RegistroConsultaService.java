package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.AnaliseSimilaridade.SimilarDto;
import com.Gerdau.Desafio_GC_Gerdau_App.model.ConsultaCadastro;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DecisaoConsulta;
import com.Gerdau.Desafio_GC_Gerdau_App.model.ResultadoConsulta;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SugestaoConsulta;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ConsultaCadastroRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Grava o histórico das tentativas de cadastro. É melhor esforço: o registro nunca atrapalha o cadastro, então
 * qualquer falha vira log. Sem {@code @Transactional} de propósito: o {@code save} do repositório é a transação,
 * e uma falha aqui não contamina a transação de quem chamou.
 */
@Service
public class RegistroConsultaService {

    private static final Logger log = LoggerFactory.getLogger(RegistroConsultaService.class);

    private final ConsultaCadastroRepository repository;

    public RegistroConsultaService(ConsultaCadastroRepository repository) {
        this.repository = repository;
    }

    public void registrar(String usuario, String textoBreve, String condicao, String cnae, String codigoGerado,
                          ResultadoConsulta resultado, DecisaoConsulta decisao, Integer servicoId,
                          List<SimilarDto> sugestoes, String justificativa) {
        try {
            ConsultaCadastro consulta = new ConsultaCadastro();
            consulta.setUsuario(usuario);
            consulta.setTextoBreve(textoBreve);
            consulta.setCondicao(condicao);
            consulta.setCnae(cnae);
            consulta.setCodigoGerado(codigoGerado);
            consulta.setResultado(resultado);
            consulta.setDecisao(decisao);
            consulta.setServicoId(servicoId);
            consulta.setJustificativa(justificativa);
            if (sugestoes != null) {
                for (SimilarDto s : sugestoes) {
                    SugestaoConsulta sugestao = new SugestaoConsulta();
                    sugestao.setConsulta(consulta);
                    sugestao.setServicoId(s.id());
                    sugestao.setCodigoPadronizado(s.codigoPadronizado());
                    sugestao.setTextoBreve(s.textoBreve());
                    sugestao.setScore(s.score());
                    consulta.getSugestoes().add(sugestao);
                }
            }
            repository.save(consulta);
        } catch (RuntimeException e) {
            log.warn("Não foi possível registrar a consulta de cadastro ({}): {}", resultado, e.toString());
        }
    }
}
