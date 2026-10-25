# Login com perfis Comprador e Aprovador — Plano de implementação

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Exigir login (usuário e senha no PostgreSQL) em todas as telas e na API, com os perfis `COMPRADOR` e `APROVADOR`, e gravar o usuário logado como revisor do De-Para.

**Architecture:** Spring Security com sessão HTTP e tela de login Thymeleaf. Uma `SecurityConfig` define as regras por rota; `UsuarioDetailsService` carrega usuários da tabela `usuarios` (migration V9). Os controllers do De-Para deixam de receber o nome do revisor e usam o `Principal`. O JS que faz `POST` envia o token CSRF lido de `<meta>` no `layout.html`.

**Tech Stack:** Spring Boot 4.1.1, Spring Security, Thymeleaf, Flyway, JUnit 5 + Mockito + MockMvc (`spring-security-test`).

**Spec:** `docs/superpowers/specs/2026-10-24-login-perfis-design.md`

## Global Constraints

- Pacote base `com.Gerdau.Desafio_GC_Gerdau_App`; código, mensagens e nomes de domínio em português.
- Perfis exatamente `COMPRADOR` e `APROVADOR` (authorities `ROLE_COMPRADOR`, `ROLE_APROVADOR`).
- Hash BCrypt; nenhuma senha em texto puro no banco ou no código (exceto as senhas de exemplo documentadas no guia de uso).
- Migration nova `V9__usuarios.sql`; nunca editar V1–V8.
- COMPRADOR: cadastro, busca, busca NBS, SmartKey. APROVADOR: tudo isso mais `/ui/revisao/**` e `/api/de-para/**`, exceto `POST /api/de-para/confirmacoes`, que o COMPRADOR também usa.
- API sem login responde `401` (sem redirecionar); perfil insuficiente responde `403`.
- Sem tela de cadastro/edição de usuário, sem SSO, sem recuperação de senha.
- Atualizar `README.md`, `docs/GUIA_DE_USO.md` e `CLAUDE.md` (migrations "V1–V8" passam a "V1–V9").

## Review Focus

- Login/CSS: `/login`, `/css/**`, `/js/**` acessíveis sem login, senão a página de login aparece sem estilo ou em loop de redirecionamento.
- Sessão expirada numa chamada `fetch` da busca NBS deve receber `401`, não o HTML da página de login (o JS mostra a mensagem de falha em vez de quebrar).
- `POST` sem token CSRF é recusado (`403`), e o `fetch` de `busca.js` envia o token.
- Usuário com `ativo = FALSE` não consegue entrar.
- Falha de login mostra mensagem clara na tela, sem expor detalhes (usuário inexistente e senha errada têm a mesma mensagem).
- Corpo de `aprovar`/`rejeitar`/`revogar` sem `revisor` (ou com um `revisor` de outra pessoa) grava o usuário logado, nunca o texto do corpo.

---

## File Structure

- Create `src/main/resources/db/migration/V9__usuarios.sql`: tabela `usuarios` e dois usuários de exemplo.
- Create `model/Usuario.java`, `model/PerfilUsuario.java`, `repository/UsuarioRepository.java`, `service/UsuarioDetailsService.java`.
- Create `config/SecurityConfig.java`: regras de rota, `PasswordEncoder`, login/logout, 401 na API.
- Create `controller/LoginController.java`, `controller/UsuarioLogadoAdvice.java`; template `login.html`.
- Modify controllers do De-Para, `RevisaoRequest`, `revisao.html`, `layout.html`, demais templates (cabeçalho), `busca.js`; delete `revisao.js`.
- Modify todos os testes em `src/test/.../controller` e criar `AcessoPorPerfilTest`, `LoginControllerTest`.

---

### Task 1: Usuários no banco e carregamento para o Spring Security

**Files:**
- Modify: `pom.xml` (adicionar `spring-boot-starter-security` e, com escopo test, `spring-boot-starter-security-test`; se esse artefato não existir na 4.1.1, usar `org.springframework.security:spring-security-test`)
- Create: `src/main/resources/db/migration/V9__usuarios.sql`
- Create: `model/PerfilUsuario.java`, `model/Usuario.java`, `repository/UsuarioRepository.java`, `service/UsuarioDetailsService.java`
- Test: `src/test/java/com/Gerdau/Desafio_GC_Gerdau_App/service/UsuarioDetailsServiceTest.java`

