# Desafio GC Gerdau App

API REST em **Spring Boot** conectada a um banco **PostgreSQL** executado em container **Docker**. O projeto padroniza códigos de serviço e bloqueia a criação de duplicados.

## Tecnologias

- Java 21
- Spring Boot 4.1.1 (Web MVC, Data JPA, DevTools)
- PostgreSQL 16 (Alpine) + pgAdmin 4 via Docker Compose
- Flyway (migrations do banco)
- Lombok
- Maven
- Apache POI e Commons CSV (leitura de planilhas `.xlsx` e `.csv`)

## O que a aplicação faz

Resolve o problema de códigos de serviço duplicados (TAP do Desafio GC 2026) na trilha Java:

1. **Cadastro com filtro**: ao criar um serviço, o sistema gera o código padronizado, compara o texto com os serviços existentes (`pg_trgm`) e **bloqueia duplicados**, avisando o comprador. Parecidos pedem confirmação.
2. **Busca de NBS**: o comprador digita o serviço (mesmo com erro) e recebe o código NBS oficial, por busca exata no De-Para ou por sugestões de similaridade.
3. **Smart Key**: junta o NBS ao sufixo de horário (ex.: `1.01.01-HN`).
4. **De-Para com revisão**: confirmações de usuários viram mapeamentos `PENDENTE`; um revisor aprova, rejeita ou revoga, com histórico. Só `APROVADO` vale como busca exata.
5. **Importação em massa**: o comprador envia uma planilha (`.csv`/`.xlsx`, até 20.000 linhas e 10 MB); o sistema aplica o filtro a cada linha em segundo plano, o comprador confere o resultado e envia ao **aprovador**, que decide. Nada entra na base sem essa aprovação. O que o aprovador liga a um serviço existente vira **sinônimo** e passa a bloquear o cadastro individual daquele texto.

Passo a passo de uso: [docs/GUIA_DE_USO.md](docs/GUIA_DE_USO.md). Regras de negócio e decisões: [docs/REGRAS_DE_NEGOCIO.md](docs/REGRAS_DE_NEGOCIO.md).

## Estrutura

```
src/main/java/com/Gerdau/Desafio_GC_Gerdau_App/
├── controller/   REST (/api, /servicos) e telas Thymeleaf (/ui)
├── service/      ServicoService, BuscaNbsService, DeParaService, SmartKeyService,
│                 ImportacaoService, ConferenciaService, AprovacaoImportacaoService, SinonimoServicoService
├── repository/   Spring Data JPA + queries nativas com pg_trgm
├── model/        entidades e enums (StatusDePara, OrigemDePara)
├── dto/          requests e responses
└── exception/    CadastroRejeitadoException, DeParaNaoEncontradoException
src/main/resources/
├── db/migration/ migrations Flyway (V1 a V10)
├── templates/    telas (busca, servicos, formulário, revisão, importação, aprovação)
└── static/       css e js
```

## Como executar

### 1. Configurar variáveis de ambiente

Copie o exemplo e preencha as senhas:

```bash
cp env.example .env
```

```env
DB_NAME=pgsql-db-gerdau
DB_USER=admin
DB_PASSWORD=sua_senha
DB_PORT=5431

PGADMIN_DEFAULT_EMAIL=admin@admin.com
PGADMIN_DEFAULT_PASSWORD=sua_senha
```

### 2. Subir o banco

```bash
docker compose up -d
docker ps
```

### 3. Rodar a aplicação

```bash
./mvnw spring-boot:run
```

Na primeira execução o Flyway cria a tabela e insere os dados mock. A API fica disponível em `http://localhost:8080`.

> O projeto exige **JDK 21+**. Se `./mvnw` acusar `release version 21 not supported`, aponte o `JAVA_HOME` para um JDK 21.

## Configuração do banco

O `application.properties` importa o `.env` e monta a conexão:

```properties
spring.config.import=optional:file:.env[.properties]
spring.datasource.url=jdbc:postgresql://localhost:${DB_PORT}/${DB_NAME}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
```

A aplicação roda fora do Docker, então o host é `localhost` e a porta é a mapeada no compose (`5431`, que aponta para a `5432` interna do container).

## Migrations (Flyway)

O schema do banco é versionado em `src/main/resources/db/migration`. Ao iniciar, o Flyway aplica em ordem as migrations ainda não executadas e registra o histórico na tabela `flyway_schema_history`.

