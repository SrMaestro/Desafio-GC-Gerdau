# Login com perfis Comprador e Aprovador — desenho

Data: 2026-10-24. Primeiro de dois projetos; o segundo é a importação em massa de serviços (CSV/Excel), que depende deste.

## Intenção e porquê

Hoje o sistema não tem login: qualquer pessoa usa todas as telas, e o De-Para guarda só um nome de "revisor" digitado à mão. A importação em massa exige que quem confere (comprador) e quem aprova (aprovador de governança) sejam pessoas distintas, com permissões distintas. Este projeto cria a identidade e a separação de papéis, sem mudar as regras de negócio existentes.

## Objetivos

- Usuário e senha guardados no próprio PostgreSQL da aplicação.
- Dois perfis: `COMPRADOR` e `APROVADOR`.
- Todas as telas e a API exigem login.
- O nome do revisor gravado no histórico do De-Para passa a ser o usuário logado, não texto livre.

## Fora do escopo

- Login corporativo (SSO) da Gerdau.
- Tela de cadastro, edição ou troca de senha de usuários; usuários nascem por migration.
- Recuperação de senha, bloqueio por tentativas, auditoria de login.
- Qualquer parte da importação em massa.

## O que seria um fracasso

- Uma rota do De-Para (tela ou API) acessível a um comprador.
- Quebrar o fluxo atual do cadastro (análise, confirmação, JS das telas) para quem está logado.
- Senha guardada em texto puro.

## Permissões (decisão do usuário)

| Perfil | Pode |
|---|---|
| COMPRADOR | cadastrar serviço, buscar serviços, buscar NBS, gerar SmartKey |
| APROVADOR | tudo do comprador, mais o De-Para (fila de revisão, aprovar, rejeitar, revogar) |

## Decisões do desenho

Decisões do usuário: perfis e permissões acima; usuário e senha na aplicação.

Minhas escolhas (o usuário pode revisar):

- Spring Security com tela de login em Thymeleaf no estilo das telas atuais, sessão HTTP.
- Hash BCrypt. Tabela `usuarios` por migration `V9`, com perfil e flag `ativo`. Usuários iniciais criados na própria migration (um comprador e um aprovador de exemplo; senhas de exemplo documentadas em `docs/GUIA_DE_USO.md` e a serem trocadas).
- Rotas do De-Para restritas ao `APROVADOR`: `/ui/revisao/**` e `/api/de-para/**`, **exceto** `POST /api/de-para/confirmacoes`, que o fluxo de cadastro do comprador usa. Verifiquei: `busca.js` chama `urlConfirmar` (a confirmação) na busca NBS, que o comprador usa; as demais rotas do De-Para são só da tela de revisão.
- O JS puro já chama a API na mesma origem, então usa a sessão do navegador. Requisições da API sem login devem responder 401 (não redirecionar para a página de login), e as de perfil insuficiente 403.
- Proteção CSRF: manter a padrão do Spring Security nas telas Thymeleaf; o construtor decide como o JS envia o token nas chamadas `POST`.
- Logout em todas as telas (no `layout.html`), mostrando o nome do usuário.

## Fica para o construtor

- Estrutura exata das classes (entidade, repository, `UserDetailsService`, configuração de segurança) seguindo o padrão em camadas existente.
- Ajustar os testes de controller (MVC test) para o novo requisito de login, e criar testes de acesso por perfil.
- Atualizar `README.md`, `docs/GUIA_DE_USO.md` e o `CLAUDE.md` (hoje listado como V1–V8).
