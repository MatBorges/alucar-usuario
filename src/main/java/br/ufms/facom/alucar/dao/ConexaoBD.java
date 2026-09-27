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
 * Cenario de desenvolvimento: container lbd_mysql em Docker dentro do WSL2,
 * publicado na porta 3434 do host.
 * - Se a aplicacao roda no Windows, use HOST = "localhost" (o WSL2 encaminha
 *   as portas publicadas para o Windows). Se falhar, use o IP do WSL, obtido
 *   no terminal WSL com: hostname -I
 * - Se a aplicacao roda dentro do proprio WSL, HOST = "localhost" tambem.
 *
 * As tres propriedades da URL sao obrigatorias neste cenario:
 * - sslMode=DISABLED ............... o container nao tem certificado configurado
 * - allowPublicKeyRetrieval=true ... exigido pelo plugin caching_sha2_password
 *                                    do MySQL 8 quando o SSL esta desligado
 * - connectionTimeZone ............. o container roda em UTC; sem isso o driver
 *                                    recusa a conexao ou grava datas deslocadas
 */
public final class ConexaoBD {

    private static final String HOST = obterValor("ALUCAR_DB_HOST", "localhost");
    private static final String PORTA = obterValor("ALUCAR_DB_PORT", "3434");
    private static final String BANCO = obterValor("ALUCAR_DB_NAME", "alucar");
    private static final String USUARIO = obterValor("ALUCAR_DB_USER", "root");
    private static final String SENHA = obterValor("ALUCAR_DB_PASSWORD", "123");

    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORTA + "/" + BANCO
            + "?sslMode=DISABLED"
            + "&allowPublicKeyRetrieval=true"
            + "&connectionTimeZone=America/Campo_Grande"
            + "&forceConnectionTimeZoneToSession=true"
            + "&characterEncoding=UTF-8";

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
            return "Nao foi possivel alcancar o MySQL em " + HOST + ":" + PORTA + ".\n\n"
                    + "Verifique no WSL:\n"
                    + "  docker ps            (o container lbd_mysql esta rodando?)\n"
                    + "  docker compose up -d (para subir o container)\n\n"
                    + "Se o container esta rodando e o erro persiste, descubra o IP do WSL\n"
                    + "com 'hostname -I' e defina a variavel de ambiente ALUCAR_DB_HOST\n"
                    + "com esse IP.";
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
