# Registro de consultas e botão "Usar este código" Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Registrar cada tentativa de cadastro individual (o que foi consultado, o que o sistema sugeriu, o que o usuário decidiu) e permitir "usar o código existente" a partir das sugestões, com uma tela para o aprovador conferir o histórico.

**Architecture:** Migration V12 cria `consultas_cadastro` e `consulta_cadastro_sugestoes`. `ServicoService.cadastrar` (usado pela tela e pela API REST) grava o registro via um bean novo, `RegistroConsultaService`, sem nunca atrapalhar o cadastro. O botão "Usar este código" é um `<button formaction>` dentro do formulário existente que posta para `POST /ui/servicos/reutilizar`. Duas telas novas, só para `APROVADOR`: `/ui/consultas` e `/ui/consultas/{id}`.

**Tech Stack:** Java 21, Spring Boot 4.1, Spring Data JPA, Flyway, Thymeleaf, JS puro, JUnit 5/Mockito, `@WebMvcTest`.

**Spec:** Sem documento separado. O desenho foi aprovado no chat em 2026-10-28 e está resumido na seção "Desenho aprovado" abaixo. A banca pede "Registro da decisão" em `desafio_gc_2026_gerdau.md`.

## Desenho aprovado

- Uma linha por tentativa de cadastro: quando, quem (usuário logado, nunca campo do formulário), texto/condição/CNAE consultados, código gerado, sugestões mostradas (código, texto, pontuação), resultado do sistema, decisão do usuário e serviço resultante.
- Bloqueios e pedidos de confirmação também são gravados.
- Botão "Usar este código" em cada sugestão (a lista ao digitar e a lista "Parecidos encontrados ao cadastrar"). O clique grava a decisão `REUTILIZOU_EXISTENTE` e leva ao detalhe do serviço escolhido.
- Tela `/ui/consultas` (lista) e `/ui/consultas/{id}` (detalhe), só para o aprovador.
- A importação em massa fica de fora (já tem rastro próprio).

**Mapeamento resultado × decisão em `cadastrar`** (decidido por mim, marcado como minha escolha):

| Situação | `resultado` | `decisao` | `servico_id` |
|---|---|---|---|
| duplicado (bloqueado, inclui corrida de unicidade e sinônimo aprovado) | `BLOQUEADO` | `null` | `null` |
| parecidos e sem `confirmar` | `REQUER_CONFIRMACAO` | `null` | `null` |
| cadastrado sem parecidos | `CADASTRADO` | `CADASTROU_NOVO` | id criado |
| cadastrado com parecidos e `confirmar=true` | `CADASTRADO` | `CONFIRMOU_MESMO_ASSIM` | id criado |
| clique em "Usar este código" | `SUGERIDO` | `REUTILIZOU_EXISTENTE` | id escolhido |

Erros de validação (`IllegalArgumentException`: código acima de 40, NBS ou condição desconhecidos) **não** geram registro.

## Global Constraints

- `ddl-auto=validate`: toda tabela nova precisa de migration; nunca editar V1 a V11. A nova é `V12__consultas_cadastro.sql`.
- `open-in-view=false`: quem lê entidade preguiçosa fora do serviço precisa de `@EntityGraph`.
- O usuário vem do `Authentication`/`Principal` no controller e é passado ao serviço; nunca de corpo ou formulário.
- Testes `@WebMvcTest`: `@Import(SecurityConfig.class)`, `@WithMockUser` e `.with(csrf())` nos `POST`.
- Código, nomes e textos em português; seguir o estilo dos vizinhos (Lombok `@Getter/@Setter/@NoArgsConstructor` nas entidades, `th:text` sempre, JS sem `innerHTML`).
- Commits só quando o usuário pedir (regra do projeto); os passos abaixo terminam em teste verde, sem commit.
- Manter `README.md` e `docs/` em dia (Tarefa 6).

## Review Focus

- A recusa (`BLOQUEADO`/`REQUER_CONFIRMACAO`) é gravada mesmo com a exceção sendo lançada logo em seguida (Tarefa 3).
- Falha ao gravar o registro não impede o cadastro nem muda a resposta (Tarefa 3).
- "Usar este código" com o formulário incompleto (campos obrigatórios vazios) ainda funciona, e com `servicoId` inexistente volta com mensagem, sem erro 500 (Tarefa 4).
- Texto digitado com `<`, `&` e aspas aparece escapado nas telas de consulta (Tarefa 5).
- Comprador não acessa `/ui/consultas` nem `/ui/consultas/{id}`; id inexistente dá 404 (Tarefa 5).