| Arquivo | Conteúdo |
|---------|----------|
| `V1__create_servicos_mock.sql` | Tabela `servicos_mock` (unicidade e condições válidas) |
| `V2__insert_servicos_mock.sql` | 15 serviços mock |
| `V3__pg_trgm_similaridade.sql` | Extensão `pg_trgm` e índice de similaridade em `servicos_mock` |
| `V4__dicionario_de_para_revisao.sql` | `cat_nbs`, `dicionario_de_para` e histórico de revisão |
| `V5__popula_cat_nbs.sql` | Recorte da NBS 2.0 em `cat_nbs` |
| `V6__cat_horarios.sql` | `cat_horarios` com os sufixos da Smart Key |
| `V7__servicos_mock_codigo_nbs.sql` | Coluna `codigo_nbs` em `servicos_mock` |
| `V8__cat_horarios_unica_fonte.sql` | `cat_horarios.ativo` e remoção do `CHECK` de condição em `servicos_mock` |
| `V9__usuarios.sql` | Tabela `usuarios` (login) e dois usuários de exemplo |
| `V10__importacao_em_massa.sql` | `lotes_importacao`, `linhas_importacao` (situação e etapa de cada linha) e `servico_sinonimo` |

Para alterar o banco, crie um novo arquivo seguindo a convenção `V{n}__descricao.sql` (ex.: `V3__add_coluna_x.sql`). **Nunca edite uma migration já aplicada**: o Flyway valida o checksum e a aplicação deixa de subir.

## Login e perfis

Todas as telas e a API exigem login (usuário e senha guardados na tabela `usuarios`, senha com BCrypt). Há dois perfis:

| Perfil | Pode |
|--------|------|
| `COMPRADOR` | Cadastrar serviço, buscar serviços, buscar NBS, Smart Key, confirmar uma sugestão de NBS e importar planilhas (os próprios lotes) |
| `APROVADOR` | Tudo o que o comprador faz, mais a revisão do De-Para (`/ui/revisao` e `/api/de-para/**`) e a aprovação de planilhas (`/ui/aprovacao` e `/api/importacoes/aprovacao/**`) |

Sem login, as telas redirecionam para `/login` e a API responde `401`. Com perfil insuficiente a resposta é `403`. Usuários de exemplo (V9) e como trocar as senhas: `docs/GUIA_DE_USO.md`. O revisor gravado no histórico do De-Para é sempre o usuário logado.

## Telas

| Rota | Tela |
|------|------|
| `/` | Lista de serviços |
| `/ui/busca` | Busca de NBS e Smart Key |
| `/ui/servicos/novo` | Cadastro de novo serviço (com filtro de duplicados) |
| `/ui/servicos/{id}` | Detalhe do serviço |
| `/login` | Entrar (única tela sem login) |
| `/ui/revisao` | Fila de revisão do De-Para (só `APROVADOR`) |
| `/ui/importacoes` | Enviar planilha e ver os próprios lotes |
| `/ui/importacoes/{id}` | Conferência do lote: com erro, parecidas, novas, já existiam e repetidas (só o dono) |
| `/ui/aprovacao` | Lotes com linhas aguardando decisão (só `APROVADOR`) |
| `/ui/aprovacao/{id}` | Aprovação das linhas de um lote, uma a uma ou em bloco (só `APROVADOR`) |

## Endpoints

Todas as rotas exigem login. As de `/api/de-para` (exceto `confirmacoes`) e as de `/api/importacoes/aprovacao` são só do `APROVADOR`. Requisições `POST` precisam do token CSRF (header `X-CSRF-TOKEN`, valor na `<meta name="_csrf">` das telas).

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/servicos` | Lista todos os serviços |
| GET | `/servicos/{id}` | Busca um serviço pelo id |
| GET | `/servicos/area/{area}` | Lista os serviços de uma área |
| POST | `/servicos/analise` | Simula o cadastro: código gerado, similares e se bloquearia |
| POST | `/servicos?confirmar=false` | Cadastra. `201` criado; `409` com a análise se bloqueado ou se pede confirmação |
| GET | `/api/nbs/busca?texto=` | Busca de NBS: `EXATO` (De-Para aprovado) ou `SUGESTOES` |
| GET | `/api/horarios` | Lista os horários e sufixos |
| GET | `/api/smart-key?nbs=&horario=` | Monta a Smart Key (`400` se NBS ou horário não existem) |
| POST | `/api/de-para/confirmacoes` | Registra a confirmação de um usuário como `PENDENTE` |
| GET | `/api/de-para/fila` | Lista os pendentes |
| POST | `/api/de-para/{id}/aprovar` | Aprova (o revisor é o usuário logado) |
| POST | `/api/de-para/{id}/rejeitar` | Rejeita (corpo: `motivo`) |
| POST | `/api/de-para/{id}/revogar` | Revoga um aprovado (corpo: `motivo`) |

### Importação em massa

O usuário que age vem sempre do login, nunca do corpo. Lote de outro usuário responde `404` (o aprovador vê apenas as linhas enviadas e em conflito). Erros do corpo/estado: `400` (dado inválido), `409` (linha já decidida, ou fora da etapa esperada), `403` (perfil). Arquivo inválido ou acima dos limites é recusado com mensagem.

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/importacoes` | Envia o arquivo (`multipart`, campo `arquivo`). `201` com `{"id"}`; o processamento continua em segundo plano |
| GET | `/api/importacoes` | Os últimos 50 lotes do usuário |
| GET | `/api/importacoes/{id}` | Progresso e contagem por situação e etapa |
| GET | `/api/importacoes/{id}/linhas` | Linhas paginadas (`situacao`, `etapa`, `scoreMinimo`, `pagina`, `tamanho` até 500) |
| PUT | `/api/importacoes/linhas/{id}` | Corrige uma linha e a reclassifica |
| POST | `/api/importacoes/linhas/{id}/decisao` | Decide uma parecida: `MESMO_SERVICO` ou `OUTRO_SERVICO` |
| POST | `/api/importacoes/{id}/decisao-em-bloco` | Decide as parecidas com score a partir de `scoreMinimo` |
| POST | `/api/importacoes/linhas/{id}/descartar` | Descarta a linha |
| POST | `/api/importacoes/{id}/enviar` | Envia ao aprovador as linhas prontas; devolve `{"enviadas"}` |
| GET | `/api/importacoes/aprovacao/lotes` | Lotes com linhas enviadas (`APROVADOR`) |
| POST | `/api/importacoes/aprovacao/linhas/{id}/aprovar` | Aprova uma linha; devolve `APROVADA` ou `CONFLITO` |
| POST | `/api/importacoes/aprovacao/linhas/{id}/rejeitar` | Rejeita (corpo: `motivo`) |
| POST | `/api/importacoes/aprovacao/{id}/aprovar-em-bloco` | Aprova as enviadas que atendem ao filtro (`situacoes`, `scoreMinimo`); devolve `aprovadas`, `conflitos`, `falhas` |
| POST | `/api/importacoes/aprovacao/{id}/rejeitar-em-bloco` | Rejeita em bloco (corpo: `motivo`, e o filtro opcional) |

