# API REST do JCondo

Base: `http://localhost:8080` (emulador Android: `http://10.0.2.2:8080`)
Documentação interativa: `/swagger-ui/index.html`

Todos os endpoints sob `/api/**` exigem o header:

```
Authorization: Bearer <token>
```

A única exceção é `POST /api/auth/login`. O token vale 12 horas e é invalidado
no logout.

---

## Formato de erro

Toda falha devolve o mesmo corpo, e é o campo `mensagem` que o aplicativo
mostra ao morador:

```json
{
  "status": 409,
  "erro": "conflito_reserva",
  "mensagem": "O horário das 18:00 às 20:00 já está reservado em Salão de Festas. Escolha outro horário.",
  "campos": null,
  "momento": "2026-09-03T14:22:10.113"
}
```

| Código | `erro` | Quando acontece |
|---|---|---|
| 400 | `validacao` | Campo obrigatório ausente ou inválido (`campos` traz erro por campo) |
| 400 | `regra_negocio` | Reserva no passado, fora do horário da área, senha atual incorreta |
| 401 | `nao_autorizado` | Credenciais erradas, token ausente, inválido ou expirado |
| 404 | `nao_encontrado` | Id que não existe |
| 409 | `conflito_reserva` | Horário já ocupado por outra reserva confirmada |
| 500 | `erro_interno` | Falha inesperada (sem stack trace no corpo) |

---

## Autenticação

### `POST /api/auth/login`

```json
{ "email": "ana.souza@email.com", "senha": "123456" }
```

**200**

```json
{
  "token": "3f9a1c4b8e2d47a5b6c1d0e9f8a7b6c5",
  "morador": {
    "id": 1,
    "nome": "Ana Paula Souza",
    "email": "ana.souza@email.com",
    "telefone": "(49) 99911-1111",
    "bloco": "A",
    "apartamento": "101",
    "unidade": "Bloco A - Apto 101",
    "perfil": "MORADOR"
  }
}
```

**401** — e-mail ou senha incorretos. A mensagem é a mesma nos dois casos, para
não revelar quais e-mails estão cadastrados.

### `POST /api/auth/logout`

**204** — invalida o token enviado no header.

---

## Tela inicial e perfil

### `GET /api/home/resumo`

Reúne em uma chamada tudo o que a tela inicial precisa.

```json
{
  "morador": { "...": "..." },
  "saudacao": "Boa tarde",
  "avisosRecentes": [ { "id": 5, "titulo": "...", "prioridade": "ALTA", "dataPublicacao": "03/09/2026 08:30" } ],
  "proximaReserva": { "id": 3, "areaNome": "Salão de Festas", "data": "2026-09-09", "horaInicio": "18:00", "horaFim": "20:00", "status": "CONFIRMADA" },
  "totalAvisos": 5,
  "reservasAtivas": 1,
  "ocorrenciasAbertas": 1
}
```

### `GET /api/perfil`

Dados do morador dono do token. Nenhum id é aceito por parâmetro — um morador
não consegue consultar o cadastro de outro.

### `PUT /api/perfil`

```json
{ "telefone": "(49) 99999-0000", "email": "novo@email.com",
  "senhaAtual": "123456", "novaSenha": "outrasenha" }
```

Todos os campos são opcionais. Bloco, apartamento, CPF e perfil não podem ser
alterados por aqui — são dados cadastrais da administração.

---

## Avisos

### `GET /api/avisos`

Lista os avisos ativos, do mais recente para o mais antigo.

```json
[
  {
    "id": 5,
    "titulo": "Manutenção dos elevadores na quinta-feira",
    "conteudo": "A manutenção preventiva...",
    "autor": "Administração",
    "prioridade": "ALTA",
    "dataPublicacao": "03/09/2026 08:30"
  }
]
```

`prioridade`: `NORMAL` · `ALTA` · `URGENTE`

### `GET /api/avisos/{id}`

### `POST /api/avisos` — somente perfil `ADMIN`

```json
{ "titulo": "Assembleia dia 20", "conteudo": "...", "prioridade": "NORMAL" }
```

**201** com o aviso criado. **400** `regra_negocio` se o morador não for ADMIN.

---

## Áreas comuns

### `GET /api/areas`

