-- ====================================================================
-- Alucar - Recriacao do banco a partir do zero
--
-- ATENCAO: este script APAGA TODOS OS DADOS das cinco tabelas.
-- Use somente no banco de desenvolvimento/local, nunca no banco que
-- vai ser usado na apresentacao com dados que voce queira manter.
--
-- Por que ele existe:
-- o schema.sql usa CREATE TABLE IF NOT EXISTS. Se as tabelas ja
-- existem com um esquema antigo, ele NAO corrige nada - simplesmente
-- nao faz nada. Por isso e preciso derrubar as tabelas antes.
--
-- Como usar:
--   1) rode este arquivo
--   2) rode o schema.sql em seguida (ele recria tudo, ja com as
--      colunas da 2a iteracao e com o usuario administrador)
--
-- Nao e preciso rodar o migracao_iteracao2.sql depois: o schema.sql
-- ja cria a tabela locacao completa. A migracao serve apenas para um
-- banco que tem dados da 1a iteracao e voce quer preservar.
-- ====================================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS locacao;
DROP TABLE IF EXISTS veiculo;
DROP TABLE IF EXISTS categoria_veiculo;
DROP TABLE IF EXISTS cliente;
DROP TABLE IF EXISTS usuario;

SET FOREIGN_KEY_CHECKS = 1;

-- Conferencia: depois de rodar o schema.sql, estas cinco tabelas
-- devem aparecer, e categoria_veiculo deve ter a coluna 'nome'.
--
-- SHOW TABLES;
-- SHOW COLUMNS FROM categoria_veiculo;
-- SHOW COLUMNS FROM locacao;
