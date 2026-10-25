# Importação em massa de serviços — Plano de implementação

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** O comprador envia uma planilha (CSV/.xlsx) de serviços; cada linha é classificada com o filtro do cadastro, o comprador confere e envia, e o aprovador decide; nada entra na base sem aprovação.

**Architecture:** Três tabelas novas (lote, linha do lote, sinônimo de serviço). Um serviço de importação lê o arquivo e classifica cada linha em segundo plano (`@Async`, progresso no banco), reaproveitando as regras do `ServicoService`. Serviços de conferência e de aprovação mudam o estado das linhas; só a aprovação grava em `servicos_mock` ou `servico_sinonimo`. Telas Thymeleaf + JS puro, sobre o login com perfis.

**Tech Stack:** Spring Boot 4.1.1 / Java 21, JPA + Flyway (migration `V10`), PostgreSQL `pg_trgm`, Thymeleaf, JS puro, Apache POI (`poi-ooxml`) para .xlsx e Apache Commons CSV para .csv (versões estáveis com mais de 2 semanas; conferir que resolvem), JUnit 5 + Mockito + MockMvc.

**Spec:** `docs/superpowers/specs/2026-10-24-importacao-em-massa-design.md` (depende de `docs/superpowers/specs/2026-10-24-login-perfis-design.md`, já implementado em `feat/login-perfis`)

## Global Constraints

- Pacote base `com.Gerdau.Desafio_GC_Gerdau_App`; código, mensagens e nomes de domínio em português.
- Migration nova `V10__importacao_em_massa.sql`; nunca editar V1–V9.
- Limiares do cadastro: 0,4 (parecido) e 0,8 (bloqueio, com mesma condição e CNAE), código com no máximo 40 caracteres. **Reusar** `ServicoService.LIMIAR_SUGESTAO`, `LIMIAR_BLOQUEIO` e `TAMANHO_MAX_CODIGO`; os novos serviços ficam no pacote `service` para acessá-los.
- Uma linha **parecida** inclui a que o cadastro bloquearia por texto (≥ 0,8, mesma condição e CNAE). Só código gerado **idêntico** a um existente, ou sinônimo aprovado, é "já existia".
- Nada entra em `servicos_mock` ou `servico_sinonimo` antes da aprovação.
- Segurança do login: aprovação só `APROVADOR`; `POST` com CSRF; o comprador só acessa os próprios lotes, o aprovador todos os enviados; testes `@WebMvcTest` usam `@Import(SecurityConfig.class)`, `@WithMockUser` e `.with(csrf())`.
- Formato do arquivo: CSV UTF-8 (`;` ou `,`) ou .xlsx (1ª planilha); colunas `area_servico`, `texto_breve`, `abreviatura`, `complemento` (opcional), `condicao` (descrição ou sufixo de `cat_horarios`), `cnae`, `codigo_nbs` (opcional), `criador_codigo` (opcional); cabeçalho obrigatório, sem diferenciar maiúsculas nem acentos.
- Rodar com JDK 21 (`JAVA_HOME=$HOME/.jdks/ms-21.0.11`). Atualizar `README.md`, `docs/GUIA_DE_USO.md`, `docs/REGRAS_DE_NEGOCIO.md` e `CLAUDE.md` ("V1–V9" passa a "V1–V10").

## Review Focus

- Arquivo sem cabeçalho obrigatório, vazio, só com cabeçalho, binário disfarçado de .csv ou acima do limite: recusa clara, nenhum lote criado.
- Células vazias, espaços nas pontas, CNAE numérico do Excel (`4399103.0` ou notação científica), texto com acento/aspas/`;` dentro do campo: a linha é lida certo ou cai em ERRO com motivo, nunca estoura o lote inteiro.
- Linha repetida no próprio arquivo ou já enviada em outro lote: sinalizada, não vira segundo código novo.
- Dois aprovadores (ou aprovador e cadastro individual) criando o mesmo código ao mesmo tempo: a linha vira CONFLITO, nada se perde nem se grava em duplicidade.
- Falha no meio do processamento (erro numa linha, reinício do app): o lote termina `FALHOU` ou `CONCLUIDO` com as linhas boas já salvas, e nunca fica `PROCESSANDO` para sempre.
- Comprador abrindo o lote de outro comprador, ou chamando a aprovação: 403/404.
- Aprovação em bloco sem filtro nenhum não aprova linhas que não foram enviadas.

