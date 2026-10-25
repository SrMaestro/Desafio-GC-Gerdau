# Guia de uso

Para quem usa o sistema no dia a dia. Não é preciso conhecer o projeto.

## 0. Entrar

Abra o sistema e entre com seu usuário e senha. Há dois perfis: **comprador** (cadastra e busca) e **aprovador** (faz o mesmo e também revisa o De-Para). Use **Sair**, no canto do cabeçalho, ao terminar.

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

## 2b. Importar vários serviços de uma planilha (comprador)

1. No menu, clique em **Importar** e envie um arquivo `.xlsx` ou `.csv` (até 10 MB). A barra de progresso mostra o andamento; você pode sair e voltar depois em "Meus lotes".
2. A tela mostra os resultados em blocos: **com erro** e **parecidas** primeiro (precisam de você), depois **novas**, **já existiam** e **repetidas** (recolhidos; abra para ver).
3. Em **com erro**, clique em "Corrigir", ajuste os campos e salve. Em **parecidas**, veja o serviço já cadastrado e a similaridade e escolha "É o mesmo serviço", "É outro serviço" ou "Descartar". Para muitas de uma vez, informe a similaridade mínima (ex.: 90%) e use os botões de decisão em bloco; o sistema pede confirmação.
4. **Enviar ao aprovador** envia as linhas que já estão prontas; linhas com erro ou ainda sem decisão não impedem o envio e podem ser tratadas depois. A tela informa quantas foram enviadas e quantas ainda precisam de atenção.

## 2c. Aprovar planilhas enviadas (aprovador)

Só o aprovador vê **Aprovações** no menu (`/ui/aprovacao`).

1. A lista mostra as planilhas com linhas esperando decisão: arquivo, quem enviou, quantas linhas aguardam e quantas estão em conflito. Clique no arquivo para abrir.
2. Cada linha mostra o que você precisa para decidir: o código proposto (nova) ou o texto, o serviço parecido já cadastrado e a similaridade (parecida), mais a decisão do comprador, área, condição, CNAE e quem conferiu. Use **Aprovar** ou **Rejeitar** (a rejeição pede um motivo).
3. Para decidir muitas de uma vez, escolha o filtro (todas, novas ou parecidas, com similaridade mínima) e use **Aprovar todas** ou **Rejeitar todas**. A tela diz quantas linhas serão afetadas, em todas as páginas, e pede confirmação. Sem filtro, rejeitar em bloco rejeita todas as linhas aguardando do lote; o motivo é obrigatório.
4. Depois da aprovação em bloco a tela mostra quantas foram aprovadas, quantas foram para conflito e quantas **não puderam ser processadas agora**; essas continuam aguardando e podem ser aprovadas de novo.
5. O bloco **em conflito** (recolhido) lista as linhas que não puderam virar código, com o motivo. Elas ficam fora das ações em bloco e não têm botões.

## 3. Revisar sugestões (aprovador)

Só o aprovador vê **Revisão**. Em **Revisão** (`/ui/revisao`) ficam os mapeamentos pendentes, do mais confirmado para o menos.

- **Aprovar**: passa a valer como busca exata. Os outros pendentes do mesmo nome são rejeitados.
- **Rejeitar**: exige motivo.
- **Revogar**: tira um aprovado de circulação, com motivo. O histórico é mantido.

Cada mudança fica registrada com o usuário logado, quando e por quê.

## 4. Implantar (equipe técnica)

1. Instale o **JDK 21** e o Docker.
2. `cp env.example .env` e preencha as senhas.
3. `docker compose up -d`
4. `./mvnw spring-boot:run`
5. Abra `http://localhost:8080`.

O banco é criado e populado pelo Flyway na primeira execução. Erros comuns estão no [README](../README.md#problemas-comuns).

## 5. Ajustar as réguas (equipe técnica)

| Parâmetro | Valor | Onde |
|-----------|-------|------|
| Limiar de sugestão de serviços parecidos | 0,4 | `ServicoService.LIMIAR_SUGESTAO` |
| Limiar de bloqueio | 0,8 | `ServicoService.LIMIAR_BLOQUEIO` |
| Limiar de sugestão de NBS | 0,2 | `BuscaNbsService.LIMIAR_SUGESTAO` |
| Tamanho máximo do código | 40 | `ServicoService.TAMANHO_MAX_CODIGO` |
