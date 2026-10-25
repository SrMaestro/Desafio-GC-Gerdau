# Redesign das telas Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Dar às 10 telas um visual corporativo único, com menu lateral azul e três cards de resumo com números reais na lista de serviços, sem mudar regra de negócio.

**Architecture:** O cabeçalho copiado em cada template vira um fragmento `menu` em `layout.html`, e `<body class="app">` vira uma grade (menu + `main`). Os totais dos cards vêm de um `ResumoService` novo, injetado em `ServicoViewController`. Todo o estilo continua em CSS puro, em `style.css`, `importacao.css` e `revisao.css`.

**Tech Stack:** Spring Boot 4.1, Thymeleaf, Spring Security, JUnit 5 + Mockito + MockMvc, CSS puro.

**Spec:** `docs/superpowers/specs/2026-10-25-redesign-telas-design.md`

## Global Constraints

- Telas (10): `login`, `servicos`, `servico-form`, `servico-detalhe`, `busca`, `importacoes`, `importacao-lote`, `aprovacao-lotes`, `aprovacao-lote`, `revisao`.
- Cores da marca: azul `#003b71`, laranja `#e0561b`. Sem tema escuro.
- Thymeleaf + CSS puro. Sem framework de front-end, sem etapa de build, sem fonte nem biblioteca de ícones externa (ícones, se houver, em SVG inline ou texto).
- Sem migration, sem rota nova, sem mudança de permissão. Os cards não criam endpoint de API.
- **Não renomear nem remover** `id`s, atributos `data-*` ou classes usados por `static/js/*.js`. Classes novas são aditivas. Antes de mexer em qualquer classe existente, rode `grep -rn "<classe>" src/main/resources/static/js`.
- Os cards: "Serviços cadastrados" (total de serviços), "Mapeamentos pendentes" (De-Para `PENDENTE`) e "Linhas aguardando aprovação" (linhas na etapa `ENVIADA`; **só aparece para o aprovador**).
- O menu mostra Serviços, Buscar NBS, Importar e, só para o aprovador, Revisão e Aprovações. Usuário logado e botão Sair ficam no rodapé do menu.
- Acessibilidade existente se mantém: foco visível, `aria-live`, `aria-label`, `aria-current`.
- Testes existentes que fixam o contrato do menu e devem continuar passando: `ServicoViewControllerTest` (nome do usuário, `action="/logout"`, `<meta name="_csrf"`, `/ui/revisao` ausente para comprador) e `ImportacaoViewControllerTest` (`Aprovações` ausente do HTML do comprador).
- Comandos: `./mvnw test -Dtest=<Classe>`; suíte inteira `./mvnw test` (precisa do Postgres do compose no ar).

## Review Focus

- Contagem falha (banco indisponível): a lista de serviços ainda abre e o card mostra "—" (Task 1 e Task 3).
- Banco vazio: os cards mostram "0", não "—" nem vazio (Task 1).
- Comprador nunca vê "Aprovações", "Revisão" nem o card de aprovação, em nenhuma tela (Task 2 e Task 3).
- Nome de usuário comprido não estoura o rodapé do menu (Task 2).
- Celular (≤ 720 px): o menu não cria rolagem horizontal da página e a tabela continua virando blocos (Task 4, verificação manual).

---

### Task 1: ResumoService com as três contagens

**Files:**
- Create: `src/main/java/com/Gerdau/Desafio_GC_Gerdau_App/dto/Resumo.java`
- Create: `src/main/java/com/Gerdau/Desafio_GC_Gerdau_App/service/ResumoService.java`
- Modify: `src/main/java/com/Gerdau/Desafio_GC_Gerdau_App/repository/DicionarioDeParaRepository.java`
- Modify: `src/main/java/com/Gerdau/Desafio_GC_Gerdau_App/repository/LinhaImportacaoRepository.java`
- Test: `src/test/java/com/Gerdau/Desafio_GC_Gerdau_App/service/ResumoServiceTest.java` (Mockito, sem banco)

**Interfaces:**
- Produces: `record Resumo(Long servicos, Long pendentes, Long aguardandoAprovacao)` (`null` = contagem indisponível). `ResumoService.resumo(boolean aprovador): Resumo`; para comprador `aguardandoAprovacao` é sempre `null`, sem consultar o banco.
- Produces: `long DicionarioDeParaRepository.countByStatus(StatusDePara status)` e `long LinhaImportacaoRepository.countByEtapa(EtapaLinha etapa)` (query derivada do Spring Data).
- Consumes: `ServicoRepository.count()` (herdado de `JpaRepository`).