---

## File Structure

- Create `src/main/resources/db/migration/V10__importacao_em_massa.sql`.
- Create em `model/`: `LoteImportacao`, `LinhaImportacao`, `SinonimoServico`, enums `StatusLote`, `SituacaoLinha`, `EtapaLinha`, `DecisaoParecida`.
- Create em `repository/`: `LoteImportacaoRepository`, `LinhaImportacaoRepository`, `SinonimoServicoRepository`.
- Create em `service/`: `LeitorPlanilha`, `SinonimoServicoService`, `ClassificadorLinha`, `ImportacaoService`, `ConferenciaService`, `AprovacaoImportacaoService`.
- Create em `dto/`: `LinhaPlanilha`, `ResultadoClassificacao`, `ResumoLote`.
- Create em `controller/`: `ImportacaoController` (REST `/api/importacoes`), `ImportacaoViewController` (telas `/ui/importacoes`).
- Create templates `importacoes.html`, `importacao-lote.html`, `aprovacao-lotes.html`, `aprovacao-lote.html`; JS `importacao.js`, `aprovacao.js`; CSS `importacao.css`.
- Modify `ServicoService`, `AnaliseSimilaridade`, `SecurityConfig`, `pom.xml`, `application.properties`, `layout`/cabeçalhos (link "Importar" e "Aprovações").

---

### Task 1: Schema e entidades

**Files:** Create `V10__importacao_em_massa.sql`, as entidades, enums e repositories da File Structure.

**Interfaces — Produces:**
- `enum StatusLote { PROCESSANDO, CONCLUIDO, FALHOU }`
- `enum SituacaoLinha { ERRO, EXISTENTE, REPETIDA, PARECIDA, NOVA }`
- `enum EtapaLinha { EM_CONFERENCIA, PRONTA, ENVIADA, APROVADA, REJEITADA, CONFLITO, IGNORADA, DESCARTADA }`
- `enum DecisaoParecida { MESMO_SERVICO, OUTRO_SERVICO }`
- `LoteImportacao`: `id` (Long), `arquivoNome`, `enviadoPor`, `criadoEm`, `status` (`StatusLote`), `totalLinhas`, `processadas`, `mensagemErro`.
- `LinhaImportacao`: `id`, `lote` (ManyToOne), `numero` (linha na planilha), campos de entrada (`areaServico`, `textoBreve`, `abreviatura`, `complemento`, `condicao`, `cnae`, `codigoNbs`, `criadorCodigo`), `codigoGerado`, `situacao`, `etapa`, `servicoParecidoId` (Integer), `score` (Double), `motivo`, `decisao` (`DecisaoParecida`), `conferidoPor`, `enviadoEm`, `decididoPor`, `decididoEm`, `servicoCriadoId` (Integer).
- `SinonimoServico`: `id`, `textoNormalizado`, `condicao` (descrição, como em `servicos_mock.id1_condicao`), `cnae`, `servico` (id `Integer`, FK para `servicos_mock`), `textoOriginal`, `aprovadoPor`, `aprovadoEm`, `linhaId`. Restrição única `(texto_normalizado, condicao, cnae)`. Só sinônimos **aprovados** são gravados; o estado "pendente" é a linha com etapa `ENVIADA` (representação equivalente ao ciclo do spec).
- Repositories: `LinhaImportacaoRepository.findByLoteIdOrderByNumero(Long)`, `findByLoteIdAndEtapa(Long, EtapaLinha)`, `existsByCodigoGeradoAndEtapaIn(String, Collection<EtapaLinha>)`; `SinonimoServicoRepository.findByTextoNormalizadoAndCondicaoAndCnae(String, String, String)`.

