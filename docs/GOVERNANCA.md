# Governança do catálogo

Quem cuida da base de serviços depois do saneamento, em quanto tempo e com quais regras. Os itens marcados **a preencher** dependem de decisão da Gerdau; o sistema já funciona sem eles, mas o monitoramento só vira rotina quando há um nome responsável.

## Responsáveis

| Papel | Quem | O que faz |
|---|---|---|
| Responsável pelo catálogo (data steward) | **a preencher** | Dono das regras de nomenclatura; acompanha `/ui/monitoramento` toda semana; decide as duplicatas possíveis |
| Aprovadores | **a preencher** (usuários com perfil `APROVADOR`) | Decidem linhas de importação, mapeamentos do De-Para e contestações |
| Compradores | usuários com perfil `COMPRADOR` | Cadastram e importam; decidem as parecidas antes de enviar |

## Prazo de aprovação

Proposta: aprovar ou rejeitar em até **2 dias úteis** após o envio. O painel de monitoramento avisa quando o tempo médio ultrapassa 48 h (`monitoramento.limite-horas-aprovacao`). Ajuste o limite se a Gerdau definir outro prazo.

## Regras de nomenclatura

- Texto breve em português, no singular, sem abreviação livre; a abreviatura do código é a única abreviação aceita.
- Diferenças que mudam o serviço (número, "interna/externa") vão no texto, não só no complemento.
- Texto, condição e CNAE iguais (ignorando maiúsculas e acentos) nunca coexistem: o banco recusa (índice `uq_servicos_texto_condicao_cnae`, V15). Cadastro com bloqueio contestável que repete exatamente esse texto também é recusado: é duplicata, não "outro serviço".
- Tamanho máximo do código: 40 caracteres (limite do SAP).

## Rotina semanal

Toda segunda, 7h (`monitoramento.cron`), o sistema grava um retrato em `monitoramento_execucao` (duplicatas possíveis, serviços sem NBS, fila de aprovação, De-Para pendentes) e registra no log um `ALERTA` para cada limite ultrapassado. O mesmo cálculo está sempre atualizado em `/ui/monitoramento` (só `APROVADOR`).

Limites (em `application.properties`): `monitoramento.limite-duplicatas` (10), `monitoramento.limite-fila-aprovacao` (50), `monitoramento.limite-horas-aprovacao` (48).

## Duplicatas legadas e mesclagem

A V15 não apaga nada. Entre serviços que já eram duplicados (mesmo texto normalizado, condição e CNAE), o mais antigo vale e os demais ficam com `duplicata_legada = TRUE`; eles aparecem no painel com origem "Legado". Para tratar uma duplicata:

1. O steward escolhe o código que fica.
2. O código descartado **não é apagado**: aposente-o (ou registre o de-para para o que fica) em vez de excluir, para que referências antigas continuem resolvendo.
3. Se for um texto escrito de outro jeito para o mesmo serviço, o aprovador marca "mesmo serviço" na importação, que grava um `servico_sinonimo`.

## Backup

A base fica em `./data-db` (volume do Docker). Antes de qualquer migration em produção faça `pg_dump` do banco; a rotina de backup agendada é responsabilidade da infraestrutura (**a preencher**).

## O que o sistema não cobre

- Cadastro de fornecedores e de-para fornecedor→serviço: este repositório trata do código de serviço do comprador.
- "Serviços sem uso": não há dado de consumo na base.
