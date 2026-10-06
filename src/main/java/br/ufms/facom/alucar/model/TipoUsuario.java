package br.ufms.facom.alucar.model;


public enum TipoUsuario {

    GERENTE("Gerente"),
    ATENDENTE("Atendente"),
    MECANICO("Mecanico");

    private final String descricao;

    TipoUsuario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