- [ ] **Step 1:** Escrever a migration e as entidades (Lombok `@Getter @Setter @NoArgsConstructor` como as existentes; enums com `@Enumerated(EnumType.STRING)`; índices em `linhas_importacao(lote_id)` e `(codigo_gerado)`).
- [ ] **Step 2: Verificar** que o `ddl-auto=validate` aceita o schema: `./mvnw test -Dtest=DesafioGcGerdauAppApplicationTests` com o Postgres do compose no ar. Expected: PASS (migration V10 aplicada, contexto sobe). Entidades sem lógica não têm teste próprio; o contexto é a prova.
- [ ] **Step 3: Commit** `feat(importacao): schema de lotes, linhas e sinonimos`

---

### Task 2: Leitura de CSV e Excel

**Files:** Modify `pom.xml`, `application.properties` (`spring.servlet.multipart.max-file-size` e `max-request-size`, valor compatível com milhares de linhas, documentado); Create `dto/LinhaPlanilha.java`, `service/LeitorPlanilha.java`, `exception/ArquivoInvalidoException.java`; Test `service/LeitorPlanilhaTest.java`.

**Interfaces — Produces:**
- `record LinhaPlanilha(int numero, String areaServico, String textoBreve, String abreviatura, String complemento, String condicao, String cnae, String codigoNbs, String criadorCodigo)` (campos já com `trim`; vazio vira `null`).
- `LeitorPlanilha.ler(String nomeArquivo, byte[] conteudo): List<LinhaPlanilha>`; constante `LeitorPlanilha.MAX_LINHAS`. Lança `ArquivoInvalidoException(String mensagem)` (extends `RuntimeException`) para: extensão que não seja `.csv`/`.xlsx`, arquivo vazio, sem as colunas obrigatórias (a mensagem lista as que faltam), sem linhas de dados, ou acima de `MAX_LINHAS`.

- [ ] **Step 1: Write the failing tests** (arquivos montados em memória; .xlsx gerado com POI no próprio teste): `csv_comPontoEVirgula`, `csv_comVirgulaEAspas` (campo `"Solda, TIG"`), `csv_comAcentosUtf8`, `xlsx_primeiraPlanilha`, `cabecalho_ignoraCaixaEAcento` (`Área_Serviço`/`AREA_SERVICO`), `colunasOpcionaisAusentes_viramNull`, `cnaeNumericoDoExcel_semPontoZero` (célula numérica `4399103` lida como `"4399103"`), `numeroDaLinha_eADaPlanilha` (primeira linha de dados = 2), `semColunaObrigatoria_mensagemListaAsFaltantes`, `arquivoVazio_recusa`, `soCabecalho_recusa`, `extensaoInvalida_recusa`, `acimaDoLimite_recusa`, `linhaTotalmenteEmBranco_eIgnorada`.
- [ ] **Step 2: Run** `./mvnw test -Dtest=LeitorPlanilhaTest` — Expected: FAIL (classes não existem).
- [ ] **Step 3: Implement** `LeitorPlanilha` (`@Component`): detecta o delimitador pela primeira linha; mapeia cabeçalhos normalizados (reusar a normalização de acento/caixa de `DeParaService.normalizar` quando servir); lê células Excel como texto formatado sem casas decimais para inteiros.
- [ ] **Step 4: Run** — Expected: PASS.
- [ ] **Step 5: Commit** `feat(importacao): leitura de CSV e Excel`

---

### Task 3: Sinônimos aprovados bloqueiam o cadastro individual

**Files:** Create `service/SinonimoServicoService.java`; Modify `ServicoService.java`, `dto/AnaliseSimilaridade.java`, `templates/servico-form.html` e/ou `static/js/form.js` (mostrar o código existente); Test `SinonimoServicoServiceTest`, `ServicoServiceTest`.