---

### Task 1: Tabelas, entidades e repositório

**Files:**
- Create: `src/main/resources/db/migration/V12__consultas_cadastro.sql`
- Create: `src/main/java/com/Gerdau/Desafio_GC_Gerdau_App/model/ResultadoConsulta.java`, `DecisaoConsulta.java`, `ConsultaCadastro.java`, `SugestaoConsulta.java`
- Create: `src/main/java/com/Gerdau/Desafio_GC_Gerdau_App/repository/ConsultaCadastroRepository.java`
- Test: `src/test/java/com/Gerdau/Desafio_GC_Gerdau_App/repository/ConsultaCadastroRepositoryTest.java`

**Interfaces:**
- Produces:
  - `enum ResultadoConsulta { BLOQUEADO, REQUER_CONFIRMACAO, CADASTRADO, SUGERIDO }`
  - `enum DecisaoConsulta { CADASTROU_NOVO, CONFIRMOU_MESMO_ASSIM, REUTILIZOU_EXISTENTE }`
  - `ConsultaCadastro` (Lombok getters/setters): `Long id`, `LocalDateTime criadoEm` (`insertable=false, updatable=false`), `String usuario`, `String textoBreve`, `String condicao` (anulável), `String cnae` (anulável), `String codigoGerado` (anulável), `ResultadoConsulta resultado`, `DecisaoConsulta decisao` (anulável), `Integer servicoId` (anulável), `List<SugestaoConsulta> sugestoes` (`@OneToMany(mappedBy="consulta", cascade=ALL)`, `@OrderBy("score DESC")`, inicializada com `new ArrayList<>()`).
  - `SugestaoConsulta`: `Long id`, `ConsultaCadastro consulta` (`@ManyToOne(fetch=LAZY)`), `Integer servicoId`, `String codigoPadronizado`, `String textoBreve`, `double score`.
  - `ConsultaCadastroRepository extends JpaRepository<ConsultaCadastro, Long>`: `List<ConsultaCadastro> findTop200ByOrderByCriadoEmDesc()` e `@EntityGraph(attributePaths = "sugestoes") Optional<ConsultaCadastro> findComSugestoesById(Long id)`.

- [ ] **Step 1: Write the failing test** `ConsultaCadastroRepositoryTest` (`@SpringBootTest @Transactional`, no estilo de `SinonimoServicoRepositoryTest`; precisa do Postgres do compose). Testes:
  - `salvaConsultaComSugestoesEBuscaComSugestoesCarregadas`: salva uma `ConsultaCadastro` (usuario `"teste"`, `BLOQUEADO`) com 2 `SugestaoConsulta` de scores 0.5 e 0.9, faz `flush`/`clear` do `EntityManager`; `findComSugestoesById` devolve a consulta, `PersistenceUnitUtil.isLoaded(c, "sugestoes")` é verdadeiro e a primeira sugestão tem score 0.9 (ordem `DESC`).
  - `listaAsMaisRecentesPrimeiro`: salva duas consultas e confirma que `findTop200ByOrderByCriadoEmDesc()` devolve a gravada por último antes da anterior (use `EntityManager` para ajustar `criado_em` por SQL nativo se os instantes coincidirem).
  - `rejeitaResultadoForaDoCatalogo`: `INSERT` nativo com `resultado = 'XYZ'` lança `DataIntegrityViolationException`/`PersistenceException`.

- [ ] **Step 2: Run to verify it fails.** `./mvnw test -Dtest=ConsultaCadastroRepositoryTest`. Esperado: erro de compilação (tipos não existem).

