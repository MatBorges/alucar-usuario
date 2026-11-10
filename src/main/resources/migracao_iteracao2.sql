-- ====================================================================
-- Alucar - Migracao da 2a iteracao: Registrar Devolucao (RF09)
--
-- Rode este script UMA VEZ num banco que ja tenha os dados da 1a
-- iteracao. Para um banco novo nao e preciso: o schema.sql ja cria a
-- tabela locacao com estas colunas.
--
-- Os nomes das colunas seguem os atributos citados no contrato de
-- operacao de registrarDevolucao.
--
-- Todas aceitam NULL de proposito: so sao preenchidas no momento da
-- devolucao, e as locacoes ATIVAS continuam validas com elas vazias.
-- ====================================================================

ALTER TABLE locacao ADD COLUMN data_hora_devolucao_efetiva DATETIME       NULL;
ALTER TABLE locacao ADD COLUMN km_final                    INT            NULL;
ALTER TABLE locacao ADD COLUMN valor_final                 DECIMAL(10, 2) NULL;
ALTER TABLE locacao ADD COLUMN vistoria                    VARCHAR(500)   NULL;
ALTER TABLE locacao ADD COLUMN custo_avaria                DECIMAL(10, 2) NULL;

-- Sobre valor_final: a caucao NAO e abatida dele (RN03 - a caucao e
-- liberada ou retida a parte), entao o valor final nunca fica negativo.
-- Ainda assim nao se declara CHECK aqui, porque o TiDB so aceita CHECK
-- com tidb_enable_check_constraint ligado - a validacao fica na
-- Controladora, como nas demais regras.

-- Conferencia (opcional):
-- SELECT id_locacao, status_locacao, data_hora_devolucao_efetiva,
--        km_final, valor_final, custo_avaria, vistoria
--   FROM locacao;
