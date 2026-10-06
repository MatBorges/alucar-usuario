package br.ufms.facom.alucar.dao;


public class DAOException extends Exception {

    public DAOException(String mensagem) {
        super(mensagem);
    }

    public DAOException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
