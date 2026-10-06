package br.ufms.facom.alucar.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Ponto unico de obtencao de conexoes com o banco de dados.
 *
 * Centralizar a conexao aqui evita que a string de conexao fique espalhada
 * pelos DAOs: se o banco mudar de servidor, apenas esta classe e alterada.
 *
 * CONEXAO PRINCIPAL: TiDB Cloud Serverless (nao exige Docker nem XAMPP, e o
 * mesmo banco para todo o grupo).
 *
 * CONEXAO DE CONTINGENCIA: um MySQL local, usado apenas se a principal
 * falhar - util para apresentar o trabalho sem depender da internet. Ela
 * NAO e silenciosa: quando entra em acao, o sistema avisa no console, para
 * que ninguem trabalhe horas sobre o banco local achando que esta no da
 * nuvem.
 *
 * Toda a configuracao pode ser sobrescrita por variavel de ambiente, sem
 * recompilar.
 *
 * Sobre os parametros da URL:
 * - sslMode ........................ VERIFY_IDENTITY no TiDB Cloud, que exige
 *                                    TLS e usa CA publica ja reconhecida pelo
 *                                    truststore da JVM; DISABLED no MySQL
 *                                    local, que nao tem certificado
 * - allowPublicKeyRetrieval=true ... exigido pelo caching_sha2_password do
 *                                    MySQL 8 apenas quando o SSL esta
 *                                    desligado; desnecessario com TLS
 * - connectionTimeZone ............. o servidor roda em UTC; sem isso as datas
 *                                    entram deslocadas
 */
public final class ConexaoBD {

    // ---------- Conexao principal: TiDB Cloud ----------

    private static final String HOST =
            obterValor("ALUCAR_DB_HOST", "gateway01.sa-east-1.prod.aws.tidbcloud.com");
    private static final String PORTA = obterValor("ALUCAR_DB_PORT", "4000");
    private static final String BANCO = obterValor("ALUCAR_DB_NAME", "alucar");
    private static final String USUARIO = obterValor("ALUCAR_DB_USER", "37xCGj4XKVsW6YU.root");
    private static final String SENHA = obterValor("ALUCAR_DB_PASSWORD", "TsZLi6DML7XLXo8");
//    private static final String SENHA = obterValor("ALUCAR_DB_PASSWORD", "TsZLi6DML7XLXo8e");

    /** O TiDB Cloud recusa conexao sem TLS: sslMode=DISABLED nao funciona la. */
    private static final String SSL_MODE = obterValor("ALUCAR_DB_SSL_MODE", "VERIFY_IDENTITY");

    // ---------- Conexao de contingencia: MySQL local ----------

    private static final boolean USAR_CONTINGENCIA =
            Boolean.parseBoolean(obterValor("ALUCAR_DB_FALLBACK", "false"));

    private static final String HOST_LOCAL = obterValor("ALUCAR_DB_LOCAL_HOST", "localhost");
    private static final String PORTA_LOCAL = obterValor("ALUCAR_DB_LOCAL_PORT", "3434");
    private static final String USUARIO_LOCAL = obterValor("ALUCAR_DB_LOCAL_USER", "root");
    private static final String SENHA_LOCAL = obterValor("ALUCAR_DB_LOCAL_PASSWORD", "123");
//    private static final String SENHA_LOCAL = obterValor("ALUCAR_DB_LOCAL_PASSWORD", "123");



    private static final String URL_PRINCIPAL =
            montarUrl(HOST, PORTA, SSL_MODE);

    private static final String URL_CONTINGENCIA =
            montarUrl(HOST_LOCAL, PORTA_LOCAL, "DISABLED");

    /** Fica true depois que a aplicacao cai para o banco local. */
    private static boolean usandoContingencia = false;

    private ConexaoBD() {
    }