- [ ] **Step 1: Escrever `ResumoServiceTest`** com Mockito (`@ExtendWith(MockitoExtension.class)`) e estes testes:
  - `aprovador_devolveAsTresContagens`: `servicoRepository.count()` → 1122, `countByStatus(PENDENTE)` → 37, `countByEtapa(ENVIADA)` → 5; `resumo(true)` é `new Resumo(1122L, 37L, 5L)`.
  - `comprador_naoConsultaLinhasEDevolveNullNoTerceiro`: `resumo(false)` tem `aguardandoAprovacao() == null` e `verifyNoInteractions(linhaImportacaoRepository)`.
  - `bancoVazio_devolveZeros`: todas as contagens 0 → `Resumo(0L, 0L, 0L)` (não `null`).
  - `contagemQueFalha_viraNullSemPropagar`: `dicionarioDeParaRepository.countByStatus(any())` lança `DataAccessResourceFailureException`; `resumo(true).pendentes()` é `null` e as outras duas contagens continuam preenchidas.
- [ ] **Step 2: Rodar e ver falhar.** `./mvnw test -Dtest=ResumoServiceTest`. Esperado: erro de compilação (classes inexistentes).
- [ ] **Step 3: Implementar** `Resumo`, os dois `countBy...` e `ResumoService` (`@Service`, construtor com `ServicoRepository`, `DicionarioDeParaRepository`, `LinhaImportacaoRepository`). Cada contagem fica num helper privado que captura `DataAccessException` e devolve `null`, para uma falha não derrubar as outras. Siga o estilo do `ServicoService` (injeção por construtor).
- [ ] **Step 4: Rodar e ver passar.** `./mvnw test -Dtest=ResumoServiceTest`. Esperado: 4 testes passando.
- [ ] **Step 5: Commit.** `git add` dos 5 arquivos; `git commit -m "feat(resumo): contagens para os cards da lista de serviços"`.

---

### Task 2: Menu lateral compartilhado em todas as telas

**Files:**
- Modify: `src/main/resources/templates/layout.html` (fragmento `menu(ativo)` substitui o fragmento `usuario`)
- Modify: as 9 telas autenticadas em `src/main/resources/templates/` (trocar `<header>…</header>` por `<th:block th:replace="~{layout :: menu('<chave>')}"/>` e `<body>` por `<body class="app">`): `servicos`, `servico-form`, `servico-detalhe`, `busca`, `importacoes`, `importacao-lote`, `aprovacao-lotes`, `aprovacao-lote`, `revisao`
- Modify: `src/main/resources/templates/login.html` (só o `<body class="login">` e a remoção do `<header>`; o visual vem na Task 4)
- Modify: `src/main/resources/static/css/style.css` (grade `body.app`, `.menu` e filhos; remover as regras de `header`, `header .nav` e `header .usuario`)
- Modify: `docs/GUIA_DE_USO.md` (seção "0. Entrar": descrever o menu lateral e quais itens cada perfil vê)
- Test: `src/test/java/com/Gerdau/Desafio_GC_Gerdau_App/controller/ServicoViewControllerTest.java`

**Interfaces:**
- Produces: fragmento `layout :: menu(ativo)`, com `ativo` entre `'servicos'`, `'busca'`, `'importar'`, `'aprovacoes'`, `'revisao'` (ou vazio); o item ativo leva `aria-current="page"`. Estrutura: `<aside class="menu">` com a marca, `<nav>` com os links e `<div class="usuario">` no rodapé (nome + `<form method="post" th:action="@{/logout}">` com botão Sair).
- Produces: o item ativo de cada tela: `servicos`, `servico-form` e `servico-detalhe` → `'servicos'`; `busca` → `'busca'`; `importacoes` e `importacao-lote` → `'importar'`; `aprovacao-lotes` e `aprovacao-lote` → `'aprovacoes'`; `revisao` → `'revisao'`.
- Consumes: `usuarioLogado` e `ehAprovador` do `UsuarioLogadoAdvice` (já existentes). Manter `<meta name="_csrf">` no `head`.

