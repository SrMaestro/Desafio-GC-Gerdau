# Guia de uso

Para quem usa o sistema no dia a dia. Não é preciso conhecer o projeto.

## 0. Entrar

Abra o sistema e entre com seu usuário e senha. Há dois perfis: **comprador** (cadastra, busca e importa planilhas) e **aprovador** (faz o mesmo e também aprova planilhas e revisa o De-Para). Use **Sair**, no canto do cabeçalho, ao terminar.

Usuários de exemplo, criados na instalação: `comprador` / `comprador123` e `aprovador` / `aprovador123`. **Troque essas senhas antes de usar de verdade.** Ainda não há tela para isso: a equipe técnica gera um novo hash BCrypt e atualiza a coluna `senha_hash` da tabela `usuarios`; para desativar alguém, `ativo = FALSE`.

## 1. Cadastrar um novo serviço (comprador)

Abra **Novo serviço** (`/ui/servicos/novo`) e preencha:

| Campo | Regra |
|-------|-------|
| Área de serviço | Obrigatório |
| Texto breve | Obrigatório. Descreva o serviço; é ele que o sistema compara com os existentes |
| Abreviatura | Obrigatório, até 10 letras ou números |
| Complemento | Opcional, começa com `_` (ex.: `_CONCRE`), até 15 caracteres |
| Condição | Escolha na lista (hora normal, hora extra, noturno...) |
| CNAE | Exatamente 7 dígitos |
| Código NBS | Opcional. Use a busca para escolher |

O código é montado assim: `ABREVIATURA` + `COMPLEMENTO` + `_` + `SUFIXO DA CONDIÇÃO` + `CNAE`. Exemplo: `CONC_CONCRE_HN4399103`. **O código não pode passar de 40 caracteres** (limite do SAP); se passar, o cadastro é recusado e é preciso encurtar a abreviatura ou o complemento.

### O que o sistema responde

| Situação | O que acontece |
|----------|----------------|
| Código já existe, ou texto muito parecido (80% ou mais) com a mesma condição e o mesmo CNAE | **Bloqueado.** Mostra o serviço existente: use-o em vez de criar outro |
| Texto parecido (40% a 80%) | **Pede confirmação.** Mostra os similares; confirme só se for outro serviço |
| Nada parecido | Cadastra |

## 2. Achar o código NBS (busca)

Em **Busca** (`/ui/busca`), digite o serviço, mesmo com erro de digitação (ex.: `instalaa eletrica`).

- **Resultado exato**: o nome já foi revisado e aprovado. Use o código mostrado.
- **Sugestões**: opções parecidas da NBS, ainda sem revisão. Escolha a correta.

Ao escolher um NBS e um horário, a tela mostra a **Smart Key** (ex.: `1.01.01-HN`) para usar no pedido de compra. A escolha também é enviada para revisão, e assim o sistema aprende.

## 3. Importar vários serviços de uma planilha (comprador)

Use quando você tem uma planilha com muitos serviços (até **20.000 linhas** e **10 MB**). O sistema aplica a cada linha o mesmo filtro do cadastro individual. **Nada entra na base sem a aprovação de um aprovador**, nem os códigos novos.

### A planilha

Arquivo `.csv` (UTF-8, separado por `;` ou `,`; um CSV salvo pelo Excel em português também é aceito) ou `.xlsx` (só a primeira aba). A primeira linha é o cabeçalho, com estes nomes (maiúsculas e acentos não importam; a ordem das colunas é livre):

| Coluna | Regra |
|--------|-------|
| `area_servico` | Obrigatória, até 50 caracteres |
| `texto_breve` | Obrigatória, até 255 caracteres. É o texto que o sistema compara |
| `abreviatura` | Obrigatória, até 10 letras ou números |
| `complemento` | Opcional, começa com `_`, até 15 caracteres |
| `condicao` | Obrigatória. A descrição da condição (ex.: `Hora normal`) ou o sufixo (ex.: `HN`); só condições ativas |
| `cnae` | Obrigatória, 7 dígitos |
| `codigo_nbs` | Opcional; se informado, precisa existir na NBS |
| `criador_codigo` | Opcional; em branco vale o seu usuário |

