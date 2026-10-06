package br.ufms.facom.alucar.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade de domínio representando o contrato de locação de veículo (RF08).
 * Segue o padrão GRASP Information Expert.
 */
public class Locacao {

    private Integer idLocacao;
    private LocalDateTime dataRetirada;
    private LocalDate dataDevolucaoPrevista;
    private int kmInicial;
    private BigDecimal valorEstimado;
    private BigDecimal valorCaucao;
    private StatusLocacao status = StatusLocacao.ATIVA;

    private Cliente cliente;
    private Veiculo veiculo;
    private Usuario atendente;

    public Locacao() {
        this.dataRetirada = LocalDateTime.now();
    }

    public Locacao(Integer idLocacao, LocalDateTime dataRetirada, LocalDate dataDevolucaoPrevista,
                   int kmInicial, BigDecimal valorEstimado, BigDecimal valorCaucao,
                   StatusLocacao status, Cliente cliente, Veiculo veiculo, Usuario atendente) {
        this.idLocacao = idLocacao;
        this.dataRetirada = dataRetirada != null ? dataRetirada : LocalDateTime.now();
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
        this.kmInicial = kmInicial;
        this.valorEstimado = valorEstimado;
        this.valorCaucao = valorCaucao;
        this.status = status != null ? status : StatusLocacao.ATIVA;
        this.cliente = cliente;
        this.veiculo = veiculo;
        this.atendente = atendente;
    }

    public boolean isAtiva() {
        return status == StatusLocacao.ATIVA;
    }

    public Integer getIdLocacao() {
        return idLocacao;
    }

    public void setIdLocacao(Integer idLocacao) {
        this.idLocacao = idLocacao;
    }

    public LocalDateTime getDataRetirada() {
        return dataRetirada;
    }

    public void setDataRetirada(LocalDateTime dataRetirada) {
        this.dataRetirada = dataRetirada;
    }

    public LocalDate getDataDevolucaoPrevista() {
        return dataDevolucaoPrevista;
    }

    public void setDataDevolucaoPrevista(LocalDate dataDevolucaoPrevista) {
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
    }

    public int getKmInicial() {
        return kmInicial;
    }

    public void setKmInicial(int kmInicial) {
        this.kmInicial = kmInicial;
    }

    public BigDecimal getValorEstimado() {
        return valorEstimado;
    }

    public void setValorEstimado(BigDecimal valorEstimado) {
        this.valorEstimado = valorEstimado;
    }

    public BigDecimal getValorCaucao() {
        return valorCaucao;
    }

    public void setValorCaucao(BigDecimal valorCaucao) {
        this.valorCaucao = valorCaucao;
    }

    public StatusLocacao getStatus() {
        return status;
    }

    public void setStatus(StatusLocacao status) {
        this.status = status;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public Usuario getAtendente() {
        return atendente;
    }

    public void setAtendente(Usuario atendente) {
        this.atendente = atendente;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Locacao locacao = (Locacao) o;
        return Objects.equals(idLocacao, locacao.idLocacao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idLocacao);
    }
}
