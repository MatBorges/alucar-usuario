package br.ufms.facom.alucar.model;

/**
 * Status operacional de uma locação.
 */
public enum StatusLocacao {

    ATIVA("Ativa"),
    CONCLUIDA("Concluída"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusLocacao(String descricao) {
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