- [ ] **Step 1: Escrever os testes** em `ServicoViewControllerTest`:
  - `menu_compradorVeServicosBuscaEImportarMasNaoRevisaoNemAprovacoes` (`@WithMockUser(roles="COMPRADOR")`): `GET /` contém `href="/ui/busca"`, `href="/ui/importacoes"`, `aria-current="page"`; não contém `/ui/revisao`, `/ui/aprovacao` nem `Aprovações`.
  - `menu_aprovadorVeRevisaoEAprovacoes`: `GET /` contém `href="/ui/revisao"` e `href="/ui/aprovacao"`.
  - `menu_marcaOItemDaTelaAtual`: `GET /ui/busca` devolve HTML em que o link de busca carrega `aria-current="page"` (por exemplo `containsString("href=\"/ui/busca\" aria-current=\"page\"")`; ajuste a ordem dos atributos ao que o fragmento gerar).
  - Os dois testes `cabecalho_*` existentes continuam como estão.
- [ ] **Step 2: Rodar e ver falhar.** `./mvnw test -Dtest=ServicoViewControllerTest`. Esperado: `menu_*` falham (não há link `/ui/busca` no cabeçalho atual; sem `aria-current`).
- [ ] **Step 3: Implementar o fragmento `menu(ativo)`** em `layout.html` e trocar o cabeçalho nas 10 telas conforme a tabela de Interfaces. Remova o fragmento `usuario` antigo, depois de conferir com `grep -rn "layout :: usuario" src/main/resources/templates` que nenhuma tela o usa mais. O nome do usuário no rodapé trunca com reticências (`text-overflow: ellipsis`, `title` com o nome inteiro).
- [ ] **Step 4: CSS do shell** em `style.css`: `body.app { display: grid; grid-template-columns: 240px minmax(0, 1fr); min-height: 100vh }`; `.menu` azul `--azul`, sticky/altura da janela, links com estado hover e ativo (fundo `rgba(255,255,255,.15)`); `.menu .usuario` empurrado para o fim (`margin-top: auto`). Abaixo de 720 px: `body.app` vira uma coluna, o `.menu` vira uma barra horizontal que quebra linha (sem rolagem horizontal da página) e o rodapé do usuário passa a ser uma linha da barra. `main` mantém `max-width: 1180px`. Mantenha o foco visível do `style.css` atual.
- [ ] **Step 5: Atualizar `docs/GUIA_DE_USO.md`** (menu lateral; Revisão e Aprovações só para o aprovador).
- [ ] **Step 6: Rodar os testes de tela.** `./mvnw test -Dtest='ServicoViewControllerTest,ImportacaoViewControllerTest,RevisaoViewControllerTest,LoginControllerTest,AcessoPorPerfilTest'`. Esperado: todos passando, incluindo `linkAprovacoes_compradorNaoVe`.
- [ ] **Step 7: Commit.** `git commit -m "feat(ui): menu lateral compartilhado nas telas"`.

---

### Task 3: Cards de resumo na lista de serviços

**Files:**
- Modify: `src/main/java/com/Gerdau/Desafio_GC_Gerdau_App/controller/ServicoViewController.java` (`listar`)
- Modify: `src/main/resources/templates/servicos.html`
- Modify: `src/main/resources/static/css/style.css` (`.resumo`, `.resumo-card`)
- Modify: `docs/GUIA_DE_USO.md` (explicar os três cards)
- Test: `src/test/java/com/Gerdau/Desafio_GC_Gerdau_App/controller/ServicoViewControllerTest.java`

**Interfaces:**
- Consumes: `ResumoService.resumo(boolean aprovador): Resumo` (Task 1) e `ehAprovador` do `UsuarioLogadoAdvice`.
- Produces: atributo de modelo `resumo` (tipo `Resumo`) em `GET /`. O controller ganha `ResumoService` no construtor e usa `Authentication` para decidir `aprovador` (mesma regra `ROLE_APROVADOR` do advice).

- [ ] **Step 1: Escrever os testes** em `ServicoViewControllerTest` (adicionar `@MockitoBean ResumoService resumoService`):
  - `cards_aprovadorVeOsTresComNumeros`: `resumo(true)` → `Resumo(1122L, 37L, 5L)`; `GET /` contém `1122`, `37`, `5`, `Serviços cadastrados`, `Mapeamentos pendentes`, `Linhas aguardando aprovação`.
  - `cards_compradorNaoVeOCardDeAprovacao` (`roles="COMPRADOR"`): `resumo(false)` → `Resumo(1122L, 37L, null)`; a página contém `Serviços cadastrados` e não contém `Linhas aguardando aprovação`.
  - `cards_contagemIndisponivelMostraTracoEAPaginaAbre`: `resumo(true)` → `Resumo(null, 37L, 5L)`; status 200 e o card de serviços mostra `—`.
  - `cards_zeroApareceComoZero`: `Resumo(0L, 0L, 0L)` → a página mostra `0` (não `—`).
  - Os testes existentes que chamam `GET /` precisam do mock devolvendo `new Resumo(0L, 0L, 0L)`; use `@BeforeEach` para isso.
