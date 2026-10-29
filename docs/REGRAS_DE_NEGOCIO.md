# Regras de negócio

## Fluxo de entrada de código

```
cadastro -> gera código -> compara (pg_trgm) -> bloqueia | pede confirmação | cadastra
```

1. O código é gerado em maiúsculas: `abreviatura + complemento + "_" + sufixo da condição + CNAE`.
2. Bloqueio: o código já existe, **ou** há serviço com similaridade de texto ≥ 0,8, mesma condição e mesmo CNAE.
   A busca desse serviço é própria e filtra a condição e o CNAE no banco (`ServicoRepository.buscarDuplicado`); ela não depende dos 5 parecidos mostrados como sugestão. Isso importa porque o mesmo texto costuma existir em várias das 14 condições, e os 5 mais parecidos podem ser todos de outras condições. Quando bloqueia, o serviço duplicado aparece em primeiro lugar nas sugestões, mesmo que não estivesse entre os 5.
3. Confirmação: há serviços com similaridade ≥ 0,4 que não bloqueiam. O cadastro só segue com `confirmar=true`.
   O 0,4 precisa ficar acima do limiar de similaridade do `pg_trgm` (padrão 0,3, visto em `SELECT show_limit()`), pois a consulta usa o operador `%` para aproveitar o índice e ele já corta nesse limiar. Com `LIMIAR_SUGESTAO` em 0,3 ou menos, a busca perde em silêncio os parecidos abaixo de 0,3; o teste `LimiarSimilaridadeTest` falha para avisar.
4. O código gerado não pode passar de 40 caracteres.
5. Se dois usuários criarem o mesmo código ao mesmo tempo, a restrição de unicidade do banco recusa o segundo, e ele recebe a resposta de bloqueio.
6. Se informado, o código NBS precisa existir em `cat_nbs`.
7. Antes da similaridade, o sistema procura um **sinônimo aprovado** (ver "Importação em massa"): se o texto, a condição e o CNAE já foram ligados a um serviço, o cadastro é bloqueado e mostra o código desse serviço.

## Registro das consultas de cadastro

Cada tentativa de cadastro individual (tela ou API) grava uma linha em `consultas_cadastro`, com o usuário logado, o texto, a condição e o CNAE consultados, o código gerado, as sugestões mostradas (`consulta_cadastro_sugestoes`) e, quando houver, o serviço resultante.

| Situação | `resultado` | `decisao` |
|----------|-------------|-----------|
| Duplicado (inclui sinônimo aprovado e corrida de unicidade) | `BLOQUEADO` | — |
| Há parecidos e não veio `confirmar` | `REQUER_CONFIRMACAO` | — |
| Cadastrado sem parecidos | `CADASTRADO` | `CADASTROU_NOVO` |
| Cadastrado com parecidos e `confirmar=true` | `CADASTRADO` | `CONFIRMOU_MESMO_ASSIM` |
| Clique em "Usar este código" numa sugestão | `SUGERIDO` | `REUTILIZOU_EXISTENTE` |

- Erros de validação (código acima de 40 caracteres, NBS ou condição desconhecidos) não geram registro.
- O registro é melhor esforço: se a gravação falhar, o cadastro segue normalmente e a falha vai para o log.
- A importação em massa não usa esta tabela; ela tem o próprio rastro (quem conferiu, quem decidiu, motivo).
- O aprovador consulta o histórico em `/ui/consultas` (últimas 200).

### Indicadores

A tela inicial mostra quatro indicadores, calculados em tempo real sobre `consultas_cadastro` (contagens simples, sem cache):

| Indicador | Como é contado |
|-----------|----------------|
| Duplicados barrados | `resultado = BLOQUEADO` |
| Códigos reaproveitados | `decisao = REUTILIZOU_EXISTENTE` |
| Sem correspondência (código novo) | `decisao = CADASTROU_NOVO`, isto é, cadastrado sem nenhum parecido |
| Duplicidade evitada | (barrados + reaproveitados) ÷ total de tentativas, arredondado para inteiro; sem tentativas, "—" |

