# Importação em massa de serviços (CSV/Excel) — desenho

Data: 2026-10-24. Segundo de dois projetos; depende do login com perfis (`docs/superpowers/specs/2026-10-24-login-perfis-design.md`, branch `feat/login-perfis`).

## Intenção e porquê

Hoje cada comprador cria os próprios códigos de serviço, um a um, e a base acumulou milhares de duplicados. O Desafio GC quer um filtro que compare cada código novo com os existentes. Falta o caminho para quem chega com uma **planilha de dados acumulados e não padronizados**, com milhares de linhas: cadastrar uma a uma é inviável, e aceitar tudo recriaria o problema.

A importação em massa aplica o mesmo filtro do cadastro a cada linha da planilha, separa o que precisa de olho humano e só deixa algo entrar na base depois de um aprovador de governança decidir.

## Objetivos

- O comprador envia um arquivo CSV ou Excel (.xlsx) com os campos do cadastro e recebe a classificação de cada linha.
- Nenhuma linha entra na base (serviços ou sinônimos) sem aprovação de um aprovador.
- O que o aprovador aprova como "este texto sujo é aquele serviço existente" passa a ser reconhecido automaticamente no futuro, na importação e no cadastro individual.
- Cada decisão (comprador e aprovador) fica registrada com usuário e data.
- Funciona com milhares de linhas por arquivo sem travar a tela.

## Fora do escopo

- Notificação por e-mail ou outro canal: o aprovador vê os lotes enviados numa tela.
- Revogar um sinônimo aprovado, editar ou excluir serviços existentes, tela de administração.
- Limpeza/saneamento da base inteira (é a trilha IA & Dados).
- Criar usuários ou perfis (vem do projeto de login).
- Ligar automaticamente, sem aprovação, qualquer linha a um serviço existente.

## O que seria um fracasso

- Uma linha entrar na base sem aprovação.
- Uma linha sumir sem rastro: toda linha do arquivo termina em algum estado visível (classificada, ignorada com motivo, com erro, aprovada, rejeitada).
- O comprador esperar a página inteira carregar para ver o resultado de milhares de linhas.
- Aprovar em bloco sem como saber o que foi aprovado.

## Decisões do usuário

- Arquivo com colunas já nos campos do cadastro.
- Três situações por linha, mais "com erro":
  - **Já existia** (código gerado idêntico a um existente): a linha é ignorada e aparece no resumo.
  - **Parecida** (similaridade ≥ 0,4 com algum serviço existente, **inclusive** o que o cadastro hoje bloquearia: similaridade ≥ 0,8, mesma condição e mesmo CNAE): vai para conferência do comprador, que vê o serviço parecido e a similaridade.
  - **Sem parecido** (nenhum serviço com similaridade ≥ 0,4): vira um código novo proposto.
  - **Com erro** (condição inexistente no catálogo, CNAE inválido, código gerado acima de 40 caracteres etc.): entra na fila para o comprador corrigir na tela, sem recusar o arquivo.
- Fluxo: importa → o comprador confere e corrige → o comprador envia ao aprovador só o que está pronto (linhas com erro podem ficar para um envio posterior) → o aprovador decide.
- **Todas** as linhas enviadas passam pelo aprovador, inclusive códigos novos.
- Aprovar uma linha parecida significa: "este texto sujo aponta para o serviço existente", sem criar serviço novo. Aprovar uma linha nova cria o serviço.
- O aprovador pode aprovar em bloco (por exemplo, todas as novas, ou todas as parecidas com similaridade mínima escolhida).
- O que foi aprovado como "texto sujo = serviço existente" é reconhecido sozinho depois; no cadastro individual, o sistema **bloqueia** e mostra o código existente.
- Arquivos com **milhares de linhas**.
- Tela do comprador no layout B, escolhido no mockup: quatro blocos na mesma tela, com erros e parecidas em cima (com erro, parecidas, novas, já existiam), botão "Enviar ao aprovador" que envia o que está pronto.
- Login com usuário e senha na aplicação; perfis `COMPRADOR` e `APROVADOR` (projeto anterior).

## Decisões minhas (o usuário pode rever)

