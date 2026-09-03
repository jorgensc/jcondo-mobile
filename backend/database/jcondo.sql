-- ===========================================================================
-- JCondo - script do banco de dados
-- Testado no MySQL do XAMPP (importar pelo phpMyAdmin ou rodar no terminal)
--
-- Atividade Avaliativa 2: alem da tabela de moradores da AA1, o script cria
-- as tabelas que o aplicativo mobile consome (avisos, areas comuns, reservas
-- e ocorrencias) e as colunas senha/perfil usadas no login do app.
--
-- As senhas sao gravadas como hash SHA-256 do texto "JCondo$" + senha
-- (ver jcondo.security.SenhaUtil). Os hashes abaixo correspondem a:
--   123456   -> moradores
--   admin123 -> sindico
-- ===========================================================================

CREATE DATABASE IF NOT EXISTS jcondo
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE jcondo;

-- ---------------------------------------------------------------------------
-- Moradores (tambem sao os usuarios do aplicativo)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS moradores (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    nome        VARCHAR(100) NOT NULL,
    apartamento VARCHAR(10)  NOT NULL,
    bloco       VARCHAR(10)  NOT NULL,
    cpf         VARCHAR(14)  NOT NULL,
    telefone    VARCHAR(20)  NOT NULL,
    email       VARCHAR(100) NOT NULL,
    senha       VARCHAR(100) NULL,
    perfil      VARCHAR(10)  NOT NULL DEFAULT 'MORADOR',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- se o banco veio da AA1, as duas colunas novas entram com estes comandos:
-- ALTER TABLE moradores ADD COLUMN senha VARCHAR(100) NULL;
-- ALTER TABLE moradores ADD COLUMN perfil VARCHAR(10) NOT NULL DEFAULT 'MORADOR';

-- ---------------------------------------------------------------------------
-- Avisos publicados pela administracao
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS avisos (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    titulo           VARCHAR(120)  NOT NULL,
    conteudo         VARCHAR(2000) NOT NULL,
    autor            VARCHAR(80)   NULL,
    prioridade       VARCHAR(10)   NOT NULL DEFAULT 'NORMAL',
    data_publicacao  DATETIME      NOT NULL,
    ativo            BIT(1)        NOT NULL DEFAULT b'1',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------------------
-- Areas comuns reservaveis
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS areas_comuns (
    id                 BIGINT      NOT NULL AUTO_INCREMENT,
    nome               VARCHAR(80) NOT NULL,
    descricao          VARCHAR(300) NULL,
    capacidade         INT         NOT NULL DEFAULT 0,
    horario_abertura   TIME        NOT NULL,
    horario_fechamento TIME        NOT NULL,
    regras             VARCHAR(300) NULL,
    ativa              BIT(1)      NOT NULL DEFAULT b'1',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------------------
-- Reservas
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reservas (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    morador_id  BIGINT      NOT NULL,
    area_id     BIGINT      NOT NULL,
    data        DATE        NOT NULL,
    hora_inicio TIME        NOT NULL,
    hora_fim    TIME        NOT NULL,
    status      VARCHAR(12) NOT NULL DEFAULT 'CONFIRMADA',
    criada_em   DATETIME    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_reserva_morador FOREIGN KEY (morador_id) REFERENCES moradores (id),
    CONSTRAINT fk_reserva_area    FOREIGN KEY (area_id)    REFERENCES areas_comuns (id),
    INDEX idx_reserva_agenda (area_id, data, status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------------------
-- Ocorrencias
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ocorrencias (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    protocolo        VARCHAR(20)   NOT NULL,
    morador_id       BIGINT        NOT NULL,
    categoria        VARCHAR(40)   NOT NULL,
    titulo           VARCHAR(120)  NOT NULL,
    descricao        VARCHAR(2000) NOT NULL,
    status           VARCHAR(15)   NOT NULL DEFAULT 'ABERTA',
    data_abertura    DATETIME      NOT NULL,
    data_atualizacao DATETIME      NULL,
    resposta         VARCHAR(1000) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ocorrencia_protocolo (protocolo),
    CONSTRAINT fk_ocorrencia_morador FOREIGN KEY (morador_id) REFERENCES moradores (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ===========================================================================
-- Dados de exemplo
-- (a aplicacao tambem popula isso sozinha pelo DataSeeder quando as tabelas
--  estao vazias; este bloco serve para quem prefere carregar direto no banco)
-- ===========================================================================

INSERT INTO moradores (nome, apartamento, bloco, cpf, telefone, email, senha, perfil) VALUES
('Ana Paula Souza',      '101', 'A', '123.456.789-01', '(49) 99911-1111', 'ana.souza@email.com',
 '114bcd61f23ee3da38676c5f2a41fc99c9f94576f8f7a607b664cd6a53d6f340', 'MORADOR'),
('Bruno Oliveira Lima',  '202', 'B', '234.567.890-12', '(49) 99922-2222', 'bruno.lima@email.com',
 '114bcd61f23ee3da38676c5f2a41fc99c9f94576f8f7a607b664cd6a53d6f340', 'MORADOR'),
('Carla Mendes Rocha',   '303', 'A', '345.678.901-23', '(49) 99933-3333', 'carla.rocha@email.com',
 '114bcd61f23ee3da38676c5f2a41fc99c9f94576f8f7a607b664cd6a53d6f340', 'MORADOR'),
('Diego Ferreira Alves', '104', 'C', '456.789.012-34', '(49) 99944-4444', 'diego.alves@email.com',
 '114bcd61f23ee3da38676c5f2a41fc99c9f94576f8f7a607b664cd6a53d6f340', 'MORADOR'),
('Roberto Síndico',      '001', 'A', '999.888.777-66', '(49) 99900-0000', 'sindico@jcondo.com',
 '2160d362ddfca07a7f219fc4ce7bedae5105bcc793435ab56fba4643ecdcad57', 'ADMIN');

INSERT INTO areas_comuns (nome, descricao, capacidade, horario_abertura, horario_fechamento, regras) VALUES
('Salão de Festas',       'Espaço coberto para até 60 pessoas, com cozinha de apoio.', 60, '10:00:00', '22:00:00', 'Devolver o espaço limpo. Som permitido até as 22h.'),
('Churrasqueira',         'Área externa com duas churrasqueiras e mesas.',             30, '10:00:00', '22:00:00', 'Carvão e utensílios por conta do morador.'),
('Quadra Poliesportiva',  'Quadra descoberta para futsal, vôlei e basquete.',          20, '08:00:00', '22:00:00', 'Uso de calçado adequado é obrigatório.'),
('Espaço Gourmet',        'Sala climatizada com mesa para 12 lugares.',                12, '10:00:00', '22:00:00', 'Reserva limitada a uma por final de semana.'),
('Sala de Reuniões',      'Sala para reuniões de condomínio e home office.',           10, '08:00:00', '20:00:00', 'Agendar com antecedência mínima de 24 horas.');

INSERT INTO avisos (titulo, conteudo, autor, prioridade, data_publicacao, ativo) VALUES
('Manutenção dos elevadores na quinta-feira',
 'A manutenção preventiva dos elevadores dos blocos A e B acontece na quinta-feira, das 8h às 12h. Durante o período apenas um elevador por bloco ficará em operação.',
 'Administração', 'ALTA', NOW(), b'1'),
('Assembleia ordinária dia 20',
 'A assembleia ordinária para aprovação da previsão orçamentária acontece no dia 20, às 19h30, no salão de festas.',
 'Síndico', 'NORMAL', NOW(), b'1'),
('Limpeza da caixa d''água - interrupção do fornecimento',
 'Na próxima terça-feira o fornecimento de água será interrompido das 9h às 14h para a limpeza semestral das caixas d''água.',
 'Administração', 'URGENTE', NOW(), b'1'),
('Nova regra para uso da churrasqueira',
 'A partir deste mês a churrasqueira deve ser reservada pelo aplicativo. Cada unidade pode reservar até duas vezes por mês.',
 'Administração', 'NORMAL', NOW(), b'1'),
('Coleta seletiva às segundas e quintas',
 'Os recicláveis devem ser depositados no container azul do subsolo.',
 'Administração', 'NORMAL', NOW(), b'1');
