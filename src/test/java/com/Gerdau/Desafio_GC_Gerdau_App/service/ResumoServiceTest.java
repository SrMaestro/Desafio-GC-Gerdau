package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.Indicadores;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.Resumo;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DecisaoConsulta;
import com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.ResultadoConsulta;
import com.Gerdau.Desafio_GC_Gerdau_App.model.StatusDePara;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ConsultaCadastroRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.DicionarioDeParaRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.LinhaImportacaoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumoServiceTest {

    @Mock
    ServicoRepository servicoRepository;

    @Mock
    DicionarioDeParaRepository dicionarioDeParaRepository;

    @Mock
    LinhaImportacaoRepository linhaImportacaoRepository;

    @Mock
    ConsultaCadastroRepository consultaCadastroRepository;

    @InjectMocks
    ResumoService service;

    @Test
    void aprovador_devolveAsTresContagens() {
        when(servicoRepository.count()).thenReturn(1122L);
        when(dicionarioDeParaRepository.countByStatus(StatusDePara.PENDENTE)).thenReturn(37L);
        when(linhaImportacaoRepository.countByEtapa(EtapaLinha.ENVIADA)).thenReturn(5L);

        assertThat(service.resumo(true)).isEqualTo(new Resumo(1122L, 37L, 5L));
    }

    @Test
    void comprador_naoConsultaLinhasEDevolveNullNoTerceiro() {
        when(servicoRepository.count()).thenReturn(1122L);
        when(dicionarioDeParaRepository.countByStatus(StatusDePara.PENDENTE)).thenReturn(37L);

        Resumo resumo = service.resumo(false);

        assertThat(resumo.aguardandoAprovacao()).isNull();
        verifyNoInteractions(linhaImportacaoRepository);
    }

    @Test
    void bancoVazio_devolveZeros() {
        when(servicoRepository.count()).thenReturn(0L);
        when(dicionarioDeParaRepository.countByStatus(StatusDePara.PENDENTE)).thenReturn(0L);
        when(linhaImportacaoRepository.countByEtapa(EtapaLinha.ENVIADA)).thenReturn(0L);

        assertThat(service.resumo(true)).isEqualTo(new Resumo(0L, 0L, 0L));
    }

    @Test
    void contagemQueFalha_viraNullSemPropagar() {
        when(servicoRepository.count()).thenReturn(1122L);
        when(dicionarioDeParaRepository.countByStatus(any()))
                .thenThrow(new DataAccessResourceFailureException("banco fora"));
        when(linhaImportacaoRepository.countByEtapa(EtapaLinha.ENVIADA)).thenReturn(5L);

        Resumo resumo = service.resumo(true);

        assertThat(resumo.pendentes()).isNull();
        assertThat(resumo.servicos()).isEqualTo(1122L);
        assertThat(resumo.aguardandoAprovacao()).isEqualTo(5L);
    }

    @Test
    void indicadores_devolvemAsContagensDoRegistroDeConsultas() {
        when(consultaCadastroRepository.count()).thenReturn(40L);
        when(consultaCadastroRepository.countByResultado(ResultadoConsulta.BLOQUEADO)).thenReturn(12L);
        when(consultaCadastroRepository.countByDecisao(DecisaoConsulta.REUTILIZOU_EXISTENTE)).thenReturn(8L);
        when(consultaCadastroRepository.countByDecisao(DecisaoConsulta.CADASTROU_NOVO)).thenReturn(15L);

        assertThat(service.indicadores()).isEqualTo(new Indicadores(40L, 12L, 8L, 15L));
    }

    @Test
    void indicadores_semRegistrosDevolveZeros() {
        assertThat(service.indicadores()).isEqualTo(new Indicadores(0L, 0L, 0L, 0L));
    }

    @Test
    void indicadores_contagemQueFalha_viraNullSemPropagar() {
        when(consultaCadastroRepository.count()).thenReturn(40L);
        when(consultaCadastroRepository.countByResultado(any()))
                .thenThrow(new DataAccessResourceFailureException("banco fora"));
        when(consultaCadastroRepository.countByDecisao(DecisaoConsulta.REUTILIZOU_EXISTENTE)).thenReturn(8L);
        when(consultaCadastroRepository.countByDecisao(DecisaoConsulta.CADASTROU_NOVO)).thenReturn(15L);

        Indicadores indicadores = service.indicadores();

        assertThat(indicadores.barradas()).isNull();
        assertThat(indicadores.tentativas()).isEqualTo(40L);
        assertThat(indicadores.reutilizadas()).isEqualTo(8L);
        assertThat(indicadores.semCorrespondencia()).isEqualTo(15L);
    }
}
