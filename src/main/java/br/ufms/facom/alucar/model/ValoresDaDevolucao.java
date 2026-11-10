package br.ufms.facom.alucar.model;

import java.math.BigDecimal;

/**
 * Resultado da consulta "valores = calculaValorFinal(...)" do DSS de
 * Registrar Devolucao.
 *
 * Agrupa as parcelas que a tela precisa exibir antes de o atendente
 * confirmar. E apenas um transportador de valores ja calculados pela
 * Locacao: nao contem regra de negocio.
 */
public class ValoresDaDevolucao {

    private final int diariasUtilizadas;
    private final int diasDeAtraso;
    private final BigDecimal valorDasDiarias;
    private final BigDecimal multaPorAtraso;
    private final BigDecimal custoAvaria;
    private final BigDecimal valorFinal;
    private final BigDecimal valorCaucaoRetido;
    private final BigDecimal valorCaucaoADevolver;
    private final BigDecimal valorAPagar;
    private final StatusVeiculo situacaoDestinoDoVeiculo;

    public ValoresDaDevolucao(int diariasUtilizadas, int diasDeAtraso,
                              BigDecimal valorDasDiarias, BigDecimal multaPorAtraso,
                              BigDecimal custoAvaria, BigDecimal valorFinal,
                              BigDecimal valorCaucaoRetido, BigDecimal valorCaucaoADevolver,
                              BigDecimal valorAPagar,
                              StatusVeiculo situacaoDestinoDoVeiculo) {
        this.diariasUtilizadas = diariasUtilizadas;
        this.diasDeAtraso = diasDeAtraso;
        this.valorDasDiarias = valorDasDiarias;
        this.multaPorAtraso = multaPorAtraso;
        this.custoAvaria = custoAvaria;
        this.valorFinal = valorFinal;
        this.valorCaucaoRetido = valorCaucaoRetido;
        this.valorCaucaoADevolver = valorCaucaoADevolver;
        this.valorAPagar = valorAPagar;
        this.situacaoDestinoDoVeiculo = situacaoDestinoDoVeiculo;
    }

    public int getDiariasUtilizadas() {
        return diariasUtilizadas;
    }

    public int getDiasDeAtraso() {
        return diasDeAtraso;
    }

    public boolean houveAtraso() {
        return diasDeAtraso > 0;
    }

    public BigDecimal getValorDasDiarias() {
        return valorDasDiarias;
    }

    public BigDecimal getMultaPorAtraso() {
        return multaPorAtraso;
    }

    public BigDecimal getCustoAvaria() {
        return custoAvaria;
    }

    public BigDecimal getValorFinal() {
        return valorFinal;
    }

    public BigDecimal getValorCaucaoRetido() {
        return valorCaucaoRetido;
    }

    public BigDecimal getValorCaucaoADevolver() {
        return valorCaucaoADevolver;
    }

    public BigDecimal getValorAPagar() {
        return valorAPagar;
    }

    public StatusVeiculo getSituacaoDestinoDoVeiculo() {
        return situacaoDestinoDoVeiculo;
    }
}
