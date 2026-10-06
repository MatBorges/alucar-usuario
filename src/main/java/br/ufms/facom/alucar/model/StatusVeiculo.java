package br.ufms.facom.alucar.model;


public enum StatusVeiculo {

    DISPONIVEL("Disponivel", "No patio, pronto para locacao"),
    LOCADO("Locado", "Em poder de um cliente"),
    RESERVADO("Reservado", "Comprometido com um agendamento futuro"),
    EM_MANUTENCAO("Em manutencao", "Na oficina"),
    DESATIVADO("Desativado", "Fora de servico ou removido da frota");

    private final String descricao;
    private final String explicacao;

    StatusVeiculo(String descricao, String explicacao) {
        this.descricao = descricao;
        this.explicacao = explicacao;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getExplicacao() {
        return explicacao;
    }


    public static StatusVeiculo[] definiveisNoCadastro() {
        return new StatusVeiculo[] {DISPONIVEL, EM_MANUTENCAO, DESATIVADO};
    }

    @Override
    public String toString() {
        return descricao;
    }
}