Uma tentativa bloqueada que o comprador repete conta de novo: o indicador mede tentativas, não pessoas nem serviços distintos. A importação em massa não entra nestes números.

## Busca de NBS (fall-back)

1. **Exata**: o nome normalizado (minúsculo, sem acento, espaços colapsados) tem mapeamento `APROVADO` no De-Para.
2. **Similaridade**: `pg_trgm` sobre `cat_nbs`, limiar 0,2, até 5 sugestões. O texto padrão repetido nas descrições da NBS é ignorado.

## Smart Key

`<código NBS>-<sufixo do horário>`, só gerada se ambos existem nos catálogos (`cat_nbs`, `cat_horarios`).

## Ciclo do De-Para

| Status | Significado |
|--------|-------------|
| `PENDENTE` | Confirmado por usuário, aguardando o aprovador |
| `APROVADO` | Vale como busca exata. Só um por nome |
| `REJEITADO` | Recusado (motivo obrigatório, ou outro foi aprovado) |
| `REVOGADO` | Aprovado que saiu de circulação (motivo obrigatório) |

- Confirmar um par que já está pendente soma uma confirmação.
- Confirmar um nome que já tem aprovado é ignorado: a confirmação nunca altera um aprovado.
- Toda mudança de status grava em `dicionario_de_para_historico` (quem, quando, motivo).
- A origem do mapeamento é `LOTE` (carga em lote), `USUARIO` ou `REVISOR`.

## Importação em massa

Para planilhas (`.csv`/`.xlsx`) com muitas linhas. Cada linha passa pelas mesmas regras do cadastro, e **nenhuma linha entra na base sem a aprovação de um aprovador**, nem os códigos novos. Uma linha nunca some: termina sempre em uma situação e uma etapa visíveis.

### Limites

Até 20.000 linhas de dados e 10 MB por arquivo; acima disso o arquivo é recusado com mensagem. O processamento é em segundo plano, uma importação por vez (as outras esperam numa fila de até 20); o progresso é gravado no lote (`PROCESSANDO`, `CONCLUIDO`, `FALHOU`). Lote que estava processando quando a aplicação caiu vira `FALHOU` na subida seguinte, mantendo as linhas já gravadas. Uma queda do banco no meio também encerra o lote como `FALHOU`.

### Situação de cada linha (na ordem em que é decidida)

| Situação | Critério |
|----------|----------|
| `ERRO` | Campo obrigatório vazio ou acima do tamanho, abreviatura ou complemento fora do padrão, CNAE sem 7 dígitos (aceita `4399103.0`, como o Excel entrega), condição inexistente ou inativa, NBS inexistente, código gerado acima de 40 caracteres |
| `REPETIDA` | O código gerado já apareceu antes no mesmo arquivo |
| `EXISTENTE` | O texto é um sinônimo aprovado (mesma condição e CNAE), ou o código gerado já está cadastrado |
| `PARECIDA` | Há serviço com similaridade de texto ≥ 0,4 (o mais parecido é guardado com a similaridade). **Inclui** o que o cadastro individual bloquearia (≥ 0,8, mesma condição e mesmo CNAE): na importação nada é bloqueado de saída, tudo que se parece vai para decisão humana |
| `NOVA` | Nenhum serviço com similaridade ≥ 0,4 |

Uma linha `NOVA` ou `PARECIDA` cujo código já foi **enviado** para aprovação em outro lote vira `REPETIDA`. Linhas ainda em conferência de outro lote não contam. Os limiares são os do cadastro (`ServicoService`).

### Etapa de cada linha

| Etapa | Significado |
|-------|-------------|
| `EM_CONFERENCIA` | Precisa do comprador: erro a corrigir ou parecida sem decisão |
| `PRONTA` | Nova, ou parecida já decidida; ainda não enviada |
| `ENVIADA` | Com o aprovador |
| `APROVADA` / `REJEITADA` | Decidida pelo aprovador (rejeição exige motivo; não volta sozinha) |
| `CONFLITO` | Na aprovação, não pôde ser gravada; o motivo fica na linha. Estado final |
| `IGNORADA` | `EXISTENTE` e `REPETIDA`, que não vão adiante |
| `DESCARTADA` | O comprador descartou |