```json
[
  {
    "id": 1,
    "nome": "Salão de Festas",
    "descricao": "Espaço coberto para até 60 pessoas, com cozinha de apoio.",
    "capacidade": 60,
    "horarioAbertura": "10:00",
    "horarioFechamento": "22:00",
    "regras": "Devolver o espaço limpo. Som permitido até as 22h."
  }
]
```

### `GET /api/areas/{id}/horarios?data=2026-09-10`

Agenda do dia em faixas de 2 horas dentro do funcionamento da área.

```json
[
  { "horaInicio": "10:00", "horaFim": "12:00", "disponivel": true  },
  { "horaInicio": "12:00", "horaFim": "14:00", "disponivel": true  },
  { "horaInicio": "14:00", "horaFim": "16:00", "disponivel": false },
  { "horaInicio": "16:00", "horaFim": "18:00", "disponivel": true  }
]
```

Uma faixa fica `disponivel: false` quando existe reserva confirmada que a
sobrepõe, ou quando o horário já passou no dia de hoje.

---

## Reservas

### `GET /api/reservas/minhas`

```json
[
  {
    "id": 3,
    "areaId": 1,
    "areaNome": "Salão de Festas",
    "data": "2026-09-09",
    "dataFormatada": "09/09/2026",
    "horaInicio": "18:00",
    "horaFim": "20:00",
    "status": "CONFIRMADA",
    "moradorNome": "Ana Paula Souza",
    "unidade": "Bloco A - Apto 101"
  }
]
```

### `POST /api/reservas`

```json
{ "areaId": 1, "data": "2026-09-10", "horaInicio": "18:00", "horaFim": "20:00" }
```

**201** com a reserva criada.

Validações aplicadas, nesta ordem:

1. a área existe e está ativa;
2. o término é depois do início;
3. a data não está no passado;
4. se for hoje, o horário ainda não passou;
5. a faixa está dentro do funcionamento da área;
6. a data está dentro dos 90 dias de antecedência;
7. **nenhuma reserva confirmada se sobrepõe** — senão, **409**.

A sobreposição é calculada no banco: duas faixas conflitam quando
`inicio < fimExistente` **e** `fim > inicioExistente`. Encostar não conflita —
14:00–16:00 e 16:00–18:00 convivem na mesma área.

### `DELETE /api/reservas/{id}`

Cancela uma reserva. Só o dono (ou um ADMIN) pode cancelar, e só antes de a
reserva acontecer. Devolve a reserva com `status: "CANCELADA"`.

---

## Ocorrências

### `GET /api/ocorrencias/categorias`

```json
["Iluminação","Limpeza","Elevador","Manutenção","Segurança","Barulho","Área comum","Outros"]
```

### `GET /api/ocorrencias/minhas`

```json
[
  {
    "id": 1,
    "protocolo": "20260901-1042",
    "categoria": "Iluminação",
    "titulo": "Lâmpada queimada na garagem",
    "descricao": "A lâmpada da vaga 42...",
    "status": "EM_ANDAMENTO",
    "statusLabel": "Em andamento",
    "dataAbertura": "01/09/2026 19:04",
    "dataAtualizacao": "02/09/2026 15:10",
    "resposta": "Material solicitado ao fornecedor.",
    "moradorNome": "Ana Paula Souza"
  }
]
```

`status`: `ABERTA` · `EM_ANDAMENTO` · `RESOLVIDA`

### `GET /api/ocorrencias/{id}`

Só devolve ocorrências do próprio morador (ADMIN vê todas).

### `POST /api/ocorrencias`

```json
{ "categoria": "Iluminação", "titulo": "Lâmpada queimada", "descricao": "..." }
```

**201** com a ocorrência criada e o `protocolo` gerado no formato
`AAAAMMDD-NNNN`.

---

## Moradores (CRUD da AA1)

Mantido da Atividade Avaliativa 1, agora protegido por token:

| Método | Rota |
|---|---|
| GET | `/api/moradores` |
| GET | `/api/moradores/{id}` |
| POST | `/api/moradores` |
| PUT | `/api/moradores/{id}` |
| DELETE | `/api/moradores/{id}` |

A senha nunca volta nas respostas. Um morador cadastrado sem senha recebe a
senha padrão `123456`, que ele troca pelo perfil no aplicativo.
