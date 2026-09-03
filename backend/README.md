# JCondo — API

Backend do sistema JCondo: CRUD web de moradores (Thymeleaf + Bootstrap) e a
**API REST consumida pelo aplicativo JCondo Mobile**.

> **Atividade Avaliativa 2** — este projeto foi estendido a partir da AA1. O CRUD
> web e a API de moradores continuam funcionando como antes; foram acrescentadas
> as entidades Aviso, AreaComum, Reserva e Ocorrencia, a autenticação por token e
> os endpoints que o aplicativo Android consome.
>
> Documentação completa dos endpoints: [`../docs/API.md`](../docs/API.md)

## Subir rápido, sem instalar MySQL

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2   # mvnw.cmd no Windows
```

Banco H2 em memória, populado automaticamente. Acesse:

- API + Swagger: <http://localhost:8080/swagger-ui/index.html>
- CRUD web de moradores: <http://localhost:8080/moradores>

**Credenciais:** `ana.souza@email.com` / `123456` (morador) ·
`sindico@jcondo.com` / `admin123` (administração)

---


Projeto integrador desenvolvido para a disciplina. É um sistema web de cadastro de
moradores de condomínio com CRUD completo (criar, listar, editar e excluir),
interface feita com Thymeleaf e Bootstrap, banco MySQL e API REST documentada
com Swagger.

Cada morador tem: ID, nome, apartamento, bloco, CPF, telefone e e-mail. Todos os
campos são validados com Bean Validation.

## O que a AA2 acrescentou

| Item | Detalhe |
|---|---|
| Entidades | `Aviso`, `AreaComum`, `Reserva`, `Ocorrencia` |
| Morador | Campos `senha` (hash SHA-256) e `perfil` (`MORADOR` / `ADMIN`) |
| Autenticação | `POST /api/auth/login` com token de sessão de 12h; interceptor protege `/api/**` |
| Regra de negócio | Validação de conflito de reserva no servidor (HTTP 409) |
| Erros | Formato JSON único `{status, erro, mensagem}` para todos os endpoints REST |
| Perfil `h2` | Banco em memória com carga automática, para rodar sem MySQL |
| Testes | `ReservaServiceTest`, `SenhaUtilTest`, `TokenServiceTest` |

## Tecnologias

- Java 21
- Spring Boot 3.5 (Spring MVC + Spring Data JPA)
- Hibernate
- MySQL
- Maven
- Thymeleaf
- Bootstrap 5 + Bootstrap Icons
- springdoc-openapi (Swagger)
- JUnit 5 e Mockito

## Requisitos para rodar

- JDK 21 instalado
- MySQL rodando na porta 3306 (usei o do XAMPP)
- Não precisa instalar o Maven, o projeto tem o wrapper (mvnw)

## Como importar o banco

1. Inicie o MySQL no XAMPP
2. Abra o phpMyAdmin (http://localhost/phpmyadmin)
3. Vá na aba "Importar", escolha o arquivo `database/jcondo.sql` e clique em Executar

Ou pelo terminal:

```
mysql -u root < database/jcondo.sql
```

O script cria o banco `jcondo`, a tabela `moradores` e já insere 5 moradores de
exemplo. A conexão está configurada para usuário root sem senha (padrão do XAMPP).
Se o seu MySQL tiver senha, é só ajustar no `application.properties`.

## Como executar

Na pasta do projeto:

```
mvnw.cmd spring-boot:run        (Windows)
./mvnw spring-boot:run          (Linux/Mac)
```

Depois é só abrir http://localhost:8080 que já cai na lista de moradores.

## Como rodar os testes

```
mvnw.cmd test
```

Os testes unitários do service rodam sem precisar do banco. Tem também um teste
de integração que sobe o contexto inteiro do Spring, esse precisa do MySQL ligado:

```
mvnw.cmd test -Dtest.integration=true
```

## Swagger

Com a aplicação rodando: http://localhost:8080/swagger-ui/index.html

Dá pra ver e testar todos os endpoints da API (/api/moradores) por lá.

## Estrutura do projeto

```
jcondo/
├── database/jcondo.sql          -> script do banco com dados de exemplo
├── docs/                        -> manuais de instalação e execução
├── src/main/java/jcondo/
│   ├── config/                  -> configuração do Swagger
│   ├── controller/              -> controllers MVC e REST
│   ├── entity/                  -> entidade Morador
│   ├── repository/              -> repository JPA
│   └── service/                 -> regras de negócio
├── src/main/resources/
│   ├── templates/               -> páginas Thymeleaf (index, form, layout)
│   ├── static/css/style.css
│   └── application.properties
├── src/test/java/jcondo/        -> testes unitários e de integração
└── pom.xml
```

A arquitetura segue o padrão em camadas: Controller -> Service -> Repository -> Entity.
O controller nunca acessa o repository direto.

## Autor

Jorge - Projeto Integrador UNOESC
