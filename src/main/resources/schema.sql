-- ============================================================
-- Alucar - Sistema de Gestao de Locadora de Veiculos
-- Script de criacao do banco (iteracao 1)
-- Compativel com MySQL 8 e TiDB Cloud
-- ============================================================

CREATE DATABASE IF NOT EXISTS alucar
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE alucar;

-- ------------------------------------------------------------
-- RF04 - Usuarios
-- Mapeamento da heranca Usuario -> Atendente / Gerente / Mecanico
-- pela estrategia de TABELA UNICA, com a coluna tipo_usuario
-- atuando como discriminador.
--
-- 'ativo' implementa a EXCLUSAO LOGICA: nenhum registro e removido
-- fisicamente, para nao quebrar as chaves estrangeiras das locacoes
-- ja realizadas nem apagar o historico.
--
-- 'data_ultima_troca_senha' atende ao RNF03: comparada com o prazo
-- configurado em ParametrosSistema, define se a senha expirou.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    matricula                VARCHAR(20)  NOT NULL,
    cpf                      VARCHAR(11)  NOT NULL,
    nome                     VARCHAR(100) NOT NULL,
    login                    VARCHAR(30)  NOT NULL,
    senha                    VARCHAR(255) NOT NULL,
    tipo_usuario             VARCHAR(15)  NOT NULL,
    data_ultima_troca_senha  DATE         NOT NULL,
    ativo                    BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_usuario PRIMARY KEY (matricula),
    CONSTRAINT uk_usuario_login UNIQUE (login),
    CONSTRAINT uk_usuario_cpf UNIQUE (cpf),
    CONSTRAINT ck_usuario_tipo CHECK (tipo_usuario IN ('GERENTE', 'ATENDENTE', 'MECANICO'))
);

-- Nota para TiDB Cloud: o suporte a CHECK constraint depende da versao e da
-- variavel tidb_enable_check_constraint. Se o CREATE TABLE acima falhar ou a
-- restricao for ignorada, troque a coluna por
--   tipo_usuario ENUM('GERENTE','ATENDENTE','MECANICO') NOT NULL
-- e remova a linha do CHECK. A validacao do tipo ja e garantida pela
-- aplicacao, em ControladoraUsuario e no enum TipoUsuario.

-- ------------------------------------------------------------
-- RF01 - Clientes
-- O CPF e a chave natural. CNH tambem e unica: uma habilitacao
-- pertence a uma unica pessoa.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cliente (
    cpf              VARCHAR(11)  NOT NULL,
    nome             VARCHAR(100) NOT NULL,
    cnh              VARCHAR(11)  NOT NULL,
    validade_cnh     DATE         NOT NULL,
    data_nascimento  DATE         NOT NULL,
    telefone         VARCHAR(15),
    email            VARCHAR(100),
    endereco         VARCHAR(200),
    ativo            BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_cliente PRIMARY KEY (cpf),
    CONSTRAINT uk_cliente_cnh UNIQUE (cnh)
);

-- ------------------------------------------------------------
-- RF03 - Categorias de veiculo
-- O nome e a chave natural, conforme o mapeamento objeto-relacional
-- do grupo. A tabela veiculo tera FK apontando para ele, por isso o
-- nome nao e alteravel pela aplicacao.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categoria_veiculo (
    nome                VARCHAR(50)    NOT NULL,
    descricao           VARCHAR(200),
    valor_base_diaria   NUMERIC(10,2)  NOT NULL,
    ativo               BOOLEAN        NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_categoria_veiculo PRIMARY KEY (nome),
    CONSTRAINT ck_categoria_valor CHECK (valor_base_diaria > 0)
);

-- ------------------------------------------------------------
-- MIGRACAO - execute apenas se as tabelas ja existiam sem as
-- colunas novas. O MySQL nao aceita ADD COLUMN IF NOT EXISTS,
-- entao rodar duas vezes gera erro de coluna duplicada, que pode
-- ser ignorado com seguranca.
--
-- As colunas cpf e data_ultima_troca_senha entram primeiro como
-- anulaveis, porque uma coluna NOT NULL nao pode ser adicionada a
-- uma tabela que ja tem linhas sem valor para ela. Depois de
-- preencher os registros existentes, elas passam a NOT NULL.
-- ------------------------------------------------------------
-- ALTER TABLE usuario ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;
-- ALTER TABLE cliente ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;
--
-- ALTER TABLE usuario ADD COLUMN cpf VARCHAR(11) NULL;
-- ALTER TABLE usuario ADD COLUMN data_ultima_troca_senha DATE NULL;
-- UPDATE usuario SET cpf = '10433218100' WHERE matricula = '0001';
-- UPDATE usuario SET data_ultima_troca_senha = CURRENT_DATE
--     WHERE data_ultima_troca_senha IS NULL;
-- ALTER TABLE usuario MODIFY COLUMN cpf VARCHAR(11) NOT NULL;
-- ALTER TABLE usuario MODIFY COLUMN data_ultima_troca_senha DATE NOT NULL;
-- ALTER TABLE usuario ADD CONSTRAINT uk_usuario_cpf UNIQUE (cpf);

-- Opcional: liberar o banco alucar para outro usuario do servidor.
-- GRANT ALL PRIVILEGES ON alucar.* TO 'mateus_lbd'@'%';
-- FLUSH PRIVILEGES;

-- ------------------------------------------------------------
-- Usuario inicial para permitir o primeiro acesso.
-- Senha em texto puro: alucar123
-- Valor gravado: hash SHA-256 gerado por SenhaUtil.gerarHash
-- CPF ficticio, com digito verificador valido.
-- ------------------------------------------------------------
INSERT INTO usuario (matricula, cpf, nome, login, senha, tipo_usuario,
                     data_ultima_troca_senha, ativo)
SELECT '0001',
       '10433218100',
       'Administrador do Sistema',
       'admin',
       '03aa71dbf30eb045c6eb87f47845392384dbf442bc94a6bcd93933a13f71f07d',
       'GERENTE',
       CURRENT_DATE,
       TRUE
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE matricula = '0001');

-- ------------------------------------------------------------
-- Categorias iniciais, para que a frota possa ser cadastrada.
-- ------------------------------------------------------------
INSERT INTO categoria_veiculo (nome, descricao, valor_base_diaria, ativo)
SELECT 'Compacto', 'Veiculos de pequeno porte, economicos, ate 4 passageiros', 120.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM categoria_veiculo WHERE nome = 'Compacto');

INSERT INTO categoria_veiculo (nome, descricao, valor_base_diaria, ativo)
SELECT 'Sedan', 'Veiculos de porte medio, porta-malas amplo, 5 passageiros', 180.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM categoria_veiculo WHERE nome = 'Sedan');

INSERT INTO categoria_veiculo (nome, descricao, valor_base_diaria, ativo)
SELECT 'SUV', 'Veiculos altos, tracao reforcada, 5 a 7 passageiros', 260.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM categoria_veiculo WHERE nome = 'SUV');

INSERT INTO categoria_veiculo (nome, descricao, valor_base_diaria, ativo)
SELECT 'Utilitario', 'Veiculos de carga, picapes e furgoes', 300.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM categoria_veiculo WHERE nome = 'Utilitario');
