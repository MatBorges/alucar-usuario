package br.ufms.facom.alucar.controller;

/**
 * Sinaliza que os dados informados na tela violam uma regra de negocio ou de
 * preenchimento. A interface grafica apenas exibe a mensagem: a decisao sobre
 * o que e valido pertence a camada de controle.
 */
public class ValidacaoException extends Exception {

    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