**Interfaces:**
- Consumes: `SinonimoServicoRepository` (Task 1), `DeParaService.normalizar(String)`.
- Produces: `SinonimoServicoService.reconhecer(String texto, String condicaoDescricao, String cnae): Optional<Servico>` (chave = texto normalizado + condição + CNAE); `SinonimoServicoService.registrar(String texto, String condicaoDescricao, String cnae, Servico servico, String aprovadoPor, Long linhaId): SinonimoServico`; `AnaliseSimilaridade` ganha o componente `String reconhecidoComo` (código padronizado do serviço ligado, ou `null`); `ServicoService` passa a receber `SinonimoServicoService` no construtor.

- [ ] **Step 1: Write the failing tests.** `SinonimoServicoServiceTest`: `reconhece_textoComAcentoECaixaDiferentes` ("Manut. MOTOR elétrico" registrado, "manut. motor eletrico" reconhecido), `naoReconhece_seCondicaoOuCnaeDiferem`, `registrar_gravaTextoNormalizado`. `ServicoServiceTest`: `analisar_sinonimoAprovado_bloqueiaEInformaOCodigoExistente` (`bloqueado == true`, `reconhecidoComo == "CONC_CONCRE_HN4399103"`) e `cadastrar_sinonimoAprovado_lancaCadastroRejeitado`. Ajustar os usos existentes do construtor de `AnaliseSimilaridade`.
- [ ] **Step 2: Run** `./mvnw test -Dtest='SinonimoServicoServiceTest,ServicoServiceTest'` — Expected: FAIL.
- [ ] **Step 3: Implement.** Em `analisar`, consultar `reconhecer` antes da similaridade; se achar, `bloqueado = true` e `reconhecidoComo` preenchido. No formulário, exibir "Este texto já é o serviço <código>" quando `reconhecidoComo != null`.
- [ ] **Step 4: Run** a suíte `./mvnw test -Dtest='!DesafioGcGerdauAppApplicationTests'` — Expected: PASS.
- [ ] **Step 5: Commit** `feat(importacao): sinonimo aprovado bloqueia o cadastro individual`

---

### Task 4: Classificar uma linha

**Files:** Create `dto/ResultadoClassificacao.java`, `service/ClassificadorLinha.java`; Test `service/ClassificadorLinhaTest.java` (Mockito).

**Interfaces:**
- Consumes: `LinhaPlanilha` (Task 2), `ServicoService` (`gerarCodigo`, e a busca de similares), `SinonimoServicoService.reconhecer`, `CatHorarioRepository`, `CatNbsRepository`.
- Produces: `record ResultadoClassificacao(SituacaoLinha situacao, String codigoGerado, Integer servicoParecidoId, Double score, String motivo)`; `ClassificadorLinha.classificar(LinhaPlanilha linha, Set<String> codigosJaVistos): ResultadoClassificacao`. Ordem das regras: (1) campos obrigatórios, formato do CNAE (7 dígitos), abreviatura/complemento válidos como em `NovoServicoForm`, condição aceita por descrição **ou** sufixo, NBS existente → `ERRO` com `motivo`; (2) código acima de 40 caracteres → `ERRO`; (3) código já em `codigosJaVistos` → `REPETIDA`; (4) sinônimo aprovado → `EXISTENTE`; (5) código idêntico a serviço existente → `EXISTENTE`; (6) algum serviço com similaridade ≥ `LIMIAR_SUGESTAO` → `PARECIDA` (com o mais parecido em `servicoParecidoId`/`score`, inclusive os que o cadastro bloquearia); (7) senão `NOVA`. Nunca lança exceção por dado ruim.