### Conferência do comprador

- Corrigir uma linha a reclassifica do zero, inclusive a repetição dentro do lote. Campos em branco valem como não informados (iguais a ausentes). Só se mexe em linhas em conferência ou prontas.
- Toda decisão do comprador (corrigir, decidir, decidir em bloco, descartar) grava quem (`conferido_por`) e quando (`conferido_em`).
- Linhas `REJEITADA` e `CONFLITO` aparecem na tela do comprador com o motivo; não podem ser alteradas no lote: o caminho é corrigir na planilha e enviar num novo lote.
- Em linha `PARECIDA` o comprador decide: `MESMO_SERVICO` (liga o texto ao serviço parecido) ou `OUTRO_SERVICO` (tratar como novo). Pode decidir em bloco por similaridade mínima, e mudar a decisão enquanto a linha não foi enviada.
- Enviar ao aprovador leva as linhas `PRONTA`; erros e parecidas sem decisão ficam para um envio posterior.

### Aprovação

- Só linhas `ENVIADA` são decididas, uma a uma ou em bloco (por situação e similaridade mínima). Cada linha é uma transação própria: um conflito ou erro numa linha não desfaz as outras.
- `NOVA`, ou `PARECIDA` com `OUTRO_SERVICO`: cria o serviço com o código gerado. Antes, o sistema confere de novo que o código ainda é livre e que continua igual ao do catálogo atual; se não, a linha vai para `CONFLITO` com o motivo. A restrição de unicidade do banco continua sendo a garantia final.
- Só para `NOVA`: antes de criar, aplica também a regra de bloqueio do cadastro individual contra a base atual (similaridade ≥ 0,8, mesma condição e mesmo CNAE). Se bloquear, `CONFLITO` com "Parecido com X (similaridade N%) já na base; verifique se é duplicado". Isso pega duas linhas do mesmo lote com o mesmo texto e códigos diferentes (a primeira aprovada bloqueia a segunda). `PARECIDA` com `OUTRO_SERVICO` não passa por essa regra: o comprador já viu o parecido e decidiu.
- `PARECIDA` com `MESMO_SERVICO`: não cria serviço; grava o **sinônimo** (texto normalizado + condição + CNAE → serviço). O serviço parecido precisa ter a mesma condição (pela descrição do catálogo) e o mesmo CNAE da linha; senão, `CONFLITO` com "O serviço parecido (X) é de outra condição/CNAE; use 'é outro serviço' ou corrija a linha". Se o texto já estava ligado a outro serviço, é `CONFLITO`; se ao mesmo serviço, é sucesso.
- Erro técnico (banco instável, por exemplo) não vira conflito: a linha continua `ENVIADA`, é contada como falha no resumo da aprovação e pode ser aprovada de novo.
- Quem aprova é sempre o usuário logado. O aprovador pode aprovar linhas que ele mesmo enviou.

### Sinônimos

Tabela `servico_sinonimo`: texto normalizado (minúsculo, sem acento, espaços colapsados), condição e CNAE ligados a um serviço, com quem aprovou, quando e de qual linha. O mesmo texto com outra condição ou outro CNAE é outro serviço. Um sinônimo é reconhecido na importação (linha `EXISTENTE`) e no cadastro individual (bloqueio, mesmo com `confirmar=true`). Nesta versão não há tela para revogar sinônimos.

### Quem vê o quê

O comprador vê só os próprios lotes (de outro usuário, a resposta é 404). O aprovador vê, de lotes alheios, apenas as linhas `ENVIADA` e `CONFLITO`; nos próprios, tudo. As telas de aprovação e a API `/api/importacoes/aprovacao/**` são só do `APROVADOR` (o comprador recebe 403).
