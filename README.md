# Desafio GC Gerdau App

API REST em **Spring Boot** conectada a um banco **PostgreSQL** executado em container **Docker**. O projeto cadastra e lista itens (produtos e serviços).

## Tecnologias

- Java 21
- Spring Boot 4.1.1 (Web MVC, Data JPA, DevTools)
- PostgreSQL 16 (Alpine) via Docker Compose
- Lombok
- Maven

## O que foi feito

1. **Criação do projeto** Spring Boot com as dependências Web, Data JPA, Lombok e DevTools.
2. **Driver do PostgreSQL** adicionado ao `pom.xml`.
3. **Container do banco** configurado no `docker-compose.yml` (PostgreSQL 16 na porta `5431`, com persistência em `./data-db`).
4. **Credenciais em variáveis de ambiente**, lidas do arquivo `.env` tanto pelo Docker Compose quanto pelo Spring Boot.
5. **Conexão com o banco** configurada no `application.properties`, com `ddl-auto=update` para criar as tabelas automaticamente e `open-in-view` desativado.
6. **Entidade `Item`** com os campos código do produto, código do serviço, descrição do item e unidade de medida.
7. **`ItemRepository`** (Spring Data JPA) e **`ItemController`** com endpoints para criar e listar itens.

## Estrutura

```
Desafio-GC-Gerdau-App/
├── docker-compose.yml
├── .env.example
├── pom.xml
└── src/main/
    ├── java/com/Gerdau/Desafio_GC_Gerdau_App/
    │   ├── controller/ItemController.java
    │   ├── model/Item.java
    │   ├── repository/ItemRepository.java
    │   └── DesafioGcGerdauAppApplication.java
    └── resources/application.properties
```

## Como executar

### 1. Configurar variáveis de ambiente

Copie o exemplo e preencha a senha:

```bash
cp .env.example .env
```

```env
DB_NAME=pgsql-db-gerdau
DB_USER=admin
DB_PASSWORD=sua_senha
DB_PORT=5431
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

A API fica disponível em `http://localhost:8080`.

## Configuração do banco

O `application.properties` importa o `.env` e monta a conexão:

```properties
spring.config.import=optional:file:.env[.properties]
spring.datasource.url=jdbc:postgresql://localhost:${DB_PORT}/${DB_NAME}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
```

A aplicação roda fora do Docker, então o host é `localhost` e a porta é a mapeada no compose (`5431`, que aponta para a `5432` interna do container).

## Endpoints

| Método | Rota      | Descrição          |
|--------|-----------|--------------------|
| POST   | `/itens`  | Cadastra um item   |
| GET    | `/itens`  | Lista os itens     |

### Exemplo

```bash
curl -X POST localhost:8080/itens \
  -H "Content-Type: application/json" \
  -d '{"codigoProduto":"000123","codigoServico":"S-45","descricaoItem":"Vergalhão CA-50 10mm","unidadeMedida":"KG"}'

curl localhost:8080/itens
```

O `id` é gerado automaticamente pelo banco e retornado na resposta.

## Verificando o banco

```bash
docker exec -it pgsql-db-gerdau psql -U admin -d pgsql-db-gerdau -c "SELECT * FROM item;"
```

## Problemas comuns

| Erro | Causa | Solução |
|------|-------|---------|
| `ClassNotFoundException: org.postgresql.Driver` | Driver ausente no `pom.xml` | Adicionar a dependência e recarregar o Maven |
| `Connection refused` na porta 5431 | Container parado ou reiniciando | `docker ps -a` e `docker logs pgsql-db-gerdau` |
| `initdb: directory exists but is not empty` | `data-db` com dados de outra versão | `docker compose down`, `sudo rm -rf data-db`, subir novamente |
| `No property '...' found for type 'Item'` | Método do repositório diferente do atributo da entidade | Alinhar os nomes |
| `password authentication failed` | Senha do `.env` alterada após a criação do banco | Recriar a pasta `data-db` |