- [ ] **Step 2: Rodar e ver falhar.** `./mvnw test -Dtest=ServicoViewControllerTest`. Esperado: `cards_*` falham.
- [ ] **Step 3: Implementar** a injeção de `ResumoService`, o atributo `resumo` em `listar` e o bloco `<section class="resumo" aria-label="Resumo">` em `servicos.html` acima da busca. Cada card é um `<div class="resumo-card">` com rótulo e número; número `null` renderiza `—`. O card de aprovação usa `th:if="${ehAprovador}"`. Mantenha intactos `#busca`, `#contador`, `#tabela-servicos` e `data-linha`.
- [ ] **Step 4: CSS** de `.resumo` (grade responsiva `repeat(auto-fit, minmax(180px, 1fr))`) e `.resumo-card` (fundo branco, borda `--linha`, canto 10 px, número grande; o card de pendentes com número em `--brasa`).
- [ ] **Step 5: Atualizar `docs/GUIA_DE_USO.md`** (o que cada card conta e que o terceiro é do aprovador).
- [ ] **Step 6: Rodar os testes.** `./mvnw test -Dtest=ServicoViewControllerTest`. Esperado: todos passando.
- [ ] **Step 7: Commit.** `git commit -m "feat(ui): cards de resumo na lista de serviços"`.

---

### Task 4: Visual dos componentes comuns e telas de serviço

**Files:**
- Modify: `src/main/resources/static/css/style.css` (tokens, botões, campos, cards, tabela, chips, selos, avisos, Smart Key, formulário)
- Modify: `src/main/resources/templates/login.html` (cartão central, cores da marca)
- Modify: `src/main/resources/templates/servico-detalhe.html` (troca o `style="color:green"` por a classe `.aviso.ok`)
- Modify (só se preciso): `servicos.html`, `servico-form.html`, `busca.html`

**Interfaces:**
- Consumes: shell e `.menu` da Task 2. Mantém todos os nomes de classe atuais de `style.css` (`.card`, `.botao`, `.busca`, `.chip`, `.copiar`, `.codigo`, `.tabela-wrap`, `.sugestoes`, `.smart-key`, `.aviso`, `.topo`, ...) e só acrescenta classes novas.

- [ ] **Step 1: Tokens** em `:root`: raio (`--raio: 10px`), sombra suave, espaçamentos, cores de selo (verde `#1d7a4d` sobre `#e3f1ea`, laranja, cinza). `--papel` passa a um cinza-azulado muito claro para o fundo da área de conteúdo.
- [ ] **Step 2: Componentes.** Atualize, com a mesma lista de classes: botões (`.botao` e `button` neutros com cantos arredondados e transição já respeitando `prefers-reduced-motion`), campos (`input`, `select`, `.busca`: borda, foco azul), `.card`, `.tabela-wrap`/`table` (cantos arredondados, cabeçalho em tom claro em vez de azul chapado, linhas com mais respiro), `.chip`, `.aviso` (`.ok` e `.erro`), `.smart-key`. A versão celular da tabela (bloco `@media (max-width: 720px)`) continua funcionando: não remova as regras `tbody tr[data-linha]`.
- [ ] **Step 3: Login.** `body.login` centraliza um cartão com a marca no topo, campos empilhados e botão laranja de largura total; mensagens `.aviso.erro` e `.aviso.ok` mantêm `role` atual.
- [ ] **Step 4: Detalhe do serviço.** A mensagem de sucesso vira `<p class="aviso ok" role="status">`; a lista `dl` passa a uma grade de duas colunas (rótulo/valor) em telas largas.
- [ ] **Step 5: Verificar o JS intacto.** Rode `git diff --stat -- src/main/resources/static/js src/main/resources/templates` e confirme que nenhum `id=` nem `data-*` foi removido ou renomeado: `git diff -U0 -- src/main/resources/templates | grep -E '^-.*(id="|data-)'` não deve listar nada além das linhas de `<header>` trocadas na Task 2.
- [ ] **Step 6: Verificação manual** (suba o banco e a app: `docker compose up -d && ./mvnw spring-boot:run`; use o skill `run` ou o navegador). Em largura de desktop e de celular (~390 px), para os perfis comprador e aprovador, confira: login; lista de serviços (busca filtra, chips, copiar código, cards); detalhe; busca NBS (sugestões e Smart Key aparecem); formulário (partes do código colorem, aviso de similares aparece). Sem rolagem horizontal da página no celular.
- [ ] **Step 7: Rodar a suíte de controllers.** `./mvnw test -Dtest='*ControllerTest'`. Esperado: passando.
- [ ] **Step 8: Commit.** `git commit -m "style(ui): novo visual dos componentes e das telas de serviço"`.

