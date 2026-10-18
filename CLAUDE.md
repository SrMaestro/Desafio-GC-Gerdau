# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Visão geral

Aplicação Spring Boot 4.1 / Java 21 (Maven, Lombok, Thymeleaf, Flyway, PostgreSQL 16) do Desafio GC Gerdau 2026. Padroniza códigos de serviço e impede duplicados. O código-fonte, a documentação e os nomes de domínio estão em português. Regras de negócio detalhadas: `docs/REGRAS_DE_NEGOCIO.md`; uso das telas: `docs/GUIA_DE_USO.md`.

## Contexto do projeto (TAP)

Origem: TAP do Desafio GC 2026 (Gerdau | Geração Caldeira), em `tap_termo_de_abertura_do_projeto.md`. Hoje cada comprador cria os próprios códigos de serviço sem nenhum filtro, o que gerou milhares de duplicados e esquecidos. O limite de 40 caracteres vem do SAP.

- Este repositório é a **trilha Java**: entrada de novos códigos, filtro que compara com os existentes, bloqueio de duplicados e aviso a quem cria. A trilha IA & Dados cuida do saneamento da base completa e entrega a lista padronizada que o filtro deve usar. Essa lista não está aqui: `servicos_mock` é só dado de exemplo.
- Critérios de sucesso do TAP que guiam decisões: a checagem de um código novo deve ser rápida, a tela deve ser simples para compradores, e a documentação deve servir a quem não participou do projeto (por isso mantenha `README.md` e `docs/` em dia ao mudar comportamento).

## Comandos

```bash
cp env.example .env              # primeira vez: preencher senhas e DB_PORT (padrão 5431)
docker compose up -d             # sobe PostgreSQL (+ pgAdmin em :5050); dados em ./data-db
./mvnw spring-boot:run           # app em http://localhost:8080 (exige JDK 21; ajuste JAVA_HOME se preciso)
./mvnw test                      # todos os testes
./mvnw test -Dtest=ServicoServiceTest                   # uma classe
./mvnw test -Dtest=ServicoServiceTest#nomeDoMetodo      # um método
```

- `DesafioGcGerdauAppApplicationTests` (`@SpringBootTest`) precisa do Postgres do compose no ar com o `.env` válido. Os demais testes de service usam Mockito e os de controller usam MVC test, sem banco.
- Não há linter configurado.

## Arquitetura

Camadas clássicas em `src/main/java/com/Gerdau/Desafio_GC_Gerdau_App/`: `controller` → `service` → `repository` (Spring Data JPA) → `model`, com `dto` e `exception`. Há dois tipos de controller: REST (`/servicos`, `/api/...`) e de telas Thymeleaf (`*ViewController`, sob `/ui` e `/`), com JS puro em `static/js` consumindo a API REST.

Quatro serviços formam o fluxo de negócio (ver `docs/REGRAS_DE_NEGOCIO.md` para limiares e regras exatas):

- **ServicoService**: cadastro. Gera o código (`abreviatura + complemento + "_" + sufixo da condição + CNAE`, máx. 40 caracteres), compara o texto com serviços existentes e decide bloquear, pedir confirmação (`?confirmar=true`) ou cadastrar. Bloqueio concorrente é garantido pela restrição de unicidade do banco, não só pela checagem em código.
- **BuscaNbsService**: dado um texto, tenta primeiro match exato no De-Para `APROVADO` (nome normalizado: minúsculo, sem acento) e senão sugere NBS por similaridade.
- **SmartKeyService**: `<NBS>-<sufixo do horário>`, validado contra `cat_nbs` e `cat_horarios`.
- **DeParaService**: ciclo `PENDENTE → APROVADO / REJEITADO / REVOGADO`; toda mudança grava em `dicionario_de_para_historico`. Só um `APROVADO` por nome; confirmação nunca altera um aprovado.

A similaridade de texto é feita no banco com `pg_trgm` por **queries nativas** nos repositories (extensão e índices criados na migration V3). Mudanças nessa lógica normalmente envolvem repository + migration, não só Java.

## Banco e migrations

- `ddl-auto=validate`: o Hibernate não cria schema. Qualquer mudança em entidade exige uma migration `V{n}__descricao.sql` em `src/main/resources/db/migration` (hoje V1–V8). **Nunca edite uma migration já aplicada** (falha de checksum do Flyway); crie uma nova.
- As condições (hora normal, hora extra, posto mensal...) vivem só em `cat_horarios`. Condição nova = `INSERT` numa migration; condição aposentada = `ativo = FALSE` (nunca apagar). `servicos_mock.id1_condicao` guarda o texto da condição na hora do cadastro, sem FK, então serviços antigos não mudam quando o catálogo muda.
- A conexão vem do `.env` (importado por `spring.config.import`) via `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`. A app roda fora do Docker, então o host é `localhost`.
- Se o Flyway reclamar de schema sem histórico, ou o Postgres de `data-db` for de outra versão/senha: `docker compose down`, apagar `data-db` e subir de novo (apaga os dados).
