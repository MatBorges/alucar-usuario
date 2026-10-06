package br.ufms.facom.alucar.model;

/**
 * Usuario responsavel pelas ordens de manutencao da frota.
 */
public class Mecanico extends Usuario {

    public Mecanico() {
        super();
    }

    public Mecanico(String matricula, String cpf, String nome, String login, String senha) {
        super(matricula, cpf, nome, login, senha);
    }

    @Override
    public TipoUsuario getTipoUsuario() {
        return TipoUsuario.MECANICO;
    }
}