---

### Task 5: Visual das telas de importação, aprovação e revisão

**Files:**
- Modify: `src/main/resources/static/css/importacao.css`
- Modify: `src/main/resources/static/css/revisao.css`
- Modify (só se preciso): `importacoes.html`, `importacao-lote.html`, `aprovacao-lotes.html`, `aprovacao-lote.html`, `revisao.html`

**Interfaces:**
- Consumes: tokens e componentes da Task 4. Mantém as classes que `importacao.js` e `aprovacao.js` criam dinamicamente (`.lotes`, `.bloco*`, `.em-bloco`, `.envio`, `.envio-aprovador`, `.filtro`, `.decisoes`...); confirme com `grep -n "className\|classList\|class=" src/main/resources/static/js/importacao.js src/main/resources/static/js/aprovacao.js` antes de tocar nelas.

- [ ] **Step 1: Alinhar `importacao.css`** aos tokens: `.lotes li` e `.bloco` com raio e sombra suave; as bordas de cor por situação (`bloco-erro`, `bloco-parecida`, `bloco-nova`, `bloco-existente`, `bloco-repetida`, `bloco-rejeitada`) viram selos/barras laterais no mesmo padrão dos selos da Task 4; `.envio` como cartão com área de envio de arquivo mais evidente; `.envio-aprovador` (sticky) com fundo e sombra para ficar legível sobre a tabela; `progress` com a cor da marca.
- [ ] **Step 2: Alinhar `revisao.css`**: tabela, `.selo-conflito`, `td.acoes` e `.aviso` no padrão novo; o campo `.revisor` só é restilizado se ainda existir em `revisao.html` (confira com `grep -n revisor src/main/resources/templates/revisao.html`; se não existir, remova a regra morta).
- [ ] **Step 3: Verificação manual** com a app no ar, perfil comprador e perfil aprovador, desktop e ~390 px: envie uma planilha pequena (`.csv`) em `/ui/importacoes`; acompanhe o progresso; confira o lote (`importacao-lote`), as decisões de parecidas e o envio; como aprovador, abra Aprovações, a lista de lotes e um lote, e a Revisão do De-Para. Tudo deve funcionar como antes e ter o visual novo, sem rolagem horizontal da página.
- [ ] **Step 4: Rodar a suíte inteira.** `./mvnw test` (Postgres do compose no ar). Esperado: tudo passando.
- [ ] **Step 5: Atualizar `README.md`** apenas se alguma linha descrever o cabeçalho ou a navegação antiga (`grep -n -i "cabeçalho\|header" README.md docs/*.md`).
- [ ] **Step 6: Commit.** `git commit -m "style(ui): novo visual de importação, aprovação e revisão"`.

---

## Auto-revisão

- **Cobertura da spec:** 10 telas (Tasks 2, 4, 5); menu lateral e perfis (Task 2); três cards com números reais, o terceiro só do aprovador (Tasks 1 e 3); celular (Tasks 2, 4, 5); login sem menu com o mesmo visual (Tasks 2 e 4); contador de bloqueios fica fora, como combinado; sem framework, sem migration e sem rota nova; `GUIA_DE_USO.md` atualizado (Tasks 2 e 3).
- **Tipos consistentes:** `Resumo(Long, Long, Long)`, `ResumoService.resumo(boolean)` e o atributo `resumo` aparecem com os mesmos nomes nas Tasks 1 e 3; `menu(ativo)` com as mesmas chaves nas Tasks 2 e 4.
- **Limite honesto:** o CSS não tem teste automatizado. As Tasks 4 e 5 se apoiam em verificação manual nos dois perfis e na suíte de controllers para pegar regressões de HTML.
