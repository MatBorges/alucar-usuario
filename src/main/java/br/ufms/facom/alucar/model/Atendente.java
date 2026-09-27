package br.ufms.facom.alucar.model;

/**
 * Usuario responsavel pelo atendimento no balcao.
 * E o ator do caso de uso "Realizar Locacao".
 */
public class Atendente extends Usuario {

    public Atendente() {
        super();
    }

    public Atendente(String matricula, String nome, String login, String senha) {
        super(matricula, nome, login, senha);
    }

    @Override
    public TipoUsuario getTipoUsuario() {
        return TipoUsuario.ATENDENTE;
    }
}