- [ ] **Step 1: Write the failing tests** (um por regra, com valores do spec): `condicaoInexistente_viraErro` ("hora extra 50%"), `cnaeInvalido_viraErro`, `codigoCom41Caracteres_viraErro`, `condicaoPorDescricao_eAceita` ("Hora normal" → sufixo `HN`), `codigoIgualAExistente_eExistente`, `sinonimoAprovado_eExistente`, `codigoJaVistoNoLote_eRepetida`, `similaridade085MesmaCondicaoECnae_eParecidaNaoExistente`, `similaridade045_eParecida`, `similaridade039_eNova`, `semSimilares_eNova`, `camposEmBranco_viraErroSemExcecao`.
- [ ] **Step 2: Run** `./mvnw test -Dtest=ClassificadorLinhaTest` — Expected: FAIL.
- [ ] **Step 3: Implement** reaproveitando `ServicoService` (expor o necessário com visibilidade de pacote; não copiar limiares nem a montagem do código).
- [ ] **Step 4: Run** — Expected: PASS.
- [ ] **Step 5: Commit** `feat(importacao): classificacao de linha`

---

### Task 5: Importar em segundo plano com progresso

**Files:** Create `service/ImportacaoService.java`, `config/AsyncConfig.java` (`@EnableAsync`, executor próprio); Test `ImportacaoServiceTest`.

**Interfaces:**
- Consumes: `LeitorPlanilha.ler`, `ClassificadorLinha.classificar`, repositories (Task 1).
- Produces: `ImportacaoService.iniciar(String nomeArquivo, byte[] conteudo, String usuario): Long` (valida e lê o arquivo na hora, cria o `LoteImportacao` em `PROCESSANDO` e dispara `processar`; lançar `ArquivoInvalidoException` **antes** de criar o lote); `@Async void processar(Long loteId, List<LinhaPlanilha> linhas)`. Etapa inicial por situação: `ERRO` e `PARECIDA` → `EM_CONFERENCIA`; `NOVA` → `PRONTA`; `EXISTENTE` e `REPETIDA` → `IGNORADA`. `criadorCodigo` em branco assume o usuário. Persistir em lotes (ex.: 200 linhas) atualizando `processadas`; fim OK → `CONCLUIDO`; exceção → `FALHOU` com `mensagemErro`. Também `resumo(Long loteId): ResumoLote` com `record ResumoLote(Long id, String arquivo, StatusLote status, int total, int processadas, Map<SituacaoLinha, Long> porSituacao, Map<EtapaLinha, Long> porEtapa)`. Na subida da aplicação, lotes que ficaram `PROCESSANDO` viram `FALHOU` (`@EventListener(ApplicationReadyEvent)`).

- [ ] **Step 1: Write the failing tests** (Mockito, `processar` chamado direto, sem async): `processar_classificaECriaLinhasComEtapaInicial`, `processar_atualizaProgresso`, `processar_repeteCodigoNoMesmoArquivo` (2ª linha `REPETIDA`/`IGNORADA`), `processar_codigoJaPendenteEmOutroLote_eRepetida`, `processar_erroInesperado_marcaLoteFalhou`, `iniciar_arquivoInvalido_naoCriaLote`, `iniciar_criadorEmBrancoAssumeUsuario`, `aoSubir_lotesProcessandoViramFalhou`, `resumo_contaPorSituacaoEEtapa`.
- [ ] **Step 2: Run** `./mvnw test -Dtest=ImportacaoServiceTest` — Expected: FAIL.
- [ ] **Step 3: Implement.** O `processar` assíncrono recebe o `loteId` e as linhas já lidas; uma exceção numa linha não pode derrubar as já salvas.
- [ ] **Step 4: Run** — Expected: PASS.
- [ ] **Step 5: Commit** `feat(importacao): processamento do lote em segundo plano`

---

### Task 6: Conferência do comprador

**Files:** Create `service/ConferenciaService.java`; Test `ConferenciaServiceTest`.

