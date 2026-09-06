-- ===========================================================================
-- JCondo - migração do banco do projeto web para a versão do aplicativo
--
-- QUANDO USAR: o banco "jcondo" já existe no seu MySQL, vindo do projeto web,
-- e você quer preservar os moradores já cadastrados.
--
-- SE O BANCO NÃO EXISTE: não use este arquivo. Use o jcondo.sql, que cria
-- tudo do zero, ou simplesmente crie o banco vazio e deixe a aplicação
-- montar as tabelas (ver o passo 1 abaixo).
--
-- Como executar no phpMyAdmin:
--   1. Selecione o banco jcondo na lista da esquerda
--   2. Aba "SQL"
--   3. Cole este arquivo e clique em "Executar"
-- ===========================================================================

USE jcondo;

-- ---------------------------------------------------------------------------
-- PASSO 1 - As tabelas: você não precisa criar nada
--
-- O projeto está configurado com spring.jpa.hibernate.ddl-auto=update. Quando
-- a aplicação sobe, o Hibernate compara as classes com o banco e cria o que
-- estiver faltando: as colunas senha e perfil na tabela moradores, e as
-- tabelas avisos, areas_comuns, reservas e ocorrencias.
--
-- Depois disso, a classe DataSeeder preenche a senha padrão (123456) de todos
-- os moradores que estavam sem senha, e cadastra as áreas comuns e os avisos
-- de exemplo caso essas tabelas estejam vazias.
--
-- Ou seja: subir a aplicação uma vez já resolve quase tudo. O único item que
-- ela não faz sozinha é o passo 2.
-- ---------------------------------------------------------------------------


-- ---------------------------------------------------------------------------
-- PASSO 2 - Criar o usuário da administração
--
-- O DataSeeder só cria os usuários de exemplo quando a tabela de moradores
-- está vazia. Como no seu caso ela já tem registros, o síndico precisa ser
-- inserido na mão. Sem ele não dá para demonstrar a publicação de avisos,
-- que é restrita ao perfil ADMIN.
--
-- Antes de rodar, confira se ele já não existe:
--     SELECT id, nome, email, perfil FROM moradores WHERE email = 'sindico@jcondo.com';
--
-- Se a consulta não devolver nada, execute o INSERT abaixo.
-- A senha é admin123, gravada como hash SHA-256 (ver SenhaUtil.java).
-- ---------------------------------------------------------------------------

INSERT INTO moradores (nome, apartamento, bloco, cpf, telefone, email, senha, perfil)
VALUES ('Roberto Síndico', '001', 'A', '999.888.777-66', '(49) 99900-0000',
        'sindico@jcondo.com',
        '2160d362ddfca07a7f219fc4ce7bedae5105bcc793435ab56fba4643ecdcad57',
        'ADMIN');


-- ---------------------------------------------------------------------------
-- PASSO 3 - Conferir o resultado
--
-- Rode depois de subir a aplicação pela primeira vez.
-- ---------------------------------------------------------------------------

SELECT 'moradores'    AS tabela, COUNT(*) AS registros FROM moradores
UNION ALL SELECT 'avisos',       COUNT(*) FROM avisos
UNION ALL SELECT 'areas_comuns', COUNT(*) FROM areas_comuns
UNION ALL SELECT 'reservas',     COUNT(*) FROM reservas
UNION ALL SELECT 'ocorrencias',  COUNT(*) FROM ocorrencias;

-- Esperado: moradores = os seus + 1 síndico, avisos = 5, areas_comuns = 5,
-- reservas = 0 e ocorrencias = 0.

-- Confira também se todo mundo consegue entrar no aplicativo.
-- Esta consulta tem que devolver ZERO linhas:
SELECT id, nome, email FROM moradores WHERE senha IS NULL OR senha = '';


