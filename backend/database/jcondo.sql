-- ===========================================================================
-- JCondo - criação do banco de dados do zero
--
-- Cria o banco, as cinco tabelas e os dados de exemplo usados na
-- demonstração do aplicativo JCondo Mobile.
--
-- ATENÇÃO: a primeira linha APAGA o banco jcondo se ele já existir, com tudo
-- que estiver dentro. Se você tem dados que quer preservar, comente a linha
-- do DROP DATABASE antes de executar.
--
-- Como executar no phpMyAdmin (XAMPP):
--   1. Ligue o MySQL no XAMPP Control Panel
--   2. Abra http://localhost/phpmyadmin
--   3. Clique na aba "Importar", no menu de cima
--   4. Escolha este arquivo e clique em "Executar"
--
-- Ou pelo terminal:
--   mysql -u root < jcondo.sql
--
-- Depois de importar, suba a aplicação com:
--   mvnw.cmd spring-boot:run
-- ===========================================================================

DROP DATABASE IF EXISTS jcondo;

CREATE DATABASE jcondo
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE jcondo;

SET NAMES utf8mb4;


-- ===========================================================================
-- ESTRUTURA
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- moradores
-- Cadastro dos moradores, que são também os usuários do aplicativo.
-- As colunas senha e perfil foram acrescentadas para o login mobile.
-- ---------------------------------------------------------------------------
CREATE TABLE moradores (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    nome        VARCHAR(100) NOT NULL,
    apartamento VARCHAR(10)  NOT NULL,
    bloco       VARCHAR(10)  NOT NULL,
    cpf         VARCHAR(14)  NOT NULL,
    telefone    VARCHAR(20)  NOT NULL,
    email       VARCHAR(100) NOT NULL,
    senha       VARCHAR(100) NULL,
    perfil      VARCHAR(10)  NOT NULL DEFAULT 'MORADOR',
    PRIMARY KEY (id),
    INDEX idx_morador_email (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------------------
-- avisos
-- Comunicados publicados pela administração. Não têm vínculo com morador
-- porque valem para o condomínio inteiro.
-- prioridade: NORMAL, ALTA ou URGENTE
-- ---------------------------------------------------------------------------
CREATE TABLE avisos (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    titulo          VARCHAR(120)  NOT NULL,
    conteudo        VARCHAR(2000) NOT NULL,
    autor           VARCHAR(80)   NULL,
    prioridade      VARCHAR(10)   NOT NULL DEFAULT 'NORMAL',
    data_publicacao DATETIME      NOT NULL,
    ativo           BIT(1)        NOT NULL DEFAULT b'1',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------------------
-- areas_comuns
-- Espaços reserváveis. A janela de funcionamento é usada pelo servidor para
-- recusar reservas fora do horário permitido.
-- ---------------------------------------------------------------------------
CREATE TABLE areas_comuns (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    nome               VARCHAR(80)  NOT NULL,
    descricao          VARCHAR(300) NULL,
    capacidade         INT          NOT NULL DEFAULT 0,
    horario_abertura   TIME         NOT NULL,
    horario_fechamento TIME         NOT NULL,
    regras             VARCHAR(300) NULL,
    ativa              BIT(1)       NOT NULL DEFAULT b'1',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------------------
-- reservas
-- Vínculo entre morador, área, data e faixa de horário.
-- status: CONFIRMADA ou CANCELADA
--
-- O índice idx_reserva_agenda existe por causa da consulta de conflito, que
-- roda a cada tentativa de reserva filtrando por área, data e status.
-- ---------------------------------------------------------------------------
CREATE TABLE reservas (
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
-- ocorrencias
-- Chamados abertos pelos moradores.
-- status: ABERTA, EM_ANDAMENTO ou RESOLVIDA
-- O protocolo é único e serve como comprovante para o morador.
-- ---------------------------------------------------------------------------
CREATE TABLE ocorrencias (
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
-- DADOS DE EXEMPLO
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- Moradores
--
-- As senhas ficam gravadas como hash SHA-256 do texto "JCondo$" + senha,
-- calculado pela classe jcondo.security.SenhaUtil. Os valores abaixo são:
--   114bcd61... = 123456   (os quatro moradores)
--   2160d362... = admin123 (o síndico)
-- ---------------------------------------------------------------------------
INSERT INTO moradores (nome, apartamento, bloco, cpf, telefone, email, senha, perfil) VALUES
('Ana Paula Souza',      '101', 'A', '123.456.789-01', '(49) 99911-1111', 'ana.souza@email.com',
 '114bcd61f23ee3da38676c5f2a41fc99c9f94576f8f7a607b664cd6a53d6f340', 'MORADOR'),
('Bruno Oliveira Lima',  '202', 'B', '234.567.890-12', '(49) 99922-2222', 'bruno.lima@email.com',
 '114bcd61f23ee3da38676c5f2a41fc99c9f94576f8f7a607b664cd6a53d6f340', 'MORADOR'),
('Carla Mendes Rocha',   '303', 'A', '345.678.901-23', '(49) 99933-3333', 'carla.rocha@email.com',
 '114bcd61f23ee3da38676c5f2a41fc99c9f94576f8f7a607b664cd6a53d6f340', 'MORADOR'),
('Diego Ferreira Alves', '104', 'C', '456.789.012-34', '(49) 99944-4444', 'diego.alves@email.com',
 '114bcd61f23ee3da38676c5f2a41fc99c9f94576f8f7a607b664cd6a53d6f340', 'MORADOR'),
('Jorge',                '001', 'A', '999.888.777-66', '(49) 99900-0000', 'sindico@jcondo.com',
 '2160d362ddfca07a7f219fc4ce7bedae5105bcc793435ab56fba4643ecdcad57', 'ADMIN');

-- ---------------------------------------------------------------------------
-- Áreas comuns
-- ---------------------------------------------------------------------------
INSERT INTO areas_comuns (nome, descricao, capacidade, horario_abertura, horario_fechamento, regras) VALUES
('Salão de Festas',      'Espaço coberto para até 60 pessoas, com cozinha de apoio.', 60, '10:00:00', '22:00:00', 'Devolver o espaço limpo. Som permitido até as 22h.'),
('Churrasqueira',        'Área externa com duas churrasqueiras e mesas.',             30, '10:00:00', '22:00:00', 'Carvão e utensílios por conta do morador.'),
('Quadra Poliesportiva', 'Quadra descoberta para futsal, vôlei e basquete.',          20, '08:00:00', '22:00:00', 'Uso de calçado adequado é obrigatório.'),
('Espaço Gourmet',       'Sala climatizada com mesa para 12 lugares.',                12, '10:00:00', '22:00:00', 'Reserva limitada a uma por final de semana.'),
('Sala de Reuniões',     'Sala para reuniões de condomínio e home office.',           10, '08:00:00', '20:00:00', 'Agendar com antecedência mínima de 24 horas.');

-- ---------------------------------------------------------------------------
-- Avisos
-- As datas são relativas ao momento da importação, para a lista do aplicativo
-- não abrir com comunicados de meses atrás.
-- ---------------------------------------------------------------------------
INSERT INTO avisos (titulo, conteudo, autor, prioridade, data_publicacao, ativo) VALUES
('Manutenção dos elevadores na quinta-feira',
 'A manutenção preventiva dos elevadores dos blocos A e B acontece na quinta-feira, das 8h às 12h. Durante o período apenas um elevador por bloco ficará em operação. Pedimos que evitem mudanças nesse horário.',
 'Administração', 'ALTA', DATE_SUB(NOW(), INTERVAL 3 HOUR), b'1'),
('Assembleia ordinária dia 20',
 'A assembleia ordinária para aprovação da previsão orçamentária do próximo exercício acontece no dia 20, às 19h30, no salão de festas. A pauta completa está afixada no mural da portaria.',
 'Síndico', 'NORMAL', DATE_SUB(NOW(), INTERVAL 1 DAY), b'1'),
('Limpeza da caixa d''água - interrupção do fornecimento',
 'Na próxima terça-feira o fornecimento de água será interrompido das 9h às 14h para a limpeza semestral das caixas d''água. Recomendamos armazenar água para o consumo durante o período.',
 'Administração', 'URGENTE', DATE_SUB(NOW(), INTERVAL 2 DAY), b'1'),
('Nova regra para uso da churrasqueira',
 'A partir deste mês a churrasqueira deve ser reservada pelo aplicativo. A reserva presencial na portaria fica descontinuada. Cada unidade pode reservar até duas vezes por mês.',
 'Administração', 'NORMAL', DATE_SUB(NOW(), INTERVAL 5 DAY), b'1'),
('Coleta seletiva às segundas e quintas',
 'Lembramos que a coleta seletiva passa às segundas e quintas pela manhã. Os recicláveis devem ser depositados no container azul do subsolo.',
 'Administração', 'NORMAL', DATE_SUB(NOW(), INTERVAL 9 DAY), b'1');

-- ---------------------------------------------------------------------------
-- Uma reserva e uma ocorrência de exemplo, no cadastro da Ana
--
-- Os ids vêm de subconsulta em vez de número fixo, para o script não depender
-- da ordem em que os registros acima foram inseridos.
-- A reserva fica seis dias à frente, então continua no futuro em qualquer dia
-- que o script for importado.
-- ---------------------------------------------------------------------------
INSERT INTO reservas (morador_id, area_id, data, hora_inicio, hora_fim, status, criada_em) VALUES
((SELECT id FROM moradores    WHERE email = 'ana.souza@email.com'),
 (SELECT id FROM areas_comuns WHERE nome  = 'Salão de Festas'),
 DATE_ADD(CURDATE(), INTERVAL 6 DAY), '18:00:00', '20:00:00', 'CONFIRMADA', NOW());

INSERT INTO ocorrencias (protocolo, morador_id, categoria, titulo, descricao, status,
                         data_abertura, data_atualizacao, resposta) VALUES
('20260901-1042',
 (SELECT id FROM moradores WHERE email = 'ana.souza@email.com'),
 'Iluminação',
 'Lâmpada queimada na garagem',
 'A lâmpada da vaga 42, no subsolo 1, está queimada há três dias. À noite o corredor fica bem escuro.',
 'EM_ANDAMENTO',
 DATE_SUB(NOW(), INTERVAL 2 DAY),
 DATE_SUB(NOW(), INTERVAL 20 HOUR),
 'Material solicitado ao fornecedor. Troca prevista para esta semana.');


-- ===========================================================================
-- CONFERÊNCIA
-- Rode esta consulta depois da importação. O resultado esperado está abaixo.
-- ===========================================================================
SELECT 'moradores'    AS tabela, COUNT(*) AS registros FROM moradores
UNION ALL SELECT 'avisos',       COUNT(*) FROM avisos
UNION ALL SELECT 'areas_comuns', COUNT(*) FROM areas_comuns
UNION ALL SELECT 'reservas',     COUNT(*) FROM reservas
UNION ALL SELECT 'ocorrencias',  COUNT(*) FROM ocorrencias;

-- Esperado:
--   moradores    5
--   avisos       5
--   areas_comuns 5
--   reservas     1
--   ocorrencias  1
