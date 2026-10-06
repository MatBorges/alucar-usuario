package br.ufms.facom.alucar.model;

import java.math.BigDecimal;
import java.util.Objects;


public class CategoriaVeiculo {

    private String nome;
    private String descricao;
    private BigDecimal valorBaseDiaria;


    private boolean ativo = true;

    public CategoriaVeiculo() {
    }

    public CategoriaVeiculo(String nome, String descricao, BigDecimal valorBaseDiaria) {
        this.nome = nome;
        this.descricao = descricao;
        this.valorBaseDiaria = valorBaseDiaria;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValorBaseDiaria() {
        return valorBaseDiaria;
    }

    public void setValorBaseDiaria(BigDecimal valorBaseDiaria) {
        this.valorBaseDiaria = valorBaseDiaria;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CategoriaVeiculo outra)) {
            return false;
        }
        return Objects.equals(nome, outra.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome);
    }

    /**
     * Usado pelo JComboBox da tela de veiculos, que exibe o resultado de
     * toString() em cada item da lista.
     */
    @Override
    public String toString() {
        return nome;
    }
}
