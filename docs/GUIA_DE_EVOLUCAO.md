# Guia de evolução

Como continuar o projeto sem quebrar o que já funciona.

O código foi organizado para que uma funcionalidade nova entre sempre nos mesmos
lugares. Este guia mostra onde ficam esses lugares, e traz o passo a passo
completo de uma adição de ponta a ponta.

---

## O mapa: onde fica cada coisa

### Backend (`backend/src/main/java/jcondo/`)

| Pasta | O que vai aqui |
|---|---|
| `entity/` | Uma classe por tabela. Só campos, construtores e getters/setters. |
| `repository/` | Interface por entidade, estendendo `JpaRepository`. É onde entram as consultas. |
| `service/` | **As regras de negócio.** Nenhuma regra deve morar fora daqui. |
| `dto/` | O formato que a API entrega e recebe. Records, achatados, com datas já formatadas. |
| `controller/` | Só recebe HTTP, chama o service e devolve DTO. Não tem `if` de regra. |
| `exception/` | Exceções de negócio e o tratador único (`ApiExceptionHandler`). |
| `security/` | Token, hash de senha, interceptor e usuário da sessão. |
| `config/` | Registro do interceptor, CORS, Swagger e carga de dados de exemplo. |

### Aplicativo (`mobile/app/src/main/java/com/jcondo/mobile/`)

| Pasta | O que vai aqui |
|---|---|
| `api/JCondoService.java` | O contrato: um método por endpoint. |
| `api/model/` | Uma classe por objeto JSON. Campos públicos, `Serializable`. |
| `api/ApiClient.java` | Construção do Retrofit e o interceptor que anexa o token. |
| `api/RespostaApi.java` | Tradução de falha em mensagem para o usuário. |
| `session/` | Token, dados do morador e endereço da API. |
| `ui/<area>/` | Uma pasta por área do MVP: fragmento, adaptador e telas daquela área. |
| `util/` | Datas, notificações, estados de tela e atalhos de interface. |

---

## Os quatro componentes que você já tem prontos

Toda tela nova nasce com esses comportamentos, sem escrever nada:

**1. `EstadoUi`** — cobre carregando, vazio e erro numa lista.
```java
estado = new EstadoUi(binding.estado, binding.listaRecycler);
estado.carregando();
estado.vazio("Nada por aqui", "Explique o que preencheria esta lista.");
estado.erro(mensagem, v -> carregar());
estado.conteudo();
```

**2. `RespostaApi`** — transforma HTTP e exceção de rede em frase legível.
```java
RespostaApi.mensagemDeErro(contexto, resposta);   // resposta com código de erro
RespostaApi.mensagemDeFalha(contexto, throwable); // falhou antes de responder
```

**3. `fragment_lista.xml`** — o layout compartilhado por avisos, reservas e
ocorrências. Título, lista, estado e botão de ação. Uma lista nova reaproveita
esse arquivo em vez de criar outro.

**4. O interceptor do `ApiClient`** — anexa o token sozinho. Você nunca precisa
enviar o header `Authorization` em uma chamada nova.

E no backend, o `ApiExceptionHandler` já converte qualquer exceção sua em JSON
padronizado. Lançar `RegraNegocioException("mensagem")` num service faz a
mensagem chegar formatada na tela do morador.

---

## Passo a passo: adicionar uma funcionalidade

Exemplo real: **documentos do condomínio** (atas, regulamento, balancetes),
que está na lista de evoluções previstas.

### Backend

**1. Entidade** — `entity/Documento.java`

Copie a estrutura de `Aviso.java`, que é a entidade mais simples.

```java
@Entity
@Table(name = "documentos")
public class Documento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(nullable = false, length = 40)
    private String categoria;      // ATA, REGULAMENTO, BALANCETE

    @Column(nullable = false, length = 300)
    private String url;

    @Column(name = "data_publicacao", nullable = false)
    private LocalDateTime dataPublicacao;

    // construtor vazio, construtor cheio, getters e setters
}
```

**2. Repositório** — `repository/DocumentoRepository.java`

```java
@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    List<Documento> findAllByOrderByDataPublicacaoDesc();
    List<Documento> findByCategoriaOrderByDataPublicacaoDesc(String categoria);
}
```

**3. Service** — `service/DocumentoService.java`

Aqui vão as regras. Se um documento só pode ser visto por quem está em dia, ou
se só o síndico publica, a verificação é aqui, e não no controller.

**4. DTO** — `dto/DocumentoDTO.java`

Record com um método estático `de(Documento)`. Formate a data no DTO, não na
tela:

```java
public record DocumentoDTO(Long id, String titulo, String categoria,
                           String url, String dataPublicacao) {
    private static final DateTimeFormatter BR =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static DocumentoDTO de(Documento d) {
        return new DocumentoDTO(d.getId(), d.getTitulo(), d.getCategoria(),
                d.getUrl(), d.getDataPublicacao().format(BR));
    }
}
```

**5. Controller** — `controller/DocumentoRestController.java`

