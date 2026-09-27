package br.ufms.facom.alucar.dao;

/**
 * Traduz falhas tecnicas de acesso a dados (SQLException) em uma excecao de
 * aplicacao, para que a camada de controle e a interface grafica nao precisem
 * conhecer detalhes de JDBC.
 */
public class DAOException extends Exception {

    public DAOException(String mensagem) {
        super(mensagem);
    }

    public DAOException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