-- ===========================================================================
-- SE VOCÊ PREFERIR CRIAR AS TABELAS POR SQL, EM VEZ DE DEIXAR COM O HIBERNATE
--
-- Rode os comandos abaixo ANTES de subir a aplicação. São exatamente as mesmas
-- estruturas que o Hibernate criaria.
--
-- Nos dois ALTER TABLE, se o MySQL responder "Duplicate column name", é porque
-- a coluna já existe: ignore esse erro específico e siga em frente.
-- ===========================================================================

-- ALTER TABLE moradores ADD COLUMN senha  VARCHAR(100) NULL;
-- ALTER TABLE moradores ADD COLUMN perfil VARCHAR(10)  NOT NULL DEFAULT 'MORADOR';
--
-- UPDATE moradores
--    SET senha = '114bcd61f23ee3da38676c5f2a41fc99c9f94576f8f7a607b664cd6a53d6f340'
--  WHERE senha IS NULL OR senha = '';
--
-- CREATE TABLE IF NOT EXISTS avisos (
--     id              BIGINT        NOT NULL AUTO_INCREMENT,
--     titulo          VARCHAR(120)  NOT NULL,
--     conteudo        VARCHAR(2000) NOT NULL,
--     autor           VARCHAR(80)   NULL,
--     prioridade      VARCHAR(10)   NOT NULL DEFAULT 'NORMAL',
--     data_publicacao DATETIME      NOT NULL,
--     ativo           BIT(1)        NOT NULL DEFAULT b'1',
--     PRIMARY KEY (id)
-- ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
--
-- CREATE TABLE IF NOT EXISTS areas_comuns (
--     id                 BIGINT       NOT NULL AUTO_INCREMENT,
--     nome               VARCHAR(80)  NOT NULL,
--     descricao          VARCHAR(300) NULL,
--     capacidade         INT          NOT NULL DEFAULT 0,
--     horario_abertura   TIME         NOT NULL,
--     horario_fechamento TIME         NOT NULL,
--     regras             VARCHAR(300) NULL,
--     ativa              BIT(1)       NOT NULL DEFAULT b'1',
--     PRIMARY KEY (id)
-- ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
--
-- CREATE TABLE IF NOT EXISTS reservas (
--     id          BIGINT      NOT NULL AUTO_INCREMENT,
--     morador_id  BIGINT      NOT NULL,
--     area_id     BIGINT      NOT NULL,
--     data        DATE        NOT NULL,
--     hora_inicio TIME        NOT NULL,
--     hora_fim    TIME        NOT NULL,
--     status      VARCHAR(12) NOT NULL DEFAULT 'CONFIRMADA',
--     criada_em   DATETIME    NOT NULL,
--     PRIMARY KEY (id),
--     CONSTRAINT fk_reserva_morador FOREIGN KEY (morador_id) REFERENCES moradores (id),
--     CONSTRAINT fk_reserva_area    FOREIGN KEY (area_id)    REFERENCES areas_comuns (id),
--     INDEX idx_reserva_agenda (area_id, data, status)
-- ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
--
-- CREATE TABLE IF NOT EXISTS ocorrencias (
--     id               BIGINT        NOT NULL AUTO_INCREMENT,
--     protocolo        VARCHAR(20)   NOT NULL,
--     morador_id       BIGINT        NOT NULL,
--     categoria        VARCHAR(40)   NOT NULL,
--     titulo           VARCHAR(120)  NOT NULL,
--     descricao        VARCHAR(2000) NOT NULL,
--     status           VARCHAR(15)   NOT NULL DEFAULT 'ABERTA',
--     data_abertura    DATETIME      NOT NULL,
--     data_atualizacao DATETIME      NULL,
--     resposta         VARCHAR(1000) NULL,
--     PRIMARY KEY (id),
--     UNIQUE KEY uk_ocorrencia_protocolo (protocolo),
--     CONSTRAINT fk_ocorrencia_morador FOREIGN KEY (morador_id) REFERENCES moradores (id)
-- ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
