# Redesign das telas — desenho

Data: 2026-10-25.

## Intenção e porquê

As telas funcionam, mas parecem simples demais: cabeçalho azul com links, tabelas e formulários sem hierarquia visual. O usuário quer "cara de site profissional". O TAP pede que a tela seja fácil para compradores e que o material seja claro para quem não participou do projeto; uma interface mais cuidada ajuda nos dois pontos. Este trabalho muda a aparência e a navegação. Não muda regra de negócio.

## Objetivos

- As 10 telas passam a seguir um único estilo: login, serviços (lista), formulário de serviço, detalhe do serviço, busca NBS, importações (lista), lote de importação, aprovação (lista), lote de aprovação e revisão do De-Para.
- Direção visual escolhida pelo usuário ("opção A", corporativa com menu lateral): menu lateral fixo azul, área de conteúdo clara, cantos arredondados, selos coloridos de situação, mais espaço entre elementos. Mantém o azul (`#003b71`) e o laranja (`#e0561b`) atuais.
- Menu lateral com: Serviços, Buscar NBS, Importar e, só para o aprovador, Aprovações. Também a Revisão do De-Para, só para o aprovador (o cabeçalho atual já tem esse link, com a mesma regra). O usuário logado e o botão Sair ficam no rodapé do menu.
- Três cards de resumo no topo da lista de serviços, com números reais:
  - **Serviços cadastrados**: total de linhas da tabela de serviços.
  - **Mapeamentos pendentes**: De-Para com status `PENDENTE`.
  - **Linhas aguardando aprovação**: linhas de importação na etapa `ENVIADA`; este card aparece só para o aprovador.
- Funciona em celular (a lista já vira blocos abaixo de 720 px; o menu lateral deve recolher).

## Fora do escopo

- Contador de duplicados bloqueados (exigiria tabela nova e gravar cada tentativa barrada). Fica como ideia separada.
- Qualquer mudança em regra de negócio, rotas, permissões ou migrations.
- Framework de front-end (React, Bootstrap, Tailwind) ou etapa de build.
- Tema escuro, troca de idioma, animações elaboradas.

## O que seria um fracasso

- Quebrar o JS atual (`app.js`, `busca.js`, `form.js`, `importacao.js`, `aprovacao.js`, `similares.js`, `comum.js`) por mudar `id`s, atributos `data-*` ou nomes de classe que eles usam.
- Os testes existentes falharem por causa da mudança.
- Perder acessibilidade que já existe (foco visível, `aria-live`, rótulos).
- Um comprador ver algo exclusivo do aprovador (o link Aprovações, o card de aprovação).
- Os cards deixarem a tela de serviços lenta ou darem erro quando a contagem falhar.

## Decisões

Decisões do usuário:
- Direção visual A (menu lateral).
- Todas as telas de uma vez.
- Três cards com números reais; bloqueios ficam para depois.

Minhas escolhas (o usuário pode revisar):
- Continua em Thymeleaf e CSS puro. A mudança se concentra em `layout.html` e `style.css` (e `importacao.css`, `revisao.css`), com ajustes nos templates.
- Os totais dos cards vêm de contagens no servidor, entregues ao modelo da tela de serviços, sem endpoint novo de API, já que a tela é renderizada no servidor. *(escolha minha; a conversa falou em "endpoint", mas a tela é Thymeleaf.)*
- A tela de login não leva o menu lateral; ganha só o mesmo visual (cartão central, cores da marca).
- Sem fonte externa nem biblioteca de ícones; ícones, se houver, em SVG inline ou texto, para não depender de rede.

## O que fica para quem for construir

- Estrutura exata do HTML do menu e como compartilhá-lo entre as telas (fragmento no `layout.html`).
- Nomes de classes novas, espaçamentos, sombras, tamanho das fontes, aparência dos selos.
- Onde fica o link da Revisão do De-Para no menu.
- Como recolher o menu em telas pequenas.
- Como fazer as contagens (métodos de repository) e onde expô-las.
- Testes: ajustar os de controller que verificam o modelo, e cobrir as contagens novas.
- Atualizar `docs/GUIA_DE_USO.md` com a nova navegação.
