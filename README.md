# Desafio GC Gerdau App

Aplicação web que **padroniza códigos de serviço e impede duplicados**. Feita para o Desafio GC 2026 (Gerdau | Geração Caldeira), na trilha Java.

## O problema e a solução

Hoje cada comprador cria os próprios códigos de serviço sem nenhum filtro. O resultado são milhares de códigos duplicados e esquecidos. O limite de 40 caracteres do código vem do SAP.

A aplicação coloca um filtro na entrada: antes de criar um código, ela compara o texto digitado com os serviços já existentes. Se for o mesmo serviço, **bloqueia e avisa o comprador**, oferecendo o código que já existe. Se for parecido, pede confirmação. Se for novo, cadastra com o código padronizado.

## O que ela faz

1. **Cadastro com filtro:** gera o código (`abreviatura + complemento + "_" + condição + CNAE`, até 40 caracteres), compara o texto com os existentes (`pg_trgm`) e bloqueia duplicados. Parecidos pedem confirmação. O comprador pode contestar um bloqueio com justificativa, que fica registrada.
2. **Busca de NBS:** o comprador digita o serviço, mesmo com erro, e recebe o código NBS oficial por busca exata no De-Para ou por sugestões de similaridade.
3. **Smart Key:** junta o NBS ao sufixo de horário (ex.: `1.0106.19.00-HN`).
4. **De-Para com revisão:** confirmações de usuários viram mapeamentos `PENDENTE`; um aprovador aprova, rejeita ou revoga, com histórico.
5. **Registro das consultas e indicadores:** toda tentativa de cadastro fica gravada e o aprovador a confere. A tela inicial mostra duplicados barrados, códigos reaproveitados e cadastros sem correspondência.
6. **Importação em massa:** o comprador envia uma planilha (`.csv` ou `.xlsx`, até 20.000 linhas e 10 MB), o sistema aplica o filtro em cada linha, o comprador confere e o aprovador decide. Nada entra na base sem aprovação.

Para ver cada tela em uso, siga o [guia de uso](docs/GUIA_DE_USO.md).

## Rodar na sua máquina

**Precisa de:** Docker (com Docker Compose) e JDK 21.

```bash
git clone <url-do-repositorio>
cd Desafio-GC-Gerdau-App

cp env.example .env              # troque as duas senhas por qualquer valor
docker compose up -d             # sobe o banco (e o pgAdmin em :5050)
export JAVA_HOME=/caminho/do/jdk-21   # só se o seu JDK padrão não for o 21
./mvnw spring-boot:run           # espere a mensagem "Started ..."
```

Abra **http://localhost:8080** e entre com um dos usuários abaixo. Na primeira execução o Flyway cria as tabelas e insere 15 serviços de exemplo.

**Para uma demonstração com dados realistas** (cerca de 330 serviços industriais de siderurgia, com quase-duplicados antigos, histórico de consultas, De-Para e lotes de importação em andamento), suba com o perfil `demo`: `./mvnw spring-boot:run -Dspring-boot.run.profiles=demo`. Os dados vêm de `src/main/resources/db/demo` e só entram nesse perfil. O que a banca vê está em [`docs/GUIA_DE_USO.md`](docs/GUIA_DE_USO.md#roteiro-de-demonstração-dados-realistas).

| Usuário | Senha | Perfil |
|---------|-------|--------|
| `comprador` | `comprador123` | Cadastra, busca e importa planilhas |
| `aprovador` | `aprovador123` | Tudo isso, mais aprovar planilhas, revisar o De-Para e ver as consultas |

O catálogo é de **exemplo** (15 serviços): a lista saneada fica com a trilha IA & Dados. Os usuários acima são só para teste, **não use essas senhas fora da sua máquina**.

O `.env` define o banco. Os valores do `env.example` já funcionam; só `DB_PASSWORD` e `PGADMIN_DEFAULT_PASSWORD` vêm vazios e precisam de algum valor:

```env
DB_NAME=pgsql-db-gerdau
DB_USER=admin
DB_PASSWORD=
DB_PORT=5431
```

### Como testar

- **Roteiro passo a passo**, com o que digitar e o resultado esperado de cada passo: [docs/GUIA_DE_USO.md](docs/GUIA_DE_USO.md#5-roteiro-de-teste-para-a-equipe).
- **Planilha de exemplo** para a importação: [docs/exemplos/servicos_exemplo.csv](docs/exemplos/servicos_exemplo.csv).
- **Testes automáticos:** `./mvnw test` (com o banco no ar).

### Voltar ao estado inicial

Tudo o que se cadastra fica no banco. Para recomeçar só com os serviços de exemplo (**apaga todos os dados**):

```bash
# pare a aplicação (Ctrl+C) e depois:
docker compose down
sudo rm -rf data-db
docker compose up -d
./mvnw spring-boot:run
```

### Problemas comuns

| Erro | Causa | Solução |
|------|-------|---------|
| `release version 21 not supported` | Maven usando JDK antigo | Definir `JAVA_HOME` para um JDK 21+ |
| `Connection refused` na porta 5431 | Container parado ou reiniciando | `docker ps -a` e `docker logs pgsql-db-gerdau` |
| `Found non-empty schema(s) "public" but no schema history table` | Banco criado antes do Flyway | `docker compose down`, `sudo rm -rf data-db`, subir novamente |
| `Validate failed: Migration checksum mismatch` | Migration já aplicada foi editada | Desfazer a edição e criar uma nova migration `V{n}__...` |
| `initdb: directory exists but is not empty` | `data-db` com dados de outra versão | `docker compose down`, `sudo rm -rf data-db`, subir novamente |
| `password authentication failed` | Senha do `.env` alterada após a criação do banco | Recriar a pasta `data-db` |

## Resultados e limitações

A similaridade foi validada com 29 casos escritos pela equipe (corretos, ambíguos e incorretos), em [docs/VALIDACAO_SIMILARIDADE.md](docs/VALIDACAO_SIMILARIDADE.md). Os principais pontos:

- A amostra é pequena e fabricada, então **não há taxa de acerto**. A validação deve ser repetida com a base saneada da trilha IA & Dados.
- Serviços diferentes que só mudam num detalhe curto no fim do nome podem ser bloqueados como duplicados. Por isso o comprador pode contestar o bloqueio, com justificativa registrada.
- Sinônimos e siglas não são entendidos pela similaridade. Os sinônimos aprovados cobrem parte disso.
- Os limiares de similaridade (0,4 e 0,8) vêm da prática e devem ser revistos com a base real.

## Documentação

| Documento | Para quê |
|-----------|----------|
| [docs/GUIA_DE_USO.md](docs/GUIA_DE_USO.md) | Passo a passo das telas e roteiro de teste |
| [docs/REGRAS_DE_NEGOCIO.md](docs/REGRAS_DE_NEGOCIO.md) | Limiares, decisões e regras de cada fluxo |
| [docs/VALIDACAO_SIMILARIDADE.md](docs/VALIDACAO_SIMILARIDADE.md) | Casos testados, resultados e limitações |
| [docs/REFERENCIA_TECNICA.md](docs/REFERENCIA_TECNICA.md) | Estrutura do código, migrations, telas e endpoints da API |
| [docs/MAPA_DO_SITE.md](docs/MAPA_DO_SITE.md) | Mapa das telas |

## Tecnologias

Java 21, Spring Boot 4.1 (Web MVC, Data JPA, Security, Thymeleaf), PostgreSQL 16 com `pg_trgm`, Flyway, Lombok, Maven, Apache POI e Commons CSV, Docker Compose.