**Interfaces:**
- Consumes: `LinhaImportacao`, `ClassificadorLinha` (para reclassificar), repositories.
- Produces (todas conferem que o lote pertence ao `usuario`, senão `AccessDeniedException`; só mexem em linhas `EM_CONFERENCIA`/`PRONTA`):
  - `corrigir(Long linhaId, LinhaPlanilha novosDados, String usuario): LinhaImportacao` — regrava os campos, reclassifica (`codigosJaVistos` = códigos das outras linhas do lote) e reaplica a etapa inicial.
  - `decidir(Long linhaId, DecisaoParecida decisao, String usuario)` — só em `PARECIDA`; passa a `PRONTA`.
  - `decidirEmBloco(Long loteId, double scoreMinimo, DecisaoParecida decisao, String usuario): int` — aplica às `PARECIDA` ainda sem decisão com `score >= scoreMinimo`; devolve quantas.
  - `descartar(Long linhaId, String usuario)` — etapa `DESCARTADA`.
  - `enviarAoAprovador(Long loteId, String usuario): int` — passa `PRONTA` para `ENVIADA` (grava `enviadoEm`); `ERRO`/`EM_CONFERENCIA` ficam; devolve quantas.

- [ ] **Step 1: Write the failing tests:** `corrigir_erroDeCondicao_reclassificaParaNova`, `corrigir_continuaComErro_mantemEmConferencia`, `decidir_somenteEmParecida`, `decidirEmBloco_respeitaScoreMinimo`, `enviar_soPronta` (ERRO e PARECIDA sem decisão não vão), `enviar_duasVezes_naoReenvia`, `outroUsuario_recebeAccessDenied`, `descartar_naoVaiNoEnvio`.
- [ ] **Step 2: Run** `./mvnw test -Dtest=ConferenciaServiceTest` — Expected: FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** — Expected: PASS.
- [ ] **Step 5: Commit** `feat(importacao): conferencia e envio ao aprovador`

---

### Task 7: Aprovação

**Files:** Create `service/AprovacaoImportacaoService.java`; Test `AprovacaoImportacaoServiceTest`.

**Interfaces:**
- Consumes: linhas `ENVIADA`, `SinonimoServicoService.registrar`, `ServicoRepository`, `CatHorarioRepository`.
- Produces:
  - `aprovar(Long linhaId, String aprovador): EtapaLinha` — `NOVA` e `PARECIDA`+`OUTRO_SERVICO`: revalida que o código ainda é livre e cria o `Servico` (campos como em `ServicoService.cadastrar`); código ocupado ou `DataIntegrityViolationException` → etapa `CONFLITO` com `motivo`, sem gravar. `PARECIDA`+`MESMO_SERVICO`: registra o sinônimo; chave já ligada a outro serviço → `CONFLITO`. Sucesso → `APROVADA` com `decididoPor`/`decididoEm`/`servicoCriadoId`.
  - `rejeitar(Long linhaId, String aprovador, String motivo)` — motivo obrigatório; etapa `REJEITADA`.
  - `aprovarEmBloco(Long loteId, FiltroAprovacao filtro, String aprovador): ResumoAprovacao` e `rejeitarEmBloco(...)`, onde `record FiltroAprovacao(Set<SituacaoLinha> situacoes, Double scoreMinimo)`; só atua em linhas `ENVIADA`; filtro vazio **não** aprova nada além de `ENVIADA`; `record ResumoAprovacao(int aprovadas, int conflitos)`.
  - Cada linha é processada em transação própria (um conflito não desfaz as demais).