- [ ] **Step 3: Create `V12__consultas_cadastro.sql`.** Tabela `consultas_cadastro` (`id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY`, `criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`, `usuario VARCHAR(100) NOT NULL`, `texto_breve VARCHAR(255) NOT NULL`, `condicao VARCHAR(50)`, `cnae VARCHAR(15)`, `codigo_gerado VARCHAR(40)`, `resultado VARCHAR(20) NOT NULL` com `CHECK` nos 4 valores, `decisao VARCHAR(25)` com `CHECK (decisao IS NULL OR decisao IN (...3 valores))`, `servico_id INTEGER REFERENCES servicos_mock (id_servico)`) e `consulta_cadastro_sugestoes` (`id` identity, `consulta_id BIGINT NOT NULL REFERENCES consultas_cadastro (id)`, `servico_id INTEGER NOT NULL`, `codigo_padronizado VARCHAR(40) NOT NULL`, `texto_breve VARCHAR(255) NOT NULL`, `score DOUBLE PRECISION NOT NULL`). Índices em `consultas_cadastro (criado_em DESC)` e `consulta_cadastro_sugestoes (consulta_id)`. Primeira linha: comentário no estilo da V9/V10.

- [ ] **Step 4: Create the enums, the two entities and the repository** com as assinaturas do bloco Interfaces. `@Enumerated(EnumType.STRING)` nos enums; mesmo estilo de `LoteImportacao`.

- [ ] **Step 5: Run tests.** `./mvnw test -Dtest=ConsultaCadastroRepositoryTest`. Esperado: 3 testes passando (o contexto aplica a V12 no banco de dev).

---

### Task 2: RegistroConsultaService

**Files:**
- Create: `src/main/java/com/Gerdau/Desafio_GC_Gerdau_App/service/RegistroConsultaService.java`
- Test: `src/test/java/com/Gerdau/Desafio_GC_Gerdau_App/service/RegistroConsultaServiceTest.java`

**Interfaces:**
- Consumes: `ConsultaCadastroRepository`, `AnaliseSimilaridade.SimilarDto(Integer id, String textoBreve, String codigoPadronizado, double score)`.
- Produces: `@Service RegistroConsultaService(ConsultaCadastroRepository repository)` com
  `void registrar(String usuario, String textoBreve, String condicao, String cnae, String codigoGerado, ResultadoConsulta resultado, DecisaoConsulta decisao, Integer servicoId, List<SimilarDto> sugestoes)`.
  Nunca lança: qualquer exceção ao gravar é registrada em log (`WARN`, sem o texto do usuário) e engolida. Sem `@Transactional` no método: o `save` do repositório é a transação, e assim a falha não contamina quem chamou.

- [ ] **Step 1: Write the failing tests** (Mockito, `@ExtendWith(MockitoExtension.class)`):
  - `registrar_montaConsultaComSugestoes`: captura o argumento de `repository.save(...)`; confere usuário, texto, condição, CNAE, código, resultado, decisão, `servicoId` e que as sugestões foram copiadas (código, texto, score) com `consulta` apontando para a consulta salva.
  - `registrar_comSugestoesNulaOuVazia_naoQuebra`: `null` e `List.of()` geram consulta sem sugestões.
  - `registrar_quandoOBancoFalha_naoPropagaExcecao`: `repository.save` lança `DataAccessResourceFailureException`; `assertDoesNotThrow`.

- [ ] **Step 2: Run to verify it fails.** `./mvnw test -Dtest=RegistroConsultaServiceTest`. Esperado: erro de compilação.

- [ ] **Step 3: Implement `RegistroConsultaService.registrar`** conforme Interfaces.

- [ ] **Step 4: Run tests.** Esperado: 3 passando.

---

### Task 3: `cadastrar` e `registrarReutilizacao` gravam o registro

**Files:**
- Modify: `src/main/java/com/Gerdau/Desafio_GC_Gerdau_App/service/ServicoService.java` (construtor, `cadastrar`, método novo)
- Modify: `src/main/java/com/Gerdau/Desafio_GC_Gerdau_App/controller/ServicoController.java` (`criar`) e `ServicoViewController.java` (`criar`) para passar o usuário
- Modify: `src/test/.../service/ServicoServiceTest.java`, `controller/ServicoControllerTest.java`, `controller/ServicoViewControllerTest.java`
- Test: `src/test/.../service/ServicoServiceTest.java`