- **Estrutura nova e paralela ao De-Para.** O De-Para existente liga texto sujo a um **código NBS**; aqui a ligação é a um **serviço**. Criar uma tabela de sinônimos de serviço (texto normalizado + condição + CNAE → serviço) com o mesmo ciclo `PENDENTE → APROVADO / REJEITADO` e histórico próprio, sem alterar o De-Para.
- **Chave do reconhecimento:** texto normalizado (minúsculo, sem acento, espaços colapsados) **mais condição e CNAE**. O mesmo texto com outra condição ou CNAE é outro serviço.
- **Reconhecimento:** antes da similaridade, a análise (importação e `ServicoService.analisar`) procura um sinônimo `APROVADO` com a mesma chave; se achar, o resultado é "já existia" na importação e bloqueio com o código existente no cadastro individual.
- **Formato do arquivo:** CSV em UTF-8 com `;` ou `,` (detectar), ou .xlsx (primeira planilha). Cabeçalho obrigatório, sem diferenciar maiúsculas nem acentos. Colunas: `area_servico`, `texto_breve`, `abreviatura`, `complemento` (opcional), `condicao`, `cnae`, `codigo_nbs` (opcional), `criador_codigo` (opcional). `condicao` aceita a descrição ou o sufixo do catálogo `cat_horarios`.
- **Limite de tamanho:** o construtor define e documenta um limite de linhas/megabytes que comporte milhares de linhas; arquivo acima do limite é recusado com mensagem clara.
- **Processamento em segundo plano** dentro da própria aplicação (sem fila externa nem novo serviço de infraestrutura), com progresso persistido no banco e consultado pela tela; o comprador pode sair e voltar.
- **Conferência do comprador na linha parecida:** ele escolhe entre "é o mesmo serviço" (ligar ao parecido), "é outro serviço" (tratar como novo) ou descartar. Pode filtrar e decidir em bloco por faixa de similaridade.
- **Visibilidade:** o comprador vê só os próprios lotes; o aprovador vê todos os lotes enviados.
- **Repetidas:** linha cujo código gerado já apareceu antes no mesmo arquivo, ou já está pendente em outro lote enviado, é sinalizada como repetida e não vira segundo código novo.
- **Aprovação revalida:** ao aprovar uma linha nova, o sistema confere de novo se o código ainda é livre. Se alguém criou o mesmo código no intervalo, a linha vai para um estado de conflito visível e não é gravada nem descartada em silêncio. A restrição de unicidade do banco continua a garantia final.
- **Linha rejeitada pelo aprovador** fica rejeitada, com motivo, e não volta sozinha; para tentar de novo, o comprador envia a linha corrigida num novo lote.
- **`criador_codigo`** em branco assume o usuário logado que enviou o lote.
- **O aprovador também pode importar e conferir** (ele tem tudo o que o comprador tem); a aprovação das próprias linhas por quem as enviou é permitida nesta versão.
- **Reaproveitar** `ServicoService` (geração de código, `buscarSimilares`, limiares) para que a importação e o cadastro nunca discordem das regras.

## Constraints

- Spring Boot 4.1 / Java 21, Thymeleaf, JS puro, Flyway, PostgreSQL; migrations novas a partir da `V10`, sem editar as anteriores; código e textos em português.
- Limiares e regras de negócio como em `docs/REGRAS_DE_NEGOCIO.md` (0,8 bloqueio, 0,4 confirmação, 40 caracteres); não duplicar os números.
- Segurança do projeto de login: endpoints de aprovação só para `APROVADOR`, `POST` com CSRF, API sem login responde 401.
- Documentação (`README.md`, `docs/GUIA_DE_USO.md`, `docs/REGRAS_DE_NEGOCIO.md`, `CLAUDE.md`) atualizada junto.

## Fica para o construtor

- Modelo de dados e nomes de tabelas/classes (lote, linha do lote, sinônimo de serviço, histórico), índices, e o desempenho da comparação por linha com milhares de linhas (medir com um arquivo grande e relatar).
- Biblioteca de leitura de .xlsx/CSV (escolher uma mantida e compatível com Java 21).
- Telas: seguir o estilo atual (`layout.html`, `style.css`) e o mockup B para o comprador; a tela do aprovador (lista de lotes, filtros, ações em bloco) o construtor desenha no mesmo estilo.
- Mensagens de erro por linha, texto do resumo e o formato de um modelo de planilha para download (se o construtor julgar útil).