Exemplo (`servicos.csv`):

```csv
area_servico;texto_breve;abreviatura;complemento;condicao;cnae
Civil;Concretagem de fundação;CONC;_CONCRE;Hora normal;4399103
Civil;Pintura de tanque de armazenamento;PINT;_TANQUE;HES;4330404
Eletromecânica;Manutenção de motor elétrico;MANUT;_MOTORE;Condição que não existe;3313000
```

O arquivo todo só é recusado se não for `.csv`/`.xlsx`, estiver vazio, passar do limite ou faltar uma coluna obrigatória (a mensagem diz qual). Problemas em linhas isoladas não recusam o arquivo: a linha aparece como "com erro".

### Passo a passo

1. No menu, clique em **Importar** e envie o arquivo. A barra de progresso mostra o andamento (5.000 linhas levam cerca de 2 minutos, medido com cerca de 20 mil serviços cadastrados); você pode sair e voltar depois em "Meus lotes". Se a aplicação for reiniciada no meio, o lote aparece como falho: envie o arquivo de novo.
2. A tela mostra o resultado em cinco situações, em blocos: **com erro** e **parecidas** primeiro (precisam de você), depois **novas**, **já existiam** e **repetidas** (recolhidos; abra para ver).

| Situação | Significa | O que fazer |
|----------|-----------|-------------|
| Com erro | Campo faltando ou inválido, condição inexistente, código acima de 40 caracteres | Corrigir |
| Parecida | Texto com 40% ou mais de similaridade com um serviço cadastrado (inclui o que o cadastro individual bloquearia) | Decidir |
| Nova | Nenhum serviço parecido | Nada; segue para aprovação |
| Já existia | O código já está cadastrado, ou o texto é um sinônimo já aprovado de um serviço | Nada; é ignorada |
| Repetida | O mesmo código aparece antes no arquivo, ou já foi enviado para aprovação em outro lote | Nada; é ignorada |

3. Em **com erro**, clique em "Corrigir", ajuste os campos e salve; a linha é classificada de novo (pode virar nova, parecida, já existia...). Em **parecidas**, veja o serviço já cadastrado e a similaridade e escolha "É o mesmo serviço" (o texto passa a apontar para o serviço existente, sem criar outro), "É outro serviço" (será proposto como novo) ou "Descartar". Para muitas de uma vez, informe a similaridade mínima (ex.: 90%) e use os botões de decisão em bloco; o sistema pede confirmação.
4. **Enviar ao aprovador** envia as linhas prontas: as novas e as parecidas já decididas. Linhas com erro ou ainda sem decisão não impedem o envio e podem ser tratadas e enviadas depois. A tela informa quantas foram enviadas e quantas ainda precisam de atenção.

5. Depois que o aprovador decide, volte ao lote: o quadro **Decisões do aprovador** mostra quantas linhas foram aprovadas, rejeitadas, estão em conflito e ainda aguardam. Abra **Rejeitadas pelo aprovador** ou **Em conflito na aprovação** para ver cada linha com o motivo (quem rejeitou e por quê, ou por que não pôde virar código). O motivo também aparece na própria linha nos outros blocos (por exemplo, em "parecidas" com "Mostrar também as já decididas").

Cada linha passa por estas etapas: *em conferência* (precisa de você), *pronta* (decidida, ainda não enviada), *enviada* (com o aprovador), *aprovada*, *rejeitada* ou *em conflito* (ver abaixo). Linhas ignoradas (já existia, repetida) ou descartadas por você não vão adiante. Você vê só os seus lotes. Uma linha rejeitada ou em conflito não pode mais ser alterada nesse lote nem volta sozinha: leia o motivo na tela, corrija a linha na planilha e envie-a num novo lote.

## 4. Aprovar planilhas enviadas (aprovador)

Só o aprovador vê **Aprovações** no menu (`/ui/aprovacao`).