**Interfaces:**
- Consumes: `RegistroConsultaService.registrar(...)` (Tarefa 2).
- Produces:
  - Construtor `ServicoService(ServicoRepository, CatNbsRepository, CatHorarioRepository, SinonimoServicoService, RegistroConsultaService)`.
  - `Servico cadastrar(NovoServicoForm form, boolean confirmarMesmoAssim, String usuario)` (substitui a assinatura de 2 parâmetros, sem manter a antiga).
  - `Servico registrarReutilizacao(String usuario, String textoBreve, Integer servicoId)`: carrega o serviço com `repository.findById` (lança `NoSuchElementException` se não existir), usa `similares(textoBreve)` como sugestões, grava `SUGERIDO` / `REUTILIZOU_EXISTENTE` / `servicoId` com condição, CNAE e código gerado `null`, e devolve o serviço.

- [ ] **Step 1: Atualizar os testes existentes** de `ServicoServiceTest` para a nova assinatura (`cadastrar(form(), x, "ana")`) e o construtor com um `@Mock RegistroConsultaService`. `ServicoControllerTest`/`ServicoViewControllerTest`: ajustar os `when(service.cadastrar(...))` para 3 parâmetros (`any()`/`anyString()`).

- [ ] **Step 2: Write the failing tests** em `ServicoServiceTest` (verificam `registro.registrar(...)` com `ArgumentCaptor` ou argumentos exatos, conforme a tabela de mapeamento):
  - `cadastrar_bloqueado_registraBloqueadoEAindaLancaExcecao`
  - `cadastrar_semConfirmarComParecidos_registraRequerConfirmacao`
  - `cadastrar_semParecidos_registraCadastradoCadastrouNovoComIdCriado`
  - `cadastrar_comConfirmacaoEParecidos_registraConfirmouMesmoAssim`
  - `cadastrar_corridaDeUnicidade_registraBloqueado` (o `saveAndFlush` lança `DataIntegrityViolationException`)
  - `cadastrar_erroDeValidacao_naoRegistra` (código acima de 40 caracteres: `verify(registro, never()).registrar(...)`)
  - `cadastrar_registroLancaExcecao_cadastroContinua`: `doThrow(new RuntimeException()).when(registro).registrar(...)`; o cadastro devolve o serviço normalmente (rede de proteção extra: o `registrar` já não lança, mas `cadastrar` não deve depender disso).
  - `registrarReutilizacao_gravaSugeridoReutilizouEDevolveOServico`
  - `registrarReutilizacao_servicoInexistente_lancaNoSuchElementESemRegistro`

- [ ] **Step 3: Run to verify they fail.** `./mvnw test -Dtest=ServicoServiceTest`. Esperado: falha de compilação/asserções.

- [ ] **Step 4: Implement** no `ServicoService`: injetar `RegistroConsultaService`; em `cadastrar`, gravar o registro em cada saída da tabela de mapeamento (um helper privado `registrar(usuario, form, analise, resultado, decisao, servicoId)` envolvendo a chamada em `try/catch (RuntimeException)` com `log.warn`); implementar `registrarReutilizacao`.

- [ ] **Step 5: Passar o usuário nos controllers.** `ServicoController.criar` ganha `Principal principal` (`principal.getName()`); `ServicoViewController.criar` ganha `Authentication authentication` (`authentication.getName()`). Ajustar os testes dos controllers com `@WithMockUser(username = "ana")` e conferir que o serviço recebe `"ana"` (`verify(service).cadastrar(any(), eq(false), eq("ana"))`).

- [ ] **Step 6: Run tests.** `./mvnw test -Dtest='ServicoServiceTest,ServicoControllerTest,ServicoViewControllerTest'`. Esperado: tudo verde.

---

### Task 4: Botão "Usar este código"

**Files:**
- Modify: `src/main/java/com/Gerdau/Desafio_GC_Gerdau_App/controller/ServicoViewController.java`
- Modify: `src/main/resources/templates/servico-form.html`, `servico-detalhe.html` (confirmar que mostra a mensagem `mensagem`; já mostra a do cadastro, reutilizar o mesmo bloco)
- Modify: `src/main/resources/static/js/similares.js`
- Modify: `src/main/resources/static/css/style.css` (estilo do botão, no padrão do `.copiar`)
- Test: `src/test/.../controller/ServicoViewControllerTest.java`

