-- ============================================================
-- Alucar - Sistema de Gestao de Locadora de Veiculos
-- Script de criacao da tabela de usuarios (iteracao 1 - RF04)
-- Banco: MySQL 8
-- ============================================================

CREATE DATABASE IF NOT EXISTS alucar
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE alucar;

-- Mapeamento da heranca Usuario -> Atendente / Gerente / Mecanico
-- pela estrategia de TABELA UNICA, com a coluna tipo_usuario
-- atuando como discriminador.
CREATE TABLE IF NOT EXISTS usuario (
    matricula     VARCHAR(20)  NOT NULL,
    nome          VARCHAR(100) NOT NULL,
    login         VARCHAR(30)  NOT NULL,
    senha         VARCHAR(255) NOT NULL,
    tipo_usuario  VARCHAR(15)  NOT NULL,

    CONSTRAINT pk_usuario PRIMARY KEY (matricula),
    CONSTRAINT uk_usuario_login UNIQUE (login),
    CONSTRAINT ck_usuario_tipo CHECK (tipo_usuario IN ('GERENTE', 'ATENDENTE', 'MECANICO'))
);

-- Opcional: liberar o banco alucar para o usuario mateus_lbd, caso voce
-- prefira nao usar o root na aplicacao. Execute como root.
-- GRANT ALL PRIVILEGES ON alucar.* TO 'mateus_lbd'@'%';
-- FLUSH PRIVILEGES;

-- Usuario inicial para permitir o primeiro acesso.
-- Senha em texto puro: alucar123
-- Valor gravado: hash SHA-256 gerado por SenhaUtil.gerarHash
INSERT INTO usuario (matricula, nome, login, senha, tipo_usuario)
SELECT '0001',
       'Administrador do Sistema',
       'admin',
       '03aa71dbf30eb045c6eb87f47845392384dbf442bc94a6bcd93933a13f71f07d',
       'GERENTE'
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE matricula = '0001');
