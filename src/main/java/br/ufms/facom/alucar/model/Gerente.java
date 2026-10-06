package br.ufms.facom.alucar.model;

/**
 * Usuario com permissao administrativa: cadastros e relatorios gerenciais.
 */
public class Gerente extends Usuario {

    public Gerente() {
        super();
    }

    public Gerente(String matricula, String cpf, String nome, String login, String senha) {
        super(matricula, cpf, nome, login, senha);
    }

    @Override
    public TipoUsuario getTipoUsuario() {
        return TipoUsuario.GERENTE;
    }
}