Formato da planilha, colunas e passo a passo: [docs/GUIA_DE_USO.md](docs/GUIA_DE_USO.md#3-importar-vários-serviços-de-uma-planilha-comprador). Regras: [docs/REGRAS_DE_NEGOCIO.md](docs/REGRAS_DE_NEGOCIO.md#importação-em-massa).

### Exemplos

A API usa a sessão do login. Com `curl`, entre uma vez e guarde o cookie; os `POST` também precisam do token CSRF, que está na `<meta name="_csrf">` das telas.

```bash
TOKEN=$(curl -s -c cj localhost:8080/login | grep -o 'name="_csrf" value="[^"]*"' | sed 's/.*value="//;s/"//')
curl -s -o /dev/null -b cj -c cj -d "username=comprador&password=comprador123&_csrf=$TOKEN" localhost:8080/login
CSRF=$(curl -s -b cj localhost:8080/ui/busca | grep -o '<meta name="_csrf" content="[^"]*"' | sed 's/.*content="//;s/"//')

curl -b cj "localhost:8080/api/nbs/busca?texto=instalaa%20eletrica"
curl -b cj "localhost:8080/api/smart-key?nbs=1.01.01&horario=HN"
curl -b cj -X POST localhost:8080/servicos/analise -H "X-CSRF-TOKEN: $CSRF" -H 'Content-Type: application/json' -d '{
  "areaServico":"Civil","textoBreve":"Concretagem de fundação","abreviatura":"CONC",
  "complemento":"_CONCRE","condicao":"HORA_NORMAL","cnae":"4399103"}'
```

## Verificando o banco

Pelo terminal:

```bash
docker exec -it pgsql-db-gerdau psql -U admin -d pgsql-db-gerdau -c "SELECT * FROM servicos_mock;"
```

Ou pelo pgAdmin em `http://localhost:5050`, com o login definido em `PGADMIN_DEFAULT_EMAIL`/`PGADMIN_DEFAULT_PASSWORD`. Ao registrar o servidor, use o host `pgsql-db-gerdau` e a porta `5432` (rede interna do Docker).

## Problemas comuns

| Erro | Causa | Solução |
|------|-------|---------|
| `release version 21 not supported` | Maven usando JDK antigo | Definir `JAVA_HOME` para um JDK 21+ |
| `Connection refused` na porta 5431 | Container parado ou reiniciando | `docker ps -a` e `docker logs pgsql-db-gerdau` |
| `Found non-empty schema(s) "public" but no schema history table` | Banco criado antes do Flyway | `docker compose down`, `sudo rm -rf data-db`, subir novamente |
| `Validate failed: Migration checksum mismatch` | Migration já aplicada foi editada | Desfazer a edição e criar uma nova migration `V{n}__...` |
| `Schema-validation: missing column ...` | Entidade `Servico` diferente da tabela | Criar migration para ajustar a tabela ou corrigir a entidade |
| `initdb: directory exists but is not empty` | `data-db` com dados de outra versão | `docker compose down`, `sudo rm -rf data-db`, subir novamente |
| `password authentication failed` | Senha do `.env` alterada após a criação do banco | Recriar a pasta `data-db` |