    public static Connection obterConexao() throws DAOException {
        try {
            return DriverManager.getConnection(URL_PRINCIPAL, USUARIO, SENHA);

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
                throw new DAOException(montarMensagemDasDuasFalhas(falhaPrincipal, falhaLocal),
                        falhaPrincipal);
            }
        }
    }

    /** Indica se a aplicacao esta operando sobre o banco local de contingencia. */
    public static boolean estaUsandoContingencia() {
        return usandoContingencia;
    }

    public static String descreverConexaoAtiva() {
        return usandoContingencia
                ? "MySQL local em " + HOST_LOCAL + ":" + PORTA_LOCAL + " (contingencia)"
                : "TiDB Cloud em " + HOST + ":" + PORTA;
    }

    private static String montarUrl(String host, String porta, String sslMode) {
        StringBuilder url = new StringBuilder("jdbc:mysql://")
                .append(host).append(":").append(porta).append("/").append(BANCO)
                .append("?sslMode=").append(sslMode)
                .append("&connectionTimeZone=America/Campo_Grande")
                .append("&forceConnectionTimeZoneToSession=true")
                .append("&characterEncoding=UTF-8")
                .append("&connectTimeout=10000")
                .append("&socketTimeout=30000");

        // Necessario somente quando a conexao nao usa TLS.
        if ("DISABLED".equalsIgnoreCase(sslMode)) {
            url.append("&allowPublicKeyRetrieval=true");
        }
        return url.toString();
    }

    /**
     * Avisa no console que a aplicacao caiu para o banco local. O aviso sai
     * apenas na primeira vez, para nao poluir a saida a cada consulta.
     */
    private static void avisarUsoDaContingencia(SQLException falhaPrincipal) {
        if (usandoContingencia) {
            return;
        }
        usandoContingencia = true;

        System.err.println("=".repeat(70));
        System.err.println("ATENCAO: o TiDB Cloud nao respondeu. A aplicacao esta operando");
        System.err.println("sobre o MySQL LOCAL em " + HOST_LOCAL + ":" + PORTA_LOCAL + ".");
        System.err.println("Os dados gravados agora NAO estarao no banco do grupo.");
        System.err.println("Motivo da falha na nuvem: " + falhaPrincipal.getMessage());
        System.err.println("=".repeat(70));
    }

    /**
     * Permite sobrescrever a configuracao por variavel de ambiente sem
     * recompilar.
     */
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
                return "Nao foi possivel alcancar o TiDB Cloud em " + HOST + ":" + PORTA + ".\n\n"
                        + "Verifique:\n"
                        + "- se ha conexao com a internet;\n"
                        + "- se o cluster nao esta pausado (clusters Serverless hibernam\n"
                        + "  apos um periodo sem uso; a primeira conexao pode demorar);\n"
                        + "- se o seu IP esta liberado na lista de acesso do cluster.";
            }
            return "Nao foi possivel alcancar o MySQL em " + HOST + ":" + PORTA + ".\n\n"
                    + "Verifique no WSL:\n"
                    + "  docker ps            (o container lbd_mysql esta rodando?)\n"
                    + "  docker compose up -d (para subir o container)";
        }
        if (detalhe.contains("SSL") || detalhe.contains("TLS")
                || detalhe.contains("certificate")) {
            return "Falha na negociacao TLS com o servidor.\n\n"
                    + "Modo SSL atual: " + SSL_MODE + "\n"
                    + "O TiDB Cloud exige TLS: use VERIFY_IDENTITY (ou REQUIRED, se\n"
                    + "houver problema com o certificado).\n"
                    + "Um MySQL local nao usa TLS: use DISABLED.";
        }
        if (detalhe.contains("Public Key Retrieval is not allowed")) {
            return "O driver recusou a autenticacao. Confirme que a URL de conexao\n"
                    + "contem allowPublicKeyRetrieval=true quando o SSL esta desligado.";
        }
        if (detalhe.contains("Access denied")) {
            return "Usuario ou senha do banco incorretos.\n\n"
                    + "Usuario configurado: " + USUARIO + "\n"
                    + "No TiDB Cloud Serverless o usuario tem o formato <prefixo>.root -\n"
                    + "confira no botao Connect do console.";
        }
        if (detalhe.contains("Unknown database")) {
            return "O banco '" + BANCO + "' nao existe no servidor.\n\n"
                    + "Crie-o pelo SQL Editor do console do TiDB Cloud, executando o\n"
                    + "conteudo de src/main/resources/schema.sql.";
        }
        return "Falha ao conectar ao banco de dados: " + detalhe;
    }
}
