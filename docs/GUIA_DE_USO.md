# Guia de uso

Para quem usa o sistema no dia a dia. Não é preciso conhecer o projeto.

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

## 3. Revisar sugestões (revisor)

Em **Revisão** (`/ui/revisao`) ficam os mapeamentos pendentes, do mais confirmado para o menos.

- **Aprovar**: passa a valer como busca exata. Os outros pendentes do mesmo nome são rejeitados.
- **Rejeitar**: exige motivo.
- **Revogar**: tira um aprovado de circulação, com motivo. O histórico é mantido.

Informe sempre o nome do revisor. Cada mudança fica registrada com quem fez, quando e por quê.

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