- [ ] **Step 1: Write the failing tests:** `aprovarNova_criaServico`, `aprovarNova_codigoOcupado_viraConflitoSemGravar`, `aprovarNova_violacaoDeUnicidade_viraConflito`, `aprovarMesmoServico_registraSinonimo`, `aprovarMesmoServico_chaveLigadaAOutroServico_viraConflito`, `aprovarOutroServico_criaServico`, `rejeitar_exigeMotivo`, `emBloco_soLinhasEnviadas`, `emBloco_comScoreMinimoFiltraParecidas`, `emBloco_umConflitoNaoDesfazOutras`, `aprovar_linhaNaoEnviada_recusa`.
- [ ] **Step 2: Run** `./mvnw test -Dtest=AprovacaoImportacaoServiceTest` — Expected: FAIL.
- [ ] **Step 3: Implement** (`@Transactional(propagation = REQUIRES_NEW)` por linha, ou equivalente que isole cada linha).
- [ ] **Step 4: Run** — Expected: PASS.
- [ ] **Step 5: Commit** `feat(importacao): aprovacao em linha e em bloco`

---

### Task 8: API REST e regras de acesso

**Files:** Create `controller/ImportacaoController.java`; Modify `config/SecurityConfig.java`; Test `ImportacaoControllerTest` (`@WebMvcTest`).

**Interfaces — Produces:** sob `/api/importacoes` (JSON; `principal.getName()` é sempre o usuário):
- `POST /` (multipart `arquivo`) → `201 {id}`; `ArquivoInvalidoException` → `400 {erro}`.
- `GET /{id}` → `ResumoLote`; `GET /{id}/linhas?situacao=&etapa=` → linhas.
- `PUT /linhas/{id}` (corrigir), `POST /linhas/{id}/decisao`, `POST /{id}/decisao-em-bloco`, `POST /linhas/{id}/descartar`, `POST /{id}/enviar` → `{enviadas}`.
- Só `APROVADOR`, sob `/api/importacoes/aprovacao`: `GET /lotes` (lotes com linhas `ENVIADA`), `POST /linhas/{id}/aprovar`, `POST /linhas/{id}/rejeitar`, `POST /{id}/aprovar-em-bloco`, `POST /{id}/rejeitar-em-bloco`.
- Posse do lote: o comprador só enxerga os próprios (404 para os de outros); o aprovador enxerga os que têm linhas enviadas. `AccessDeniedException` do serviço → `403`.

- [ ] **Step 1: Write the failing tests:** `semLogin_401`; `comprador_enviaArquivo_201`; `arquivoInvalido_400ComMensagem`; `comprador_naoChamaAprovacao_403`; `aprovador_aprovaEmBloco_200`; `comprador_loteDeOutro_404`; `postSemCsrf_403`.
- [ ] **Step 2: Run** `./mvnw test -Dtest=ImportacaoControllerTest` — Expected: FAIL.
- [ ] **Step 3: Implement** o controller e, em `SecurityConfig`, `/api/importacoes/aprovacao/**` e `/ui/importacoes/aprovacao/**` só `APROVADOR`, o restante de `/api/importacoes/**` e `/ui/importacoes/**` para `COMPRADOR` ou `APROVADOR` (regra mais específica antes da geral).
- [ ] **Step 4: Run** a suíte `!DesafioGcGerdauAppApplicationTests` — Expected: PASS.
- [ ] **Step 5: Commit** `feat(importacao): API e acesso por perfil`

---

### Task 9: Telas do comprador

**Files:** Create `ImportacaoViewController`, `importacoes.html` (enviar arquivo + lista dos próprios lotes), `importacao-lote.html` (progresso e os quatro blocos do mockup B: com erro, parecidas, novas, já existiam), `importacao.js`, `importacao.css`; Modify os cabeçalhos (link "Importar"); Test `ImportacaoViewControllerTest`.

**Interfaces:** consome `/api/importacoes` (Task 8). Comportamento: upload com `fetch` + `X-CSRF-TOKEN` das `<meta>`; barra de progresso consultando `GET /{id}` até `CONCLUIDO`/`FALHOU`; blocos "com erro" e "parecidas" paginados/filtráveis (milhares de linhas não podem virar milhares de nós na tela de uma vez); bloco "parecidas" mostra o serviço parecido, a similaridade e as ações "é o mesmo serviço", "é outro serviço", "descartar", mais decisão em bloco por similaridade mínima; erro: formulário de correção por linha; "Enviar ao aprovador" envia o que está `PRONTA` (não fica bloqueado por linhas com erro); "novas" e "já existiam" recolhidos.

