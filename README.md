# Desafio GC Gerdau App

API REST em **Spring Boot** conectada a um banco **PostgreSQL** executado em container **Docker**. O projeto padroniza códigos de serviço e bloqueia a criação de duplicados.

## Tecnologias

- Java 21
- Spring Boot 4.1.1 (Web MVC, Data JPA, DevTools)
- PostgreSQL 16 (Alpine) + pgAdmin 4 via Docker Compose
- Flyway (migrations do banco)
- Lombok
- Maven

## O que a aplicação faz

Resolve o problema de códigos de serviço duplicados (TAP do Desafio GC 2026) na trilha Java:

1. **Cadastro com filtro**: ao criar um serviço, o sistema gera o código padronizado, compara o texto com os serviços existentes (`pg_trgm`) e **bloqueia duplicados**, avisando o comprador. Parecidos pedem confirmação.
2. **Busca de NBS**: o comprador digita o serviço (mesmo com erro) e recebe o código NBS oficial, por busca exata no De-Para ou por sugestões de similaridade.
3. **Smart Key**: junta o NBS ao sufixo de horário (ex.: `1.01.01-HN`).
4. **De-Para com revisão**: confirmações de usuários viram mapeamentos `PENDENTE`; um revisor aprova, rejeita ou revoga, com histórico. Só `APROVADO` vale como busca exata.

Passo a passo de uso: [docs/GUIA_DE_USO.md](docs/GUIA_DE_USO.md). Regras de negócio e decisões: [docs/REGRAS_DE_NEGOCIO.md](docs/REGRAS_DE_NEGOCIO.md).

## Estrutura

```
src/main/java/com/Gerdau/Desafio_GC_Gerdau_App/
├── controller/   REST (/api, /servicos) e telas Thymeleaf (/ui)
├── service/      ServicoService, BuscaNbsService, DeParaService, SmartKeyService
├── repository/   Spring Data JPA + queries nativas com pg_trgm
├── model/        entidades e enums (Condicao, StatusDePara, OrigemDePara)
├── dto/          requests e responses
└── exception/    CadastroRejeitadoException, DeParaNaoEncontradoException
src/main/resources/
├── db/migration/ migrations Flyway (V1 a V7)
├── templates/    telas (busca, servicos, formulário, revisão)
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

Para alterar o banco, crie um novo arquivo seguindo a convenção `V{n}__descricao.sql` (ex.: `V3__add_coluna_x.sql`). **Nunca edite uma migration já aplicada**: o Flyway valida o checksum e a aplicação deixa de subir.

## Telas

| Rota | Tela |
|------|------|
| `/` | Lista de serviços |
| `/ui/busca` | Busca de NBS e Smart Key |
| `/ui/servicos/novo` | Cadastro de novo serviço (com filtro de duplicados) |
| `/ui/servicos/{id}` | Detalhe do serviço |
| `/ui/revisao` | Fila de revisão do De-Para |

## Endpoints

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
| POST | `/api/de-para/{id}/aprovar` | Aprova (corpo: `revisor`) |
| POST | `/api/de-para/{id}/rejeitar` | Rejeita (corpo: `revisor`, `motivo`) |
| POST | `/api/de-para/{id}/revogar` | Revoga um aprovado (corpo: `revisor`, `motivo`) |

### Exemplos

```bash
curl localhost:8080/api/nbs/busca?texto=instalaa%20eletrica
curl "localhost:8080/api/smart-key?nbs=1.01.01&horario=HN"
curl -X POST localhost:8080/servicos/analise -H 'Content-Type: application/json' -d '{
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