**Interfaces:**
- Produces: `enum PerfilUsuario { COMPRADOR, APROVADOR }`; `Usuario` (campos `id`, `username`, `senhaHash`, `perfil`, `ativo`); `UsuarioRepository extends JpaRepository<Usuario, Long>` com `Optional<Usuario> findByUsername(String username)`; `UsuarioDetailsService implements UserDetailsService`, construtor `UsuarioDetailsService(UsuarioRepository)`.

- [ ] **Step 1: Write the failing test** `UsuarioDetailsServiceTest` (Mockito, sem banco), com estes testes:
  - `carrega_usuarioAtivo`: repository devolve `Usuario("ana", "$2a$hash", APROVADOR, ativo=true)`; `loadUserByUsername("ana")` tem `getUsername()=="ana"`, `getPassword()=="$2a$hash"`, authorities `[ROLE_APROVADOR]`, `isEnabled()`.
  - `usuarioInativo_ficaDesabilitado`: `ativo=false` → `isEnabled()` é `false`.
  - `usuarioInexistente_lancaUsernameNotFound`: repository devolve `Optional.empty()` → `UsernameNotFoundException`.
- [ ] **Step 2: Run** `./mvnw test -Dtest=UsuarioDetailsServiceTest` — Expected: FAIL (classes não existem).
- [ ] **Step 3: Implement** `PerfilUsuario`, `Usuario` (`@Entity @Table(name="usuarios")`, Lombok como as outras entidades, `perfil` com `@Enumerated(EnumType.STRING)`), `UsuarioRepository` e `UsuarioDetailsService` (anotado `@Service`; monta `org.springframework.security.core.userdetails.User` com `.disabled(!ativo)` e `.roles(perfil.name())`).
- [ ] **Step 4: Write `V9__usuarios.sql`**: `usuarios(id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, username VARCHAR(50) NOT NULL UNIQUE, senha_hash VARCHAR(100) NOT NULL, perfil VARCHAR(10) NOT NULL CHECK (perfil IN ('COMPRADOR','APROVADOR')), ativo BOOLEAN NOT NULL DEFAULT TRUE)`. Atenção: `Usuario.id` deve ser `Integer` ou a coluna `BIGINT`, para o `ddl-auto=validate` passar. Inserir `comprador` (senha `comprador123`) e `aprovador` (senha `aprovador123`) com hashes BCrypt reais, gerados com `BCryptPasswordEncoder` (por exemplo via `jshell` com o classpath de `./mvnw dependency:build-classpath`); não inventar hash.
- [ ] **Step 5: Run** `./mvnw test -Dtest=UsuarioDetailsServiceTest` — Expected: PASS.
- [ ] **Step 6: Commit** `git add pom.xml src/main src/test && git commit -m "feat(login): tabela de usuarios e carregamento para o Spring Security"`

---

### Task 2: Regras de acesso, login e adaptação dos testes existentes

Esta tarefa é atômica: ao ligar a segurança, todos os testes de controller existentes quebram até serem adaptados.

**Files:**
- Create: `config/SecurityConfig.java`, `controller/LoginController.java`, `src/main/resources/templates/login.html`
- Modify: os 7 testes em `src/test/java/.../controller/*Test.java`
- Test: `controller/AcessoPorPerfilTest.java`, `controller/LoginControllerTest.java`

**Interfaces:**
- Produces: `SecurityConfig` (`@Configuration @EnableWebSecurity`) com beans `SecurityFilterChain securityFilterChain(HttpSecurity http)` e `PasswordEncoder passwordEncoder()` (BCrypt). Não deve depender de repository, para poder ser importado em `@WebMvcTest`. `LoginController`: `GET /login` → view `login`.

- [ ] **Step 1: Write the failing tests.**
  `AcessoPorPerfilTest` (`@WebMvcTest(DeParaController.class)` + `@Import(SecurityConfig.class)` + `@MockitoBean DeParaService`), com:
  - `semLogin_apiResponde401`: `get("/api/de-para/fila")` → `401`, sem header `Location`.
  - `comprador_naoAcessaFilaDoDePara`: `@WithMockUser(roles="COMPRADOR")` `get("/api/de-para/fila")` → `403`.
  - `comprador_podeConfirmar`: `@WithMockUser(roles="COMPRADOR")` `post("/api/de-para/confirmacoes")` com JSON válido e `csrf()` → `2xx`.
  - `comprador_naoAprova`: `post("/api/de-para/1/aprovar")` com `csrf()` → `403`.
  - `aprovador_acessaFila`: `@WithMockUser(roles="APROVADOR")` → `200`.
  - `postSemCsrf_ehRecusado`: `@WithMockUser(roles="APROVADOR")` `post("/api/de-para/1/aprovar")` sem `csrf()` → `403`.
  Teste equivalente para a tela: `@WebMvcTest(RevisaoViewController.class)` `comprador_naoAcessaTelaDeRevisao` → `get("/ui/revisao")` `403`; sem login → redirect (`302`) para `/login`.
  `LoginControllerTest`: `get("/login")` sem login → `200`, view `login`; `get("/css/style.css")` sem login → `200`.
