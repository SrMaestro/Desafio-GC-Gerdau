package com.Gerdau.Desafio_GC_Gerdau_App.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.LinhaPlanilha;
import com.Gerdau.Desafio_GC_Gerdau_App.exception.ArquivoInvalidoException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class LeitorPlanilhaTest {

    private static final String CABECALHO = "area_servico;texto_breve;abreviatura;complemento;condicao;cnae;codigo_nbs;criador_codigo";

    private final LeitorPlanilha leitor = new LeitorPlanilha();

    private static byte[] bytes(String texto) {
        return texto.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] xlsx(Object[]... linhas) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet planilha = wb.createSheet("Dados");
            wb.createSheet("Outra").createRow(0).createCell(0).setCellValue("ignorada");
            for (int i = 0; i < linhas.length; i++) {
                Row row = planilha.createRow(i);
                for (int j = 0; j < linhas[i].length; j++) {
                    Object v = linhas[i][j];
                    if (v instanceof Number n) {
                        row.createCell(j).setCellValue(n.doubleValue());
                    } else if (v != null) {
                        row.createCell(j).setCellValue(v.toString());
                    }
                }
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    @Test
    void csv_comPontoEVirgula() {
        String csv = CABECALHO + "\nManutenção;Solda de tubo;SOLD;TIG;Hora normal;4399103;1.0101;ana\n";

        List<LinhaPlanilha> linhas = leitor.ler("lote.csv", bytes(csv));

        assertThat(linhas).containsExactly(new LinhaPlanilha(2, "Manutenção", "Solda de tubo", "SOLD", "TIG",
                "Hora normal", "4399103", "1.0101", "ana"));
    }

    @Test
    void csv_comVirgulaEAspas() {
        String csv = "area_servico,texto_breve,abreviatura,condicao,cnae\n"
                + "Manutenção,\"Solda, TIG\",SOLD,HN,4399103\n";

        List<LinhaPlanilha> linhas = leitor.ler("lote.csv", bytes(csv));

        assertThat(linhas).hasSize(1);
        assertThat(linhas.get(0).textoBreve()).isEqualTo("Solda, TIG");
        assertThat(linhas.get(0).condicao()).isEqualTo("HN");
    }

    @Test
    void csv_comAcentosUtf8() {
        String csv = CABECALHO + "\nCaldeiraria;Inspeção de válvula;INSP;;HN;4399103;;\n";

        List<LinhaPlanilha> linhas = leitor.ler("lote.csv", bytes(csv));

        assertThat(linhas.get(0).textoBreve()).isEqualTo("Inspeção de válvula");
    }

    @Test
    void csv_windows1252_ehLidoComFallback() {
        String csv = CABECALHO + "\nCaldeiraria;Inspeção de válvula;INSP;;HN;4399103;;\n";

        List<LinhaPlanilha> linhas = leitor.ler("lote.csv", csv.getBytes(java.nio.charset.Charset.forName("windows-1252")));

        assertThat(linhas.get(0).textoBreve()).isEqualTo("Inspeção de válvula");
    }

    @Test
    void csv_utf8ValidoContinuaIntacto() {
        String csv = "\uFEFF" + CABECALHO + "\nCaldeiraria;Ã© Inspeção;INSP;;HN;4399103;;\n";

        List<LinhaPlanilha> linhas = leitor.ler("lote.csv", bytes(csv));

        assertThat(linhas.get(0).textoBreve()).isEqualTo("Ã© Inspeção");
        assertThat(linhas.get(0).areaServico()).isEqualTo("Caldeiraria");
    }

    @Test
    void xlsx_primeiraPlanilha() throws Exception {
        byte[] arquivo = xlsx(
                new Object[] {"area_servico", "texto_breve", "abreviatura", "condicao", "cnae"},
                new Object[] {"Manutenção", "Solda de tubo", "SOLD", "HN", "4399103"});

        List<LinhaPlanilha> linhas = leitor.ler("lote.xlsx", arquivo);

        assertThat(linhas).hasSize(1);
        assertThat(linhas.get(0).textoBreve()).isEqualTo("Solda de tubo");
    }

    @Test
    void cabecalho_ignoraCaixaEAcento() {
        String csv = "Área_Serviço;TEXTO_BREVE;Abreviatura;Condição;CNAE\nManutenção;Solda;SOLD;HN;4399103\n";

        List<LinhaPlanilha> linhas = leitor.ler("lote.csv", bytes(csv));

        assertThat(linhas.get(0).areaServico()).isEqualTo("Manutenção");
        assertThat(linhas.get(0).condicao()).isEqualTo("HN");
    }

    @Test
    void colunasOpcionaisAusentes_viramNull() {
        String csv = "area_servico;texto_breve;abreviatura;condicao;cnae\nManutenção;Solda;SOLD;HN;4399103\n";

        LinhaPlanilha linha = leitor.ler("lote.csv", bytes(csv)).get(0);

        assertThat(linha.complemento()).isNull();
        assertThat(linha.codigoNbs()).isNull();
        assertThat(linha.criadorCodigo()).isNull();
    }

    @Test
    void cnaeNumericoDoExcel_semPontoZero() throws Exception {
        byte[] arquivo = xlsx(
                new Object[] {"area_servico", "texto_breve", "abreviatura", "condicao", "cnae"},
                new Object[] {"Manutenção", "Solda", "SOLD", "HN", 4399103});

        List<LinhaPlanilha> linhas = leitor.ler("lote.xlsx", arquivo);

        assertThat(linhas.get(0).cnae()).isEqualTo("4399103");
    }

    @Test
    void numeroDaLinha_eADaPlanilha() {
        String csv = CABECALHO + "\nA;Um;AB;;HN;1;;\nA;Dois;AB;;HN;1;;\n";

        List<LinhaPlanilha> linhas = leitor.ler("lote.csv", bytes(csv));

        assertThat(linhas).extracting(LinhaPlanilha::numero).containsExactly(2, 3);
    }

    @Test
    void semColunaObrigatoria_mensagemListaAsFaltantes() {
        String csv = "area_servico;texto_breve;abreviatura\nA;Um;AB\n";

        assertThatThrownBy(() -> leitor.ler("lote.csv", bytes(csv)))
                .isInstanceOf(ArquivoInvalidoException.class)
                .hasMessageContaining("condicao")
                .hasMessageContaining("cnae")
                .hasMessageNotContaining("texto_breve");
    }

    @Test
    void arquivoVazio_recusa() {
        assertThatThrownBy(() -> leitor.ler("lote.csv", new byte[0]))
                .isInstanceOf(ArquivoInvalidoException.class);
    }

    @Test
    void soCabecalho_recusa() {
        assertThatThrownBy(() -> leitor.ler("lote.csv", bytes(CABECALHO + "\n")))
                .isInstanceOf(ArquivoInvalidoException.class)
                .hasMessageContaining("linhas");
    }

    @Test
    void extensaoInvalida_recusa() {
        assertThatThrownBy(() -> leitor.ler("lote.txt", bytes(CABECALHO + "\nA;Um;AB;;HN;1;;\n")))
                .isInstanceOf(ArquivoInvalidoException.class)
                .hasMessageContaining(".csv");
    }

    @Test
    void acimaDoLimite_recusa() {
        StringBuilder csv = new StringBuilder(CABECALHO).append('\n');
        for (int i = 0; i <= LeitorPlanilha.MAX_LINHAS; i++) {
            csv.append("A;Servico ").append(i).append(";AB;;HN;1;;\n");
        }

        assertThatThrownBy(() -> leitor.ler("lote.csv", bytes(csv.toString())))
                .isInstanceOf(ArquivoInvalidoException.class)
                .hasMessageContaining(String.valueOf(LeitorPlanilha.MAX_LINHAS));
    }

    @Test
    void linhaTotalmenteEmBranco_eIgnorada() {
        String csv = CABECALHO + "\nA;Um;AB;;HN;1;;\n;;;;;;;\nA;Tres;AB;;HN;1;;\n";

        List<LinhaPlanilha> linhas = leitor.ler("lote.csv", bytes(csv));

        assertThat(linhas).extracting(LinhaPlanilha::numero).containsExactly(2, 4);
    }
}
