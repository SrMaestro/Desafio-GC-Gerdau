# Mapa do site

Todas as telas da aplicação (Thymeleaf), com rota, template, perfil de acesso e finalidade. Regras de negócio: `REGRAS_DE_NEGOCIO.md`. Uso passo a passo: `GUIA_DE_USO.md`.

Templates em `src/main/resources/templates`; estilos em `static/css`; scripts em `static/js`.

## Menu principal (`layout.html`)

| Item | Rota | Perfil |
|---|---|---|
| Serviços (marca "Catálogo de Serviços") | `/` | todos |
| Buscar NBS | `/ui/busca` | todos |
| Importar | `/ui/importacoes` | todos |
| Revisão | `/ui/revisao` | só `APROVADOR` |
| Consultas | `/ui/consultas` | só `APROVADOR` |
| Aprovações | `/ui/aprovacao` | só `APROVADOR` |

O `layout.html` também define o `head` (fontes Montserrat e IBM Plex Mono, `tema.js`, `style.css`, `app.js`, `busca.js`) e o `menu`, que recebe a seção ativa e tem o botão de tema claro/escuro.

## Acesso

| Rota | Template | Descrição |
|---|---|---|
| `/login` | `login.html` | Tela de entrada (pública). Sem login, as telas redirecionam para cá e a API responde `401`. |

## Serviços (catálogo)

| Rota | Template | Perfil | Descrição |
|---|---|---|---|
| `/` | `servicos.html` | todos | Lista paginada de serviços, com indicadores no topo e, numa caixa só, busca por texto, abas por área com contagem (que respeita a busca) e tabela com selo de condição. JS: `app.js` (colore as partes do código). Botões "Novo serviço" e "Buscar NBS". |
| `/ui/servicos/novo` | `servico-form.html` | todos | Formulário de cadastro. Mostra o código gerado, o NBS escolhido e os serviços parecidos; permite "Usar este código" ou confirmar o cadastro. JS: `form.js`, `similares.js`. |
| `POST /ui/servicos` | (redireciona) | todos | Cria o serviço e vai para o detalhe. |
| `POST /ui/servicos/reutilizar` | (redireciona) | todos | Registra a reutilização de um código existente. |
| `/ui/servicos/{id}` | `servico-detalhe.html` | todos | Detalhe de um serviço. |

## Busca NBS

| Rota | Template | Perfil | Descrição |
|---|---|---|---|
| `/ui/busca` | `busca.html` | todos | Dado um texto, acha o NBS (De-Para aprovado ou similaridade) e gera o código do pedido (Smart Key). JS: `busca.js`. |

## Importação em massa

| Rota | Template | Perfil | Descrição |
|---|---|---|---|
| `/ui/importacoes` | `importacoes.html` | todos | Envio de planilha `.csv`/`.xlsx` e lista "Meus lotes". JS: `comum.js`, `importacao.js`. |
| `/ui/importacoes/{id}` | `importacao-lote.html` | todos | Conferência do lote: corrigir linhas, decidir parecidas, enviar; mostra as decisões do aprovador. |

## Área do aprovador

| Rota | Template | Descrição |
|---|---|---|
| `/ui/revisao` | `revisao.html` | Mapeamentos do De-Para: pendentes e aprovados. Ações `POST /ui/revisao/{id}/aprovar`, `/rejeitar`, `/revogar` (redirecionam de volta). Estilo: `revisao.css`. |
| `/ui/consultas` | `consultas.html` | Histórico de tentativas de cadastro (`consultas_cadastro`). |
| `/ui/consultas/{id}` | `consulta-detalhe.html` | Uma consulta: texto, serviço relacionado e sugestões mostradas. |
| `/ui/aprovacao` | `aprovacao-lotes.html` | Lotes de importação enviados para aprovação. JS: `comum.js`, `aprovacao.js`. |
| `/ui/aprovacao/{id}` | `aprovacao-lote.html` | Linhas aguardando decisão e "Decidir de uma vez". |

## Fluxos entre telas

- **Cadastro:** Serviços → Novo serviço → (bloqueio, parecidos ou sucesso) → Detalhe do serviço.
- **Código NBS:** Serviços ou Novo serviço → Buscar NBS → volta ao formulário com o código.
- **Importação:** Importar → Lote (conferência) → envio → Aprovações → Lote (decisão do aprovador).
- **Revisão:** o comprador sugere um mapeamento na busca NBS; o aprovador decide em Revisão.

## API que as telas usam

Não são telas, mas o JS as consome: `/servicos`, `/api/de-para/**` (só `APROVADOR`, exceto `POST /api/de-para/confirmacoes`), `/api/importacoes/**` (`/aprovacao/**` só `APROVADOR`).

## CSS

- `style.css`: base compartilhada e tema escuro (bloco `:root[data-tema="dark"]` no fim).
- `importacao.css`: telas de importação e aprovação.
- `revisao.css`: tela de revisão.