- [ ] **Step 2: Run** `./mvnw test -Dtest='AcessoPorPerfilTest,LoginControllerTest'` — Expected: FAIL.
- [ ] **Step 3: Implement `SecurityConfig.securityFilterChain`.** Regras, nesta ordem: `/login`, `/css/**`, `/js/**` liberados; `POST /api/de-para/confirmacoes` para `COMPRADOR` ou `APROVADOR`; `/ui/revisao/**` e `/api/de-para/**` só `APROVADOR`; qualquer outra rota autenticada. `formLogin` em `/login` (sucesso → `/`, falha → `/login?error`), `logout` em `POST /logout` (→ `/login?logout`). Para `/api/**` e `/servicos/**` usar `HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)` (via `defaultAuthenticationEntryPointFor`) em vez do redirecionamento. CSRF mantido no padrão.
- [ ] **Step 4: Implement `LoginController` e `login.html`** no estilo das telas atuais (usar `layout :: head('Entrar')`, `/css/style.css`). Formulário `th:action="@{/login}"`, campos `username` e `password`. Mostrar "Usuário ou senha inválidos." quando `param.error` e "Você saiu." quando `param.logout`.
- [ ] **Step 5: Adaptar os 7 testes existentes.** Em cada classe `@WebMvcTest`: `@Import(SecurityConfig.class)`, `@WithMockUser(roles="APROVADOR")` na classe, e `.with(csrf())` em todo `post(...)`.
- [ ] **Step 6: Run** `./mvnw test` (exceto `DesafioGcGerdauAppApplicationTests`, que precisa do banco: `-Dtest='!DesafioGcGerdauAppApplicationTests'`) — Expected: PASS.
- [ ] **Step 7: Commit** `git commit -m "feat(login): regras de acesso por perfil e tela de login"`

---

### Task 3: O revisor é o usuário logado

**Files:**
- Modify: `dto/RevisaoRequest.java`, `controller/DeParaController.java`, `controller/RevisaoViewController.java`, `templates/revisao.html`
- Delete: `static/js/revisao.js`
- Test: `DeParaControllerTest.java`, `RevisaoViewControllerTest.java`

**Interfaces:**
- Consumes: sessão autenticada (Task 2).
- Produces: `record RevisaoRequest(String motivo)`; `DeParaController.aprovar/rejeitar/revogar(@PathVariable Long id, @RequestBody RevisaoRequest request, Principal principal)`; `RevisaoViewController.aprovar(Long id, Principal principal, RedirectAttributes)`, e `rejeitar`/`revogar` com `@RequestParam String motivo`. O serviço (`DeParaService`) não muda: continua recebendo `String revisor`.

- [ ] **Step 1: Update the tests first.**
  `DeParaControllerTest`: `aprovar_usaUsuarioLogadoComoRevisor` com `@WithMockUser(username="ana", roles="APROVADOR")`, `post("/api/de-para/1/aprovar")` com corpo `{}` e `csrf()` → `verify(service).aprovar(1L, "ana")`; `aprovar_ignoraRevisorDoCorpo` com corpo `{"revisor":"outro"}` → `verify(service).aprovar(1L, "ana")`; remover `aprovar_exigeRevisor`; ajustar os demais para a nova assinatura (corpo `{"motivo":"..."}`).
  `RevisaoViewControllerTest`: `aprovar_gravaUsuarioLogado`: `post("/ui/revisao/1/aprovar")` sem parâmetro `revisor`, usuário `ana` → `verify(service).aprovar(1L, "ana")`; idem rejeitar/revogar com `motivo`.