**Interfaces:**
- Consumes: `ServicoService.registrarReutilizacao(String usuario, String textoBreve, Integer servicoId): Servico` (Tarefa 3).
- Produces: `POST /ui/servicos/reutilizar` com `textoBreve` e `servicoId`; sucesso redireciona para `/ui/servicos/{servicoId}` com flash `mensagem` = `"Código <código> reutilizado. Nenhum serviço novo foi cadastrado."`; `NoSuchElementException` redireciona para `/ui/servicos/novo` com flash `erro` = `"Serviço não encontrado."` (o formulário exibe o flash `erro` num `.aviso.bloqueio`).

- [ ] **Step 1: Write the failing tests** (`ServicoViewControllerTest`, `.with(csrf())`):
  - `reutilizar_registraEVaiParaODetalheComMensagem`: `service.registrarReutilizacao("user", "Solda de tubo", 7)` devolve um `Servico` com código `ABC_HN1234567`; resposta redirecionando para `/ui/servicos/7`, flash `mensagem` contendo o código; `verify` com o nome do usuário logado.
  - `reutilizar_servicoInexistente_voltaAoFormularioComErro`: o serviço lança `NoSuchElementException`; redireciona para `/ui/servicos/novo` com flash `erro`; sem erro 500.
  - `reutilizar_semCsrf_eRecusado`: 403.
  - `formulario_comParecidos_mostraBotaoUsarEsteCodigo`: ao rejeitar um cadastro com `analise.similares` não vazio, o HTML contém `Usar este código` e `formaction="/ui/servicos/reutilizar"` e `formnovalidate`.

- [ ] **Step 2: Run to verify they fail.** `./mvnw test -Dtest=ServicoViewControllerTest`. Esperado: falhas.

- [ ] **Step 3: Implement `ServicoViewController.reutilizar(String textoBreve, Integer servicoId, Authentication authentication, RedirectAttributes redirect): String`** com `@PostMapping("/ui/servicos/reutilizar")`, conforme Interfaces.

- [ ] **Step 4: Botão no `servico-form.html`.** Em cada item da lista "Parecidos encontrados ao cadastrar", `<button type="submit" class="usar" name="servicoId" th:value="${s.id}" th:formaction="@{/ui/servicos/reutilizar}" formmethod="post" formnovalidate>Usar este código</button>`. Ele fica dentro do formulário principal, por isso leva `textoBreve` e o token CSRF junto, e `formnovalidate` ignora os `required` dos outros campos. Adicionar o bloco do flash `erro` perto dos avisos do topo.

- [ ] **Step 5: Botão na lista ao digitar** (`similares.js`): em `mostrar`, criar o mesmo `<button>` com `setAttribute('formaction', campo.dataset.urlReutilizar)`, `formmethod="post"`, `formnovalidate`, `name="servicoId"`, `value=s.id`, `textContent = 'Usar este código'`. Em `servico-form.html`, acrescentar `th:attr="data-url=@{/servicos/similares},data-url-reutilizar=@{/ui/servicos/reutilizar}"` ao campo do texto. Sem `innerHTML`.

- [ ] **Step 6: Run tests.** `./mvnw test -Dtest=ServicoViewControllerTest`. Esperado: verde. Depois conferir à mão: `./mvnw spring-boot:run`, digitar um texto parecido com um serviço existente, clicar em "Usar este código" e ver o redirecionamento com a mensagem (mesmo com os outros campos vazios).

---

### Task 5: Telas de consultas (só aprovador)

**Files:**
- Create: `src/main/java/com/Gerdau/Desafio_GC_Gerdau_App/controller/ConsultaViewController.java`
- Create: `src/main/resources/templates/consultas.html`, `consulta-detalhe.html`
- Modify: `src/main/java/com/Gerdau/Desafio_GC_Gerdau_App/config/SecurityConfig.java` (incluir `/ui/consultas/**` na regra de `APROVADOR`)
- Modify: `src/main/resources/templates/layout.html` (link "Consultas" no menu, com `th:if="${ehAprovador}"` e `ativo == 'consultas'`)
- Test: `src/test/.../controller/ConsultaViewControllerTest.java` e `AcessoPorPerfilTest.java`