- [ ] **Step 1: Write the failing tests** (MockMvc): `listaDeLotes_soOsDoUsuario`, `lote_mostraOsQuatroBlocosComContagens`, `lote_deOutroUsuario_404`, `semLogin_redirecionaParaLogin`, `cabecalho_mostraLinkImportar`.
- [ ] **Step 2: Run** `./mvnw test -Dtest=ImportacaoViewControllerTest` — Expected: FAIL.
- [ ] **Step 3: Implement** as telas no estilo de `layout.html`/`style.css` (JS puro; texto sempre inserido com `textContent`, nunca `innerHTML` com dados da planilha).
- [ ] **Step 4: Run** a suíte — Expected: PASS.
- [ ] **Step 5: Commit** `feat(importacao): telas do comprador`

---

### Task 10: Tela do aprovador

**Files:** Create `aprovacao-lotes.html` (lotes com linhas enviadas, contagens), `aprovacao-lote.html` (linhas enviadas com filtro por tipo e similaridade mínima, aprovar/rejeitar uma ou em bloco, mostrando antes de confirmar quantas linhas serão afetadas), `aprovacao.js`; Modify o cabeçalho (link "Aprovações" só para o aprovador); Test `ImportacaoViewControllerTest` (casos do aprovador).

- [ ] **Step 1: Write the failing tests:** `aprovador_veListaDeLotesEnviados`, `comprador_naoAcessaAprovacao_403`, `linkAprovacoes_soParaAprovador`, `ecraMostraLinhasEmConflitoComOMotivo`.
- [ ] **Step 2: Run** `./mvnw test -Dtest=ImportacaoViewControllerTest` — Expected: FAIL.
- [ ] **Step 3: Implement.** A ação em bloco pede confirmação com a contagem; linhas em `CONFLITO` aparecem com o motivo e ficam fora de novas ações em bloco.
- [ ] **Step 4: Run** a suíte — Expected: PASS.
- [ ] **Step 5: Commit** `feat(importacao): telas do aprovador`

---

### Task 11: Documentação, desempenho e verificação no app real

**Files:** Modify `README.md` (migration V10, telas, endpoints, formato do arquivo), `docs/GUIA_DE_USO.md` (passo a passo do comprador e do aprovador, colunas, exemplo de CSV), `docs/REGRAS_DE_NEGOCIO.md` (as quatro situações, sinônimos, "parecida" inclui o que o cadastro bloquearia), `CLAUDE.md` ("V1–V10" e um parágrafo da importação).

- [ ] **Step 1:** Atualizar a documentação, incluindo um CSV de exemplo curto.
- [ ] **Step 2: Carga:** gerar um CSV de 5.000 linhas (mistura de novas, parecidas, repetidas, com erro e existentes), importar pelo app e registrar o tempo até `CONCLUIDO`. Expected: termina em minutos, não horas; se não, otimizar a comparação (lotes de consulta, índice) e repetir. Relatar o tempo medido.
- [ ] **Step 3: Verificação ao vivo** (app + Postgres): importar como `comprador`; ver os quatro blocos; corrigir uma linha com erro; decidir parecidas; enviar; comprador recebe 403 em `/ui/importacoes/aprovacao`; como `aprovador`, aprovar em bloco; confirmar no banco serviços novos e sinônimos; tentar cadastrar no formulário o texto de um sinônimo aprovado e ver o bloqueio com o código existente. Não apagar dados do banco do usuário sem pedir.
- [ ] **Step 4:** `./mvnw test` completo com o Postgres no ar — Expected: tudo passa.
- [ ] **Step 5: Commit** `docs(importacao): guia, regras e verificacao`
