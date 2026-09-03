# JCondo Mobile

Aplicativo Android de gestão condominial e a API REST que o alimenta.

Implementação da **Atividade Avaliativa 2** da disciplina de Desenvolvimento para
Dispositivos Móveis (UNOESC), a partir do projeto elaborado na Atividade Avaliativa 1.

**Acadêmico:** Jorge Luiz do Nascimento

---

## O que o aplicativo faz

| Tela | Função |
|---|---|
| **Login** | Autenticação por e-mail e senha, com token de sessão guardado no aparelho |
| **Início** | Saudação, contadores do condomínio, próxima reserva, avisos recentes e atalhos |
| **Avisos** | Comunicados da administração, com prioridade e leitura completa |
| **Reservas** | Reserva de áreas comuns em três passos, com agenda de horários e cancelamento |
| **Ocorrências** | Abertura de chamados com protocolo e acompanhamento do status |
| **Perfil** | Dados de contato editáveis, preferência de notificação e saída da conta |

Regra central do sistema: **duas reservas nunca ocupam o mesmo horário na mesma área.**
A validação acontece no servidor, não no aplicativo — se dois moradores confirmarem
o mesmo horário ao mesmo tempo, um recebe HTTP 409 com a explicação e a agenda
atualizada na tela.

---

## Estrutura do repositório

```
jcondo-mobile/
├── backend/     API REST em Spring Boot 3.5 + Java 21 (evolução do projeto da AA1)
├── mobile/      Aplicativo Android em Java, com Gradle e Retrofit
└── docs/        Relatório técnico e documentação da API
```

---

## Como executar

### 1. Subir a API

Requisito: **JDK 21**.

**Opção rápida — sem instalar banco** (recomendada para avaliação):

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

No Windows, troque `./mvnw` por `mvnw.cmd`.

Isso sobe a API em `http://localhost:8080` com um banco H2 em memória, já populado
com moradores, áreas comuns, avisos, uma reserva e uma ocorrência de exemplo.

**Opção com MySQL** (configuração original da AA1):

1. Importe `backend/database/jcondo.sql` no MySQL;
2. Ajuste usuário e senha em `backend/src/main/resources/application.properties`;
3. Execute `./mvnw spring-boot:run`.

**Verificar se subiu:**

- Swagger: <http://localhost:8080/swagger-ui/index.html>
- CRUD web de moradores (da AA1): <http://localhost:8080/moradores>

Para testar a API pelo Swagger: chame `POST /api/auth/login`, copie o `token` da
resposta e cole no botão **Authorize**.

### 2. Rodar o aplicativo

Requisito: **Android Studio** (Ladybug ou mais recente).

1. `File → Open` e selecione a pasta **`mobile`** (não a raiz do repositório);
2. Aguarde o *Gradle Sync* baixar as dependências;
3. `Run` em um emulador com **API 26 ou superior**.

O app já vem apontando para `http://10.0.2.2:8080/`, que é como o emulador
enxerga o `localhost` do computador.

**Para testar em um celular físico:** conecte o aparelho na mesma rede Wi-Fi do
computador, descubra o IP da máquina (`ipconfig` no Windows, `ip a` no Linux) e,
na tela de login, toque em **Configurar servidor** e informe `http://SEU_IP:8080`.
Não precisa recompilar.

### 3. Entrar

| Perfil | E-mail | Senha |
|---|---|---|
| Morador | `ana.souza@email.com` | `123456` |
| Administração | `sindico@jcondo.com` | `admin123` |

Os demais moradores de exemplo (`bruno.lima@`, `carla.rocha@`, `diego.alves@`)
usam a mesma senha `123456` — úteis para demonstrar o conflito de reserva com
duas contas ao mesmo tempo.

---

## Tecnologias

**Aplicativo**

- Java 17 · Android SDK 35 · minSdk 26
- Gradle 8.9 · Android Gradle Plugin 8.7.3
- Material Design 3 · View Binding
- Retrofit 2.11 + Gson + OkHttp (interceptor de autenticação e log)

**API**

- Java 21 · Spring Boot 3.5 (Web, Data JPA, Validation)
- MySQL (padrão) ou H2 em memória (perfil `h2`)
- springdoc-openapi (Swagger UI)
- JUnit 5 + Mockito nos testes

---

## Testes automatizados

```bash
cd backend
./mvnw test
```

Cobrem a regra de conflito de reserva, as validações de data e horário, a
permissão de cancelamento, a geração da agenda, o hash de senha e o ciclo de
vida do token de sessão.

---

## Documentação

- [`docs/API.md`](docs/API.md) — todos os endpoints, com exemplos de requisição e resposta
- [`docs/Relatorio_Tecnico_JCondo_Mobile.docx`](docs/) — relatório técnico da atividade

---

## Sobre segurança

Este é um projeto acadêmico. Duas escolhas foram feitas para manter o escopo, e
ambas estão documentadas no relatório:

- as senhas usam **SHA-256 com sal fixo** em vez de BCrypt/Argon2 — suficiente
  para não guardar senha em texto puro, mas não é o que se usaria em produção;
- a API roda em **HTTP** na rede local, com o `cleartext` liberado apenas para
  faixas de endereço privadas em `network_security_config.xml`.