**Interfaces:**
- Consumes: `ConsultaCadastroRepository.findTop200ByOrderByCriadoEmDesc()` e `findComSugestoesById(Long)` (Tarefa 1).
- Produces: `GET /ui/consultas` (view `consultas`, atributo `consultas`) e `GET /ui/consultas/{id}` (view `consulta-detalhe`, atributo `consulta`; id inexistente → `ResponseStatusException(HttpStatus.NOT_FOUND)`). Seguir `RevisaoViewController` para como `ehAprovador`/`usuarioLogado` chegam ao modelo.

- [ ] **Step 1: Write the failing tests:**
  - `ConsultaViewControllerTest` (`@WebMvcTest`, `@WithMockUser(roles = "APROVADOR")`): `lista_mostraAsConsultas` (usuário, texto, resultado e decisão aparecem); `lista_vazia_mostraMensagem`; `detalhe_mostraSugestoesEScores`; `detalhe_idInexistente_da404`; `textoComHtml_apareceEscapado` (texto `<b>x</b> & "y"` aparece como `&lt;b&gt;`, sem `<b>` cru).
  - `AcessoPorPerfilTest`: `comprador_naoAcessaConsultas` (GET `/ui/consultas` e `/ui/consultas/1` → 403) e `semLogin_vaiParaLogin`.

- [ ] **Step 2: Run to verify they fail.** `./mvnw test -Dtest='ConsultaViewControllerTest,AcessoPorPerfilTest'`.

- [ ] **Step 3: Implement** o controller, as duas telas (tabela com data, usuário, texto, condição, CNAE, resultado, decisão, serviço; detalhe com a lista de sugestões e link para cada serviço; rótulos legíveis para os valores dos enums, ex.: "Bloqueado", "Pediu confirmação", "Cadastrou novo", "Cadastrou mesmo assim", "Reutilizou código existente"), a regra de segurança e o link do menu. Estilo e classes seguindo `revisao.html`/`style.css`; só `th:text`.

- [ ] **Step 4: Run tests.** `./mvnw test -Dtest='ConsultaViewControllerTest,AcessoPorPerfilTest,ServicoViewControllerTest,LoginControllerTest,RevisaoViewControllerTest'`. Esperado: verde (o menu é compartilhado, então as outras telas precisam continuar passando).

---

### Task 6: Documentação e verificação final

**Files:**
- Modify: `README.md` (lista de migrations V1 a V12, linha da V12 na tabela, rotas `/ui/consultas`, `POST /ui/servicos/reutilizar`, menção ao registro)
- Modify: `docs/GUIA_DE_USO.md` (botão "Usar este código" no cadastro; tela **Consultas** do aprovador)
- Modify: `docs/REGRAS_DE_NEGOCIO.md` (seção "Registro das consultas": o que é gravado, tabela resultado × decisão, que erros de validação não geram registro, que falha ao gravar não impede o cadastro)
- Modify: `CLAUDE.md` ("hoje V1–V11" → "V1–V12"; mencionar `RegistroConsultaService` junto de `ServicoService`)

- [ ] **Step 1: Atualizar os quatro arquivos** conforme acima.
- [ ] **Step 2: Rodar a suíte inteira.** `./mvnw test` (Postgres do compose no ar). Esperado: `BUILD SUCCESS`, nenhum teste falhando.
- [ ] **Step 3: Conferência ponta a ponta à mão** (`./mvnw spring-boot:run`): cadastrar um serviço novo sem parecidos, tentar um duplicado, cadastrar um parecido com "Cadastrar mesmo assim" e usar o botão numa sugestão; entrar como aprovador, abrir **Consultas** e ver as 4 linhas com os resultados e decisões esperados.

---

## Autorrevisão

- **Cobertura do desenho:** tabelas e campos (T1), gravação em todas as saídas e botão (T3, T4), telas e acesso (T5), docs (T6). A importação fica fora, como combinado.
- **Consistência de tipos:** `registrar(...)` (T2) tem os mesmos 9 parâmetros usados em T3; `registrarReutilizacao(String, String, Integer): Servico` (T3) é o que T4 consome; `findComSugestoesById`/`findTop200ByOrderByCriadoEmDesc` (T1) são o que T5 consome.
- **Minhas escolhas, para conferir:** gravação "melhor esforço" (falha de registro só vira log), mapeamento da tabela resultado × decisão, `SUGERIDO` como resultado da reutilização, últimas 200 consultas sem paginação, botão via `formaction` dentro do formulário existente.
