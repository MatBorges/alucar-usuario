package br.ufms.facom.alucar.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Ponto unico de acesso ao banco de dados (MySQL).
 *
 * Todos os DAOs obtem a conexao por aqui. A configuracao tem valores
 * padrao embutidos, que servem para o container Docker usado no
 * desenvolvimento, e pode ser sobrescrita por variaveis de ambiente sem
 * recompilar:
 *
 *   ALUCAR_DB_HOST       (padrao localhost)
 *   ALUCAR_DB_PORT       (padrao 3434)
 *   ALUCAR_DB_NAME       (padrao alucar)
 *   ALUCAR_DB_USER       (padrao root)
 *   ALUCAR_DB_PASSWORD   (padrao 123)
 *   ALUCAR_DB_SSL_MODE   (padrao DISABLED)
 */
public final class ConexaoBD {

    private static final String HOST = obterValor("ALUCAR_DB_HOST", "localhost");
    private static final String PORTA = obterValor("ALUCAR_DB_PORT", "3434");
    private static final String BANCO = obterValor("ALUCAR_DB_NAME", "alucar");
    private static final String USUARIO = obterValor("ALUCAR_DB_USER", "root");
    private static final String SENHA = obterValor("ALUCAR_DB_PASSWORD", "123");

    /** O MySQL do container roda sem TLS. */
    private static final String SSL_MODE = obterValor("ALUCAR_DB_SSL_MODE", "DISABLED");

    private static final String URL = montarUrl();

    private ConexaoBD() {
    }

    public static Connection obterConexao() throws DAOException {
        try {
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (SQLException e) {
            throw new DAOException(montarMensagemDeFalha(e), e);
        }
    }

    /** Texto exibido na barra inferior da tela principal. */
    public static String descreverConexaoAtiva() {
        return "MySQL em " + HOST + ":" + PORTA + "/" + BANCO;
    }

    private static String montarUrl() {
        StringBuilder url = new StringBuilder("jdbc:mysql://")
                .append(HOST).append(":").append(PORTA).append("/").append(BANCO)
                .append("?sslMode=").append(SSL_MODE)
                .append("&connectionTimeZone=America/Campo_Grande")
                .append("&forceConnectionTimeZoneToSession=true")
                .append("&characterEncoding=UTF-8")
                .append("&connectTimeout=10000")
                .append("&socketTimeout=30000");

        // Necessario somente quando a conexao nao usa TLS.
        if ("DISABLED".equalsIgnoreCase(SSL_MODE)) {
            url.append("&allowPublicKeyRetrieval=true");
        }
        return url.toString();
    }

    private static String obterValor(String variavel, String padrao) {
        String valor = System.getenv(variavel);
        return (valor == null || valor.isBlank()) ? padrao : valor;
    }

    /**
     * Traduz as falhas mais comuns em orientacoes concretas, em vez de
     * repassar a mensagem crua do driver.
     */
    private static String montarMensagemDeFalha(SQLException e) {
        String detalhe = e.getMessage() == null ? "" : e.getMessage();

        if (detalhe.contains("Communications link failure")
                || detalhe.contains("Connection refused")
                || detalhe.contains("Could not connect")) {

            return "Nao foi possivel conectar ao MySQL em " + HOST + ":" + PORTA + ".\n\n"
                    + "Verifique se o container do banco esta no ar\n"
                    + "(docker compose ps) e se a porta " + PORTA + " confere.";
        }
        if (detalhe.contains("Access denied")) {
            return "Usuario ou senha do banco incorretos.\n\n"
                    + "Usuario configurado: " + USUARIO;
        }
        if (detalhe.contains("Unknown database")) {
            return "O banco '" + BANCO + "' nao existe no servidor " + HOST + ":" + PORTA + ".\n\n"
                    + "Crie o banco e rode o schema.sql.";
        }
        if (detalhe.contains("Public Key Retrieval is not allowed")) {
            return "O driver recusou a autenticacao.\n\n"
                    + "Confirme que a URL contem allowPublicKeyRetrieval=true\n"
                    + "quando o SSL esta desligado.";
        }
        return "Falha ao conectar ao banco de dados: " + detalhe;
    }
}
