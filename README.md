# Desafio GC Gerdau App

API REST em **Spring Boot** conectada a um banco **PostgreSQL** executado em container **Docker**. O projeto consulta um catálogo de serviços (área, descrição, condição, CNAE e código padronizado).

## Tecnologias

- Java 21
- Spring Boot 4.1.1 (Web MVC, Data JPA, DevTools)
- PostgreSQL 16 (Alpine) + pgAdmin 4 via Docker Compose
- Flyway (migrations do banco)
- Lombok
- Maven

## O que foi feito

1. **Criação do projeto** Spring Boot com as dependências Web, Data JPA, Lombok e DevTools.
2. **Driver do PostgreSQL** adicionado ao `pom.xml`.
3. **Containers** configurados no `docker-compose.yml`: PostgreSQL 16 na porta `5431` (persistência em `./data-db`) e pgAdmin na porta `5050`.
4. **Credenciais em variáveis de ambiente**, lidas do arquivo `.env` tanto pelo Docker Compose quanto pelo Spring Boot.
5. **Migrations com Flyway**: a tabela `servicos_mock` e os dados mock são criados automaticamente na primeira execução da aplicação.
6. **Conexão com o banco** configurada no `application.properties`, com `ddl-auto=validate` (o Hibernate só confere se a entidade bate com a tabela criada pelo Flyway) e `open-in-view` desativado.
7. **Entidade `Servico`**, **`ServicoRepository`** (Spring Data JPA) e **`ServicoController`** com endpoints de consulta.

## Estrutura

```
Desafio-GC-Gerdau-App/
├── docker-compose.yml
├── env.example
├── pom.xml
└── src/main/
    ├── java/com/Gerdau/Desafio_GC_Gerdau_App/
    │   ├── controller/ServicoController.java
    │   ├── model/Servico.java
    │   ├── repository/ServicoRepository.java
    │   └── DesafioGcGerdauAppApplication.java
    └── resources/
        ├── application.properties
        └── db/migration/
            ├── V1__create_servicos_mock.sql
            └── V2__insert_servicos_mock.sql
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
| `V1__create_servicos_mock.sql` | Cria a tabela `servicos_mock` (constraints de unicidade e de condições válidas) |
| `V2__insert_servicos_mock.sql` | Insere os 15 serviços mock |

Para alterar o banco, crie um novo arquivo seguindo a convenção `V{n}__descricao.sql` (ex.: `V3__add_coluna_x.sql`). **Nunca edite uma migration já aplicada**: o Flyway valida o checksum e a aplicação deixa de subir.

## Endpoints

| Método | Rota                     | Descrição                       |
|--------|--------------------------|---------------------------------|
| GET    | `/servicos`              | Lista todos os serviços         |
| GET    | `/servicos/{id}`         | Busca um serviço pelo id        |
| GET    | `/servicos/area/{area}`  | Lista os serviços de uma área   |

### Exemplo

```bash
curl localhost:8080/servicos
curl localhost:8080/servicos/1
curl localhost:8080/servicos/area/Civil
```

Resposta de `/servicos/1`:

```json
{
  "id": 1,
  "areaServico": "Civil",
  "textoBreve": "Concretagem de fundação",
  "abreviatura": "CONC",
  "complemento": "_CONCRE",
  "id1Condicao": "Hora normal",
  "id2Cnae": "4399103",
  "codigoPadronizado": "CONC_CONCRE_HN4399103",
  "criadorCodigo": "bruno.fernandes",
  "dataCriacao": "2026-10-16T15:43:24.431946"
}
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
