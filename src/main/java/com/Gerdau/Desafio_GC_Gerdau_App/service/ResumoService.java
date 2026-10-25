package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.Resumo;
import com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.StatusDePara;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.DicionarioDeParaRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.LinhaImportacaoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ServicoRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.function.LongSupplier;

@Service
public class ResumoService {

    private final ServicoRepository servicoRepository;
    private final DicionarioDeParaRepository dicionarioDeParaRepository;
    private final LinhaImportacaoRepository linhaImportacaoRepository;

    public ResumoService(ServicoRepository servicoRepository, DicionarioDeParaRepository dicionarioDeParaRepository,
                         LinhaImportacaoRepository linhaImportacaoRepository) {
        this.servicoRepository = servicoRepository;
        this.dicionarioDeParaRepository = dicionarioDeParaRepository;
        this.linhaImportacaoRepository = linhaImportacaoRepository;
    }

    /** Só o aprovador recebe as linhas aguardando aprovação; para o comprador esse total fica {@code null}. */
    public Resumo resumo(boolean aprovador) {
        Long servicos = contar(servicoRepository::count);
        Long pendentes = contar(() -> dicionarioDeParaRepository.countByStatus(StatusDePara.PENDENTE));
        Long aguardando = aprovador
                ? contar(() -> linhaImportacaoRepository.countByEtapa(EtapaLinha.ENVIADA))
                : null;
        return new Resumo(servicos, pendentes, aguardando);
    }

    /** Uma contagem que falha não derruba a tela: vira {@code null} e o card mostra "—". */
    private Long contar(LongSupplier contagem) {
        try {
            return contagem.getAsLong();
        } catch (DataAccessException e) {
            return null;
        }
    }
}