1. A lista mostra as planilhas com linhas esperando decisão: arquivo, quem enviou, quantas linhas aguardam e quantas estão em conflito. Clique no arquivo para abrir.
2. Cada linha mostra o que você precisa para decidir: o código proposto (nova) ou o texto, o serviço parecido já cadastrado e a similaridade (parecida), mais a decisão do comprador, área, condição, CNAE e quem conferiu. Use **Aprovar** ou **Rejeitar** (a rejeição pede um motivo).
3. Para decidir muitas de uma vez, escolha o filtro (todas, novas ou parecidas, com similaridade mínima) e use **Aprovar todas** ou **Rejeitar todas**. A tela diz quantas linhas serão afetadas, em todas as páginas, e pede confirmação. Sem filtro, rejeitar em bloco rejeita todas as linhas aguardando do lote; o motivo é obrigatório.
4. O que acontece ao aprovar: linha **nova** (ou parecida decidida como "outro serviço") cria o serviço com o código proposto; linha **parecida** decidida como "mesmo serviço" não cria serviço, grava o texto como **sinônimo** do serviço existente. Dali em diante, esse texto (com a mesma condição e o mesmo CNAE) é reconhecido sozinho: a importação o marca como "já existia" e o cadastro individual o **bloqueia** mostrando o código existente.
5. Depois da aprovação em bloco a tela mostra quantas foram aprovadas, quantas foram para conflito e quantas **não puderam ser processadas agora**; essas continuam aguardando e podem ser aprovadas de novo.
6. O bloco **em conflito** (recolhido) lista as linhas que não puderam virar código, com o motivo (por exemplo, alguém cadastrou o mesmo código depois da importação). Elas ficam fora das ações em bloco e não têm botões; o comprador deve corrigir e enviar de novo num novo lote.

## 5. Revisar sugestões (aprovador)

Só o aprovador vê **Revisão**. Em **Revisão** (`/ui/revisao`) ficam os mapeamentos pendentes, do mais confirmado para o menos.

- **Aprovar**: passa a valer como busca exata. Os outros pendentes do mesmo nome são rejeitados.
- **Rejeitar**: exige motivo.
- **Revogar**: tira um aprovado de circulação, com motivo. O histórico é mantido.

Cada mudança fica registrada com o usuário logado, quando e por quê.

## 6. Implantar (equipe técnica)

1. Instale o **JDK 21** e o Docker.
2. `cp env.example .env` e preencha as senhas.
3. `docker compose up -d`
4. `./mvnw spring-boot:run`
5. Abra `http://localhost:8080`.

O banco é criado e populado pelo Flyway na primeira execução. Erros comuns estão no [README](../README.md#problemas-comuns).

## 7. Ajustar as réguas (equipe técnica)

| Parâmetro | Valor | Onde |
|-----------|-------|------|
| Limiar de sugestão de serviços parecidos | 0,4 (**deve ficar acima do limiar do `pg_trgm`, 0,3**; ver abaixo) | `ServicoService.LIMIAR_SUGESTAO` |
| Limiar de bloqueio | 0,8 | `ServicoService.LIMIAR_BLOQUEIO` |
| Limiar de sugestão de NBS | 0,2 | `BuscaNbsService.LIMIAR_SUGESTAO` |
| Tamanho máximo do código | 40 | `ServicoService.TAMANHO_MAX_CODIGO` |
| Máximo de linhas por planilha | 20.000 | `LeitorPlanilha.MAX_LINHAS` |
| Tamanho máximo da planilha | 10 MB | `spring.servlet.multipart.max-file-size` (`application.properties`) |

A busca de serviços parecidos usa o operador `%` do `pg_trgm` para aproveitar o índice, e ele corta pelo limiar do próprio `pg_trgm` (`show_limit()`, padrão 0,3). Por isso o limiar de sugestão precisa continuar acima dele: para baixá-lo a 0,3 ou menos, baixe junto o do `pg_trgm`, ou parecidos deixam de aparecer em silêncio. O teste `LimiarSimilaridadeTest` avisa se isso acontecer.