```java
@RestController
@RequestMapping("/api/documentos")
@Tag(name = "Documentos", description = "Atas, regulamentos e balancetes")
public class DocumentoRestController {

    private final DocumentoService service;
    private final UsuarioLogado usuarioLogado;

    // construtor

    @GetMapping
    public List<DocumentoDTO> listar() {
        return service.listar().stream().map(DocumentoDTO::de).toList();
    }
}
```

Não precisa fazer nada para proteger a rota: qualquer caminho sob `/api/**` já
passa pelo `AuthInterceptor`.

**6. Dados de exemplo** — acrescente um método `semearDocumentos()` no
`DataSeeder`, no mesmo formato dos outros, e chame no `run()`.

**7. Script SQL** — acrescente o `CREATE TABLE` em `database/jcondo.sql`, para
quem usa MySQL.

**8. Teste** — `DocumentoServiceTest`, no molde do `ReservaServiceTest`.

### Aplicativo

**1. Modelo** — `api/model/Documento.java`

Campos públicos com os mesmos nomes do JSON, `implements Serializable`.

**2. Contrato** — acrescente em `api/JCondoService.java`:

```java
@GET("api/documentos")
Call<List<Documento>> documentos();
```

**3. Adaptador** — `ui/documentos/DocumentoAdapter.java`, copiando o
`AvisoAdapter`. Crie o `item_documento.xml` no molde do `item_aviso.xml`.

**4. Fragmento** — `ui/documentos/DocumentosFragment.java`, copiando o
`AvisosFragment`. Ele já usa `fragment_lista.xml`, então você não cria layout de
tela.

**5. Navegação** — acrescente o item em `res/menu/menu_navegacao.xml` e o `else
if` correspondente no `MainActivity`:

```java
} else if (id == R.id.nav_documentos) {
    mostrar("documentos", DocumentosFragment::new);
}
```

**6. Textos** — todos os textos novos vão em `res/values/strings.xml`. Nenhuma
string escrita direto no layout ou no Java.

**7. Ícone** — um vector drawable em `res/drawable/`, no mesmo formato dos
existentes.

---

## Regras que valem sempre

**A regra de negócio mora no service do backend.** Se você se pegar escrevendo
um `if` de regra dentro de um `Fragment` ou de um `Controller`, é sinal de que
está no lugar errado. O aplicativo pode *antecipar* a regra para melhorar a
experiência (esconder um botão, desabilitar um horário), mas quem decide é o
servidor.

**Todo texto visível vai para `strings.xml`.** Isso é o que permite traduzir o
aplicativo depois sem caçar frase no meio do código.

**Toda tela que carrega dados usa o `EstadoUi`.** Nenhuma lista pode ficar
branca sem explicação.

**Toda chamada de API trata `onFailure`.** O par `onResponse`/`onFailure` do
Retrofit sempre vem completo, e a mensagem sai do `RespostaApi`.

**Antes de usar o `binding` dentro de um callback, cheque se ele é nulo.** O
fragmento pode ter saído da tela enquanto a resposta vinha:

```java
if (binding == null) {
    return;
}
```

**Uma exceção de negócio nova herda de `RuntimeException`** e ganha um
`@ExceptionHandler` no `ApiExceptionHandler`, com o código HTTP certo.

---

## Onde mexer para as evoluções já previstas

| Evolução | Por onde começar |
|---|---|
| Foto na ocorrência | `OcorrenciaRequest` recebe o arquivo; use `@Multipart` no Retrofit e `MultipartFile` no controller. Precisa de permissão de câmera no manifesto. |
| Notificação push real | Crie um projeto no Firebase, adicione o `google-services.json`, e um serviço no backend que dispara o envio quando um aviso é publicado. O `Notificacoes.java` já tem o canal pronto. |
| BCrypt no lugar do SHA-256 | Adicione `spring-boot-starter-security` só pelo `BCryptPasswordEncoder` e troque o corpo de `SenhaUtil`. Configure o `SecurityFilterChain` para não bloquear as rotas, senão ele assume o controle. |
| Sessão com JWT | Troque o `TokenService` por emissão e validação de JWT. O `AuthInterceptor` e o interceptor do app não mudam. |
| Tela administrativa | Uma pasta `ui/admin/`, visível só quando `SessionManager.getMorador().isAdmin()` for verdadeiro. Os endpoints de admin já existem. |
| Cache offline | Room no aplicativo, gravando a resposta de cada lista. O `EstadoUi` ganharia um estado a mais: "mostrando dados de X minutos atrás". |
| Versão iOS | Nenhuma mudança no backend. A API já entrega tudo pronto para consumo. |

---

## Antes de fazer commit

```bash
cd backend && ./mvnw test          # os testes precisam passar
```

No Android Studio, `Build > Rebuild Project` deve terminar sem erro e sem aviso
novo. Se você acrescentou uma tela, rode no emulador e passe pelos quatro
estados: carregando, com conteúdo, vazia e sem rede (basta parar a API).
