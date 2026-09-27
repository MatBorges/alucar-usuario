package br.ufms.facom.alucar.model;

/**
 * Discriminador da heranca Usuario -> Atendente / Gerente / Mecanico.
 * Mapeado na coluna tipo_usuario da tabela usuario (estrategia de tabela unica).
 */
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
