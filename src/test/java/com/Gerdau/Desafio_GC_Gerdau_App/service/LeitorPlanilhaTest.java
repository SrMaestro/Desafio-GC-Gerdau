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

    // ---- .xlsx lido em streaming ----

    private static final String[] CABECALHO_XLSX = {"area_servico", "texto_breve", "abreviatura", "complemento",
            "condicao", "cnae"};

    @Test
    void xlsx_formula_devolveOValorCalculadoNaoOTextoDaFormula() throws Exception {
        byte[] arquivo;
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet planilha = wb.createSheet("Dados");
            Row cab = planilha.createRow(0);
            for (int j = 0; j < CABECALHO_XLSX.length; j++) {
                cab.createCell(j).setCellValue(CABECALHO_XLSX[j]);
            }
            Row r = planilha.createRow(1);
            r.createCell(0).setCellValue("Manutenção");
            r.createCell(1).setCellFormula("TRIM(\"  Solda de tubo  \")");
            r.createCell(2).setCellFormula("UPPER(\"sold\")");
            r.createCell(4).setCellValue("HN");
            r.createCell(5).setCellFormula("4399100+3");
            wb.getCreationHelper().createFormulaEvaluator().evaluateAll(); // grava o valor em cache, como o Excel
            wb.write(out);
            arquivo = out.toByteArray();
        }

        LinhaPlanilha linha = leitor.ler("lote.xlsx", arquivo).get(0);

        assertThat(linha.textoBreve()).isEqualTo("Solda de tubo");
        assertThat(linha.abreviatura()).isEqualTo("SOLD");
        assertThat(linha.cnae()).isEqualTo("4399103");
    }

    @Test
    void xlsx_celulasELinhasFaltandoNoMeio_mantemColunasENumeros() throws Exception {
        byte[] arquivo;
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet planilha = wb.createSheet("Dados");
            Row cab = planilha.createRow(1); // linha 1 da planilha em branco (sem elemento <row>)
            for (int j = 0; j < CABECALHO_XLSX.length; j++) {
                cab.createCell(j).setCellValue(CABECALHO_XLSX[j]);
            }
            Row r = planilha.createRow(2);
            r.createCell(0).setCellValue("Manutenção");
            // texto_breve, abreviatura e complemento sem célula: o streaming pula, a coluna não pode deslocar
            r.createCell(4).setCellValue("HN");
            r.createCell(5).setCellValue(4399103);
            Row r2 = planilha.createRow(5); // linhas 4 e 5 da planilha ausentes
            r2.createCell(1).setCellValue("Pintura");
            r2.createCell(5).setCellValue("4321000");
            wb.write(out);
            arquivo = out.toByteArray();
        }

        List<LinhaPlanilha> linhas = leitor.ler("lote.xlsx", arquivo);

        assertThat(linhas).containsExactly(
                new LinhaPlanilha(3, "Manutenção", null, null, null, "HN", "4399103", null, null),
                new LinhaPlanilha(6, null, "Pintura", null, null, null, "4321000", null, null));
    }

    @Test
    void xlsx_acimaDoLimite_recusa() throws Exception {
        byte[] arquivo;
        try (org.apache.poi.xssf.streaming.SXSSFWorkbook wb = new org.apache.poi.xssf.streaming.SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet planilha = wb.createSheet("Dados");
            Row cab = planilha.createRow(0);
            for (int j = 0; j < CABECALHO_XLSX.length; j++) {
                cab.createCell(j).setCellValue(CABECALHO_XLSX[j]);
            }
            for (int i = 1; i <= LeitorPlanilha.MAX_LINHAS + 1; i++) {
                Row r = planilha.createRow(i);
                r.createCell(0).setCellValue("A");
                r.createCell(1).setCellValue("Servico " + i);
                r.createCell(5).setCellValue(4399103);
            }
            wb.write(out);
            arquivo = out.toByteArray();
        }

        assertThatThrownBy(() -> leitor.ler("lote.xlsx", arquivo))
                .isInstanceOf(ArquivoInvalidoException.class)
                .hasMessageContaining(String.valueOf(LeitorPlanilha.MAX_LINHAS));
    }

    @Test
    void xlsx_noLimiteExato_eAceito() throws Exception {
        byte[] arquivo;
        try (org.apache.poi.xssf.streaming.SXSSFWorkbook wb = new org.apache.poi.xssf.streaming.SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet planilha = wb.createSheet("Dados");
            Row cab = planilha.createRow(0);
            for (int j = 0; j < CABECALHO_XLSX.length; j++) {
                cab.createCell(j).setCellValue(CABECALHO_XLSX[j]);
            }
            for (int i = 1; i <= LeitorPlanilha.MAX_LINHAS; i++) {
                planilha.createRow(i).createCell(1).setCellValue("Servico " + i);
            }
            wb.write(out);
            arquivo = out.toByteArray();
        }

        assertThat(leitor.ler("lote.xlsx", arquivo)).hasSize(LeitorPlanilha.MAX_LINHAS);
    }

    @Test
    void xlsx_bombaDeCompactacao_recusaComMensagemClara() throws Exception {
        byte[] valido = xlsx(
                new Object[] {"area_servico", "texto_breve", "abreviatura", "condicao", "cnae"},
                new Object[] {"Manutenção", "Solda de tubo", "SOLD", "HN", "4399103"});
        // mesma pasta, mas a planilha ganha 30 MB de espaços (XML válido) que compactam para quase nada
        ByteArrayOutputStream bomba = new ByteArrayOutputStream();
        try (java.util.zip.ZipInputStream in = new java.util.zip.ZipInputStream(new java.io.ByteArrayInputStream(valido));
             java.util.zip.ZipOutputStream out = new java.util.zip.ZipOutputStream(bomba)) {
            java.util.zip.ZipEntry e;
            while ((e = in.getNextEntry()) != null) {
                out.putNextEntry(new java.util.zip.ZipEntry(e.getName()));
                out.write(in.readAllBytes());
                if (e.getName().equals("xl/worksheets/sheet1.xml")) {
                    byte[] espacos = new byte[1024 * 1024];
                    java.util.Arrays.fill(espacos, (byte) ' ');
                    for (int i = 0; i < 30; i++) {
                        out.write(espacos);
                    }
                }
                out.closeEntry();
            }
        }

        assertThatThrownBy(() -> leitor.ler("lote.xlsx", bomba.toByteArray()))
                .isInstanceOf(ArquivoInvalidoException.class)
                .hasMessageContaining("descompactado")
                .hasMessageNotContaining("Zip bomb");
    }

    @Test
    void xlsx_streamingParaNaPrimeiraLinhaAcimaDoLimite() {
        // cabeçalho + MAX_LINHAS de dados passam; a próxima linha não vazia interrompe a leitura ali mesmo
        LeitorPlanilha.ColetorDeLinhas coletor = new LeitorPlanilha.ColetorDeLinhas();
        for (int i = 0; i <= LeitorPlanilha.MAX_LINHAS; i++) {
            coletor.startRow(i);
            coletor.cell("A" + (i + 1), "x", null);
            coletor.endRow(i);
        }
        coletor.startRow(LeitorPlanilha.MAX_LINHAS + 1);
        coletor.cell(null, "x", null);

        assertThatThrownBy(() -> coletor.endRow(LeitorPlanilha.MAX_LINHAS + 1))
                .isInstanceOf(LeitorPlanilha.AcimaDoLimite.class);
        assertThat(coletor.linhas).hasSize(LeitorPlanilha.MAX_LINHAS + 2);
    }
}