- [ ] **Step 2: Run** `./mvnw test -Dtest='DeParaControllerTest,RevisaoViewControllerTest'` — Expected: FAIL.
- [ ] **Step 3: Implement** as novas assinaturas; `Principal.getName()` é o revisor. Em `revisao.html`, remover o campo "Seu nome", os `<input type="hidden" name="revisor">`, o atributo `data-revisor` e a tag `<script ... revisao.js>`; apagar `revisao.js`.
- [ ] **Step 4: Run** os mesmos testes — Expected: PASS.
- [ ] **Step 5: Commit** `git commit -m "feat(login): revisor do De-Para vem do usuario logado"`

---

### Task 4: Cabeçalho com usuário, sair e CSRF no JS

**Files:**
- Create: `controller/UsuarioLogadoAdvice.java`
- Modify: `templates/layout.html`, `servicos.html`, `servico-form.html`, `servico-detalhe.html`, `busca.html`, `revisao.html`, `static/css/style.css`, `static/js/busca.js`
- Test: `controller/UsuarioLogadoAdviceTest.java` (ou asserções em `ServicoViewControllerTest`)

**Interfaces:**
- Produces: `@ControllerAdvice` `UsuarioLogadoAdvice` com `@ModelAttribute("usuarioLogado") String` (nome ou `null`) e `@ModelAttribute("ehAprovador") boolean`. Fragmento `layout :: usuario` (nome + botão Sair). Metas `_csrf` e `_csrf_header` no fragmento `layout :: head`.

- [ ] **Step 1: Write the failing tests** em `ServicoViewControllerTest` (`@WithMockUser(username="ana", roles="COMPRADOR")`): `GET /` contém `ana` e um formulário `action="/logout"`, e **não** contém o link `/ui/revisao`; com `roles="APROVADOR"` contém `/ui/revisao`. `layout.html` renderizado contém `<meta name="_csrf"`.
- [ ] **Step 2: Run** `./mvnw test -Dtest=ServicoViewControllerTest` — Expected: FAIL.
- [ ] **Step 3: Implement** o advice (lê `Authentication` do `SecurityContextHolder` ou recebe `Authentication`; `ehAprovador` = authority `ROLE_APROVADOR`), o fragmento `usuario` (`<form method="post" th:action="@{/logout}">` com botão "Sair") incluído no `<header>` de cada template, e o link "Revisão" com `th:if="${ehAprovador}"`. Não usar `sec:*`/`thymeleaf-extras-springsecurity` (compatibilidade com Security 7 não verificada).
- [ ] **Step 4: `busca.js`:** no `fetch(urlConfirmar, ...)` acrescentar o header cujo nome vem de `meta[name=_csrf_header]` e o valor de `meta[name=_csrf]`. Tratar `resp.status === 401` mostrando a mesma mensagem de falha já usada no `catch`.
- [ ] **Step 5: Run** `./mvnw test -Dtest='!DesafioGcGerdauAppApplicationTests'` — Expected: PASS.
- [ ] **Step 6: Commit** `git commit -m "feat(login): cabecalho com usuario e sair, CSRF no JS"`

---

### Task 5: Documentação e verificação no app real

**Files:**
- Modify: `README.md` (tabela de endpoints: perfil exigido; seção de login), `docs/GUIA_DE_USO.md` (como entrar, usuários e senhas de exemplo, "troque antes de usar de verdade", o que cada perfil faz), `CLAUDE.md` ("V1–V8" → "V1–V9" e uma linha sobre o login), `docs/REGRAS_DE_NEGOCIO.md` (revisor = usuário logado, se o documento citar o nome digitado)

- [ ] **Step 1:** Atualizar os documentos acima.
- [ ] **Step 2:** Subir o ambiente (`docker compose up -d`, `./mvnw spring-boot:run`) e verificar, anotando o resultado:
  - `curl -i localhost:8080/` → `302` para `/login`.
  - `curl -i localhost:8080/api/de-para/fila` → `401`.
  - Login como `comprador`: cadastro e busca funcionam; `/ui/revisao` → `403`; `/api/de-para/fila` → `403`.
  - Login como `aprovador`: `/ui/revisao` abre; aprovar um item grava `aprovador` em "Aprovado por" e no `dicionario_de_para_historico`.
  - Usuário inativo (`UPDATE usuarios SET ativo = FALSE`) não entra; senha errada mostra "Usuário ou senha inválidos.".
- [ ] **Step 3:** Rodar `./mvnw test` completo (com Postgres no ar) — Expected: tudo passa, incluindo `DesafioGcGerdauAppApplicationTests`.
- [ ] **Step 4: Commit** `git commit -m "docs(login): documenta perfis, login e usuarios de exemplo"`
