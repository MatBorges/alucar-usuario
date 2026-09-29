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
 * Suporta os dois cenarios do projeto sem alteracao de codigo, apenas por
 * variaveis de ambiente:
 *
 * 1) MySQL local em container Docker no WSL2 (padrao)
 *    HOST=localhost  PORTA=3434  SSL=DISABLED
 *    Se a aplicacao roda no Windows e o container no WSL, "localhost"
 *    normalmente funciona. Se falhar, use o IP obtido no WSL com: hostname -I
 *
 * 2) TiDB Cloud Serverless
 *    HOST=gateway01.<regiao>.prod.aws.tidbcloud.com  PORTA=4000
 *    USUARIO=<prefixo>.root  SSL=VERIFY_IDENTITY
 *    O TiDB Cloud exige conexao TLS: sslMode=DISABLED e recusado.
 *
 * Sobre os parametros da URL:
 * - sslMode ........................ DISABLED no container local (sem
 *                                    certificado); VERIFY_IDENTITY no TiDB
 *                                    Cloud, que usa CA publica reconhecida
 *                                    pelo truststore da JVM
 * - allowPublicKeyRetrieval=true ... exigido pelo caching_sha2_password do
 *                                    MySQL 8 apenas quando o SSL esta
 *                                    desligado; desnecessario com TLS
 * - connectionTimeZone ............. o servidor roda em UTC; sem isso as datas
 *                                    entram deslocadas
 */
public final class ConexaoBD {

    private static final String HOST = obterValor("ALUCAR_DB_HOST", "localhost");
    private static final String PORTA = obterValor("ALUCAR_DB_PORT", "3434");
    private static final String BANCO = obterValor("ALUCAR_DB_NAME", "alucar");
    private static final String USUARIO = obterValor("ALUCAR_DB_USER", "root");
    private static final String SENHA = obterValor("ALUCAR_DB_PASSWORD", "123");

//    private static final String HOST = obterValor("ALUCAR_DB_HOST", "gateway01.sa-east-1.prod.aws.tidbcloud.com");
//    private static final String PORTA = obterValor("ALUCAR_DB_PORT", "4000");
//    private static final String BANCO = obterValor("ALUCAR_DB_NAME", "alucar");
//    private static final String USUARIO = obterValor("ALUCAR_DB_USER", "37xCGj4XKVsW6YU.root");
//    private static final String SENHA = obterValor("ALUCAR_DB_PASSWORD", "TsZLi6DML7XLXo8e");

    /** DISABLED para o container local; VERIFY_IDENTITY para o TiDB Cloud. */
    private static final String SSL_MODE = obterValor("ALUCAR_DB_SSL_MODE", "DISABLED");

    private static final String URL = montarUrl();

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

    private ConexaoBD() {
    }

    public static Connection obterConexao() throws DAOException {
        try {
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (SQLException e) {
            throw new DAOException(montarMensagemDeFalha(e), e);
        }
    }

    /**
     * Permite sobrescrever a configuracao por variavel de ambiente sem
     * recompilar - util porque o IP do WSL muda a cada reinicializacao.
     */
    private static String obterValor(String variavel, String padrao) {
        String valor = System.getenv(variavel);
        return (valor == null || valor.isBlank()) ? padrao : valor;
    }

    /**
     * Traduz as falhas mais comuns do cenario Docker/WSL em orientacoes
     * concretas, em vez de repassar a mensagem crua do driver.
     */
    private static String montarMensagemDeFalha(SQLException e) {
        String detalhe = e.getMessage() == null ? "" : e.getMessage();

        if (detalhe.contains("Communications link failure")
                || detalhe.contains("Connection refused")) {
            if (HOST.contains("tidbcloud.com")) {
                return "Nao foi possivel alcancar o TiDB Cloud em " + HOST + ":" + PORTA + ".\n\n"
                        + "Verifique:\n"
                        + "- se o cluster nao esta pausado (clusters Serverless hibernam\n"
                        + "  apos um periodo sem uso; a primeira conexao pode demorar);\n"
                        + "- se o seu IP esta liberado na lista de acesso do cluster;\n"
                        + "- se ALUCAR_DB_SSL_MODE esta definido como VERIFY_IDENTITY,\n"
                        + "  pois o TiDB Cloud nao aceita conexao sem TLS.";
            }
            return "Nao foi possivel alcancar o MySQL em " + HOST + ":" + PORTA + ".\n\n"
                    + "Verifique no WSL:\n"
                    + "  docker ps            (o container lbd_mysql esta rodando?)\n"
                    + "  docker compose up -d (para subir o container)\n\n"
                    + "Se o container esta rodando e o erro persiste, descubra o IP do WSL\n"
                    + "com 'hostname -I' e defina a variavel de ambiente ALUCAR_DB_HOST\n"
                    + "com esse IP.";
        }
        if (detalhe.contains("SSL") || detalhe.contains("TLS")
                || detalhe.contains("certificate")) {
            return "Falha na negociacao TLS com o servidor.\n\n"
                    + "Modo SSL atual: " + SSL_MODE + "\n"
                    + "O TiDB Cloud exige TLS: defina ALUCAR_DB_SSL_MODE=VERIFY_IDENTITY.\n"
                    + "O container local nao usa TLS: defina ALUCAR_DB_SSL_MODE=DISABLED.";
        }
        if (detalhe.contains("Public Key Retrieval is not allowed")) {
            return "O driver recusou a autenticacao. Confirme que a URL de conexao\n"
                    + "contem allowPublicKeyRetrieval=true.";
        }
        if (detalhe.contains("Access denied")) {
            return "Usuario ou senha do banco incorretos.\n\n"
                    + "Usuario configurado: " + USUARIO + "\n"
                    + "Ajuste as constantes em ConexaoBD ou defina as variaveis\n"
                    + "ALUCAR_DB_USER e ALUCAR_DB_PASSWORD.";
        }
        if (detalhe.contains("Unknown database")) {
            return "O banco '" + BANCO + "' nao existe.\n\n"
                    + "Execute o script de criacao:\n"
                    + "  docker exec -i lbd_mysql mysql -uroot -p123 < src/main/resources/schema.sql\n\n"
                    + "Ou importe src/main/resources/schema.sql pelo phpMyAdmin\n"
                    + "em http://localhost:4343";
        }
        return "Falha ao conectar ao banco de dados: " + detalhe;
    }
}
