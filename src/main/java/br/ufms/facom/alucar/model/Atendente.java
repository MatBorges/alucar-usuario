package br.ufms.facom.alucar.model;


public class Atendente extends Usuario {

    public Atendente() {
        super();
    }

    public Atendente(String matricula, String cpf, String nome, String login, String senha) {
        super(matricula, cpf, nome, login, senha);
    }

    @Override
    public TipoUsuario getTipoUsuario() {
        return TipoUsuario.ATENDENTE;
    }
}
