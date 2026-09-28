-- ============================================================
-- Alucar - Sistema de Gestao de Locadora de Veiculos
-- Script de criacao do banco (iteracao 1)
-- Banco: MySQL 8
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
-- A coluna 'ativo' implementa a EXCLUSAO LOGICA: nenhum registro e
-- removido fisicamente, para nao quebrar as chaves estrangeiras das
-- locacoes ja realizadas nem apagar o historico.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    matricula     VARCHAR(20)  NOT NULL,
    nome          VARCHAR(100) NOT NULL,
    login         VARCHAR(30)  NOT NULL,
    senha         VARCHAR(255) NOT NULL,
    tipo_usuario  VARCHAR(15)  NOT NULL,
    ativo         BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_usuario PRIMARY KEY (matricula),
    CONSTRAINT uk_usuario_login UNIQUE (login),
    CONSTRAINT ck_usuario_tipo CHECK (tipo_usuario IN ('GERENTE', 'ATENDENTE', 'MECANICO'))
);

-- Nota para TiDB Cloud: o suporte a CHECK constraint depende da versao e da
-- variavel tidb_enable_check_constraint. Se o CREATE TABLE acima falhar ou a
-- restricao for ignorada, remova a linha do CHECK - a validacao do tipo ja e
-- garantida pela aplicacao, em ControladoraUsuario e no enum TipoUsuario.

-- ------------------------------------------------------------
-- RF03 - Clientes
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
-- MIGRACAO - execute apenas se as tabelas ja existiam SEM a
-- coluna 'ativo'. O MySQL nao aceita ADD COLUMN IF NOT EXISTS,
-- entao rodar duas vezes gera erro de coluna duplicada, que pode
-- ser ignorado com seguranca.
-- ------------------------------------------------------------
-- ALTER TABLE usuario ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;
-- ALTER TABLE cliente ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;

-- Opcional: liberar o banco alucar para o usuario mateus_lbd, caso voce
-- prefira nao usar o root na aplicacao. Execute como root.
-- GRANT ALL PRIVILEGES ON alucar.* TO 'mateus_lbd'@'%';
-- FLUSH PRIVILEGES;

-- Usuario inicial para permitir o primeiro acesso.
-- Senha em texto puro: alucar123
-- Valor gravado: hash SHA-256 gerado por SenhaUtil.gerarHash
INSERT INTO usuario (matricula, nome, login, senha, tipo_usuario, ativo)
SELECT '0001',
       'Administrador do Sistema',
       'admin',
       '03aa71dbf30eb045c6eb87f47845392384dbf442bc94a6bcd93933a13f71f07d',
       'GERENTE',
       TRUE
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE matricula = '0001');
