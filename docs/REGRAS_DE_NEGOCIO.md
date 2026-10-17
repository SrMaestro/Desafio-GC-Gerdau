# Regras de negócio

## Fluxo de entrada de código

```
cadastro -> gera código -> compara (pg_trgm) -> bloqueia | pede confirmação | cadastra
```

1. O código é gerado em maiúsculas: `abreviatura + complemento + "_" + sufixo da condição + CNAE`.
2. Bloqueio: o código já existe, **ou** há serviço com similaridade de texto ≥ 0,8, mesma condição e mesmo CNAE.
3. Confirmação: há serviços com similaridade ≥ 0,4 que não bloqueiam. O cadastro só segue com `confirmar=true`.
4. O código gerado não pode passar de 40 caracteres.
5. Se dois usuários criarem o mesmo código ao mesmo tempo, a restrição de unicidade do banco recusa o segundo, e ele recebe a resposta de bloqueio.
6. Se informado, o código NBS precisa existir em `cat_nbs`.

## Busca de NBS (fall-back)

1. **Exata**: o nome normalizado (minúsculo, sem acento, espaços colapsados) tem mapeamento `APROVADO` no De-Para.
2. **Similaridade**: `pg_trgm` sobre `cat_nbs`, limiar 0,2, até 5 sugestões. O texto padrão repetido nas descrições da NBS é ignorado.

## Smart Key

`<código NBS>-<sufixo do horário>`, só gerada se ambos existem nos catálogos (`cat_nbs`, `cat_horarios`).

## Ciclo do De-Para

| Status | Significado |
|--------|-------------|
| `PENDENTE` | Confirmado por usuário, aguardando revisor |
| `APROVADO` | Vale como busca exata. Só um por nome |
| `REJEITADO` | Recusado (motivo obrigatório, ou outro foi aprovado) |
| `REVOGADO` | Aprovado que saiu de circulação (motivo obrigatório) |

- Confirmar um par que já está pendente soma uma confirmação.
- Confirmar um nome que já tem aprovado é ignorado: a confirmação nunca altera um aprovado.
- Toda mudança de status grava em `dicionario_de_para_historico` (quem, quando, motivo).
- A origem do mapeamento é `LOTE` (carga em lote), `USUARIO` ou `REVISOR`.
