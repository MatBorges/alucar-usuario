package br.ufms.facom.alucar.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;



public final class ConexaoBD {

    // ---------- Conexao TiDB Cloud ----------

//    Banco Pedro:
//    host: gateway01.sa-east-1.prod.aws.tidbcloud.com
//    porta: 4000
//    banco: alucar
//    user: 37xCGj4XKVsW6YU.root
//    s: TsZLi6DML7XLXo8e

    private static final String HOST =
            obterValor("ALUCAR_DB_HOST", "gateway01.ap-northeast-1.prod.aws.tidbcloud.com");
    private static final String PORTA = obterValor("ALUCAR_DB_PORT", "4000");
    private static final String BANCO = obterValor("ALUCAR_DB_NAME", "alucar");
    private static final String USUARIO = obterValor("ALUCAR_DB_USER", "EkuJSQGHMQUgwcb.root");
    private static final String SENHA = obterValor("ALUCAR_DB_PASSWORD", "8tx4t7P9CksUfj0H");

    /** O TiDB Cloud recusa conexao sem TLS: sslMode=DISABLED nao funciona la. */
    private static final String SSL_MODE = obterValor("ALUCAR_DB_SSL_MODE", "VERIFY_IDENTITY");

    // ---------- Conexao MySQL local ----------

    private static final boolean PREFERIR_LOCAL =
            Boolean.parseBoolean(obterValor("ALUCAR_PREFERIR_LOCAL", "true"));

    private static final boolean USAR_CONTINGENCIA =
            Boolean.parseBoolean(obterValor("ALUCAR_DB_FALLBACK", "true"));

    private static final String HOST_LOCAL = obterValor("ALUCAR_DB_LOCAL_HOST", "localhost");
    private static final String PORTA_LOCAL = obterValor("ALUCAR_DB_LOCAL_PORT", "3306");
    private static final String USUARIO_LOCAL = obterValor("ALUCAR_DB_LOCAL_USER", "root");
    private static final String SENHA_LOCAL = obterValor("ALUCAR_DB_LOCAL_PASSWORD", "Pedro561561561!");



    private static final String URL_PRINCIPAL =
            montarUrl(HOST, PORTA, SSL_MODE);

    private static final String URL_CONTINGENCIA =
            montarUrl(HOST_LOCAL, PORTA_LOCAL, "DISABLED");

    /** Fica true depois que a aplicacao opera sobre o banco local. */
    private static boolean usandoContingencia = false;

    private ConexaoBD() {
    }

    public static Connection obterConexao() throws DAOException {
        if (PREFERIR_LOCAL) {
            try {
                Connection conn = DriverManager.getConnection(URL_CONTINGENCIA, USUARIO_LOCAL, SENHA_LOCAL);
                usandoContingencia = true;
                return conn;
            } catch (SQLException falhaLocal) {
                try {
                    Connection conn = DriverManager.getConnection(URL_PRINCIPAL, USUARIO, SENHA);
                    usandoContingencia = false;
                    return conn;
                } catch (SQLException falhaPrincipal) {
                    throw new DAOException(montarMensagemDasDuasFalhas(falhaPrincipal, falhaLocal), falhaPrincipal);
                }
            }
        } else {
            try {
                Connection conn = DriverManager.getConnection(URL_PRINCIPAL, USUARIO, SENHA);
                usandoContingencia = false;
                return conn;
            } catch (SQLException falhaPrincipal) {
                if (!USAR_CONTINGENCIA) {
                    throw new DAOException(montarMensagemDeFalha(falhaPrincipal), falhaPrincipal);
                }
                try {
                    Connection conexaoLocal =
                            DriverManager.getConnection(URL_CONTINGENCIA, USUARIO_LOCAL, SENHA_LOCAL);
                    avisarUsoDaContingencia(falhaPrincipal);
                    return conexaoLocal;
                } catch (SQLException falhaLocal) {
                    throw new DAOException(montarMensagemDasDuasFalhas(falhaPrincipal, falhaLocal), falhaPrincipal);
                }
            }
        }
    }

    /** Indica se a aplicacao esta operando sobre o banco local. */
    public static boolean estaUsandoContingencia() {
        return usandoContingencia;
    }

    public static String descreverConexaoAtiva() {
        return usandoContingencia
                ? "MySQL local em " + HOST_LOCAL + ":" + PORTA_LOCAL
                : "TiDB Cloud em " + HOST + ":" + PORTA;
    }

    private static String montarUrl(String host, String porta, String sslMode) {
        StringBuilder url = new StringBuilder("jdbc:mysql://")
                .append(host).append(":").append(porta).append("/").append(BANCO)
                .append("?sslMode=").append(sslMode)
                .append("&connectionTimeZone=-04:00")
                .append("&forceConnectionTimeZoneToSession=true")
                .append("&characterEncoding=UTF-8")
                .append("&connectTimeout=3000")
                .append("&socketTimeout=30000");

        // Necessario somente quando a conexao nao usa TLS.
        if ("DISABLED".equalsIgnoreCase(sslMode)) {
            url.append("&allowPublicKeyRetrieval=true");
        }
        return url.toString();
    }

//    Avisa se o banco usado é o local
    private static void avisarUsoDaContingencia(SQLException falhaPrincipal) {
        if (usandoContingencia) {
            return;
        }
        usandoContingencia = true;

        System.err.println("=".repeat(70));
        System.err.println("ATENCAO: o TiDB Cloud nao respondeu. A aplicacao esta operando");
        System.err.println("sobre o MySQL LOCAL em " + HOST_LOCAL + ":" + PORTA_LOCAL + ".");
        System.err.println("Falha: " + falhaPrincipal.getMessage());
        System.err.println("=".repeat(70));
    }

//   Lê variavel de ambiente
    private static String obterValor(String variavel, String padrao) {
        String valor = System.getenv(variavel);
        return (valor == null || valor.isBlank()) ? padrao : valor;
    }

    private static String montarMensagemDasDuasFalhas(SQLException falhaPrincipal,
                                                      SQLException falhaLocal) {
        return montarMensagemDeFalha(falhaPrincipal)
                + "\n\n----------------------------------------\n"
                + "O banco local de contingencia tambem nao respondeu em "
                + HOST_LOCAL + ":" + PORTA_LOCAL + ".\n"
                + "Detalhe: " + falhaLocal.getMessage();
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

            if (HOST.contains("tidbcloud.com")) {
                return "Nao foi possivel conectar ao TiDB  " + HOST + ":" + PORTA + ".\n\n";
            }
            return "Nao foi possivel conectar ao MySQL " + HOST + ":" + PORTA + ".\n\n";
        }
        if (detalhe.contains("SSL") || detalhe.contains("TLS")
                || detalhe.contains("certificate")) {
            return "Falha no TLS com o servidor.\n\n";
        }
        if (detalhe.contains("Public Key Retrieval is not allowed")) {
            return "O driver recusou a autenticacao. Confirme que a URL de conexao\n"
                    + "contem allowPublicKeyRetrieval=true quando o SSL esta desligado.";
        }
        if (detalhe.contains("Access denied")) {
            return "Usuario ou senha do banco incorretos.\n\n"
                    + "Usuario configurado: " + USUARIO + "\n";
        }
        if (detalhe.contains("Unknown database")) {
            return "O banco '" + BANCO + "' nao existe no servidor.\n\n";
        }
        return "Falha ao conectar ao banco de dados: " + detalhe;
    }
}
