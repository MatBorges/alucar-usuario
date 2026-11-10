package br.ufms.facom.alucar.util;

import java.math.BigDecimal;

/**
 * Parametros configuraveis do sistema.
 *
 * Centraliza os valores numericos das regras de negocio que a locadora pode
 * querer ajustar sem recompilar o sistema. Todos tem um valor padrao embutido
 * e podem ser sobrescritos por variavel de ambiente, o que permite que o grupo
 * defina os numeros definitivos depois, sem mexer no codigo.
 */
public final class ParametrosSistema {

    // Politica de senha (RF01)
    private static final int PRAZO_PADRAO_EM_DIAS = 90;

    // RN03 - garantia minima exigida na locacao
    private static final BigDecimal CAUCAO_MINIMA_PADRAO = new BigDecimal("500.00");

    // RN05 - desconto para locacoes de longa duracao
    private static final int DIAS_PARA_DESCONTO_PADRAO = 30;
    private static final BigDecimal PERCENTUAL_DESCONTO_PADRAO = new BigDecimal("0.10");

    // RN06 - multa contratual fixa + taxa diaria de atraso
    private static final BigDecimal MULTA_CONTRATUAL_PADRAO = new BigDecimal("150.00");
    private static final BigDecimal TAXA_DIARIA_ATRASO_PADRAO = new BigDecimal("0.20");

    private ParametrosSistema() {
    }

    /** Dias de validade da senha antes de exigir troca. */
    public static int getPrazoExpiracaoSenhaEmDias() {
        return lerInteiro("ALUCAR_PRAZO_SENHA_DIAS", PRAZO_PADRAO_EM_DIAS);
    }

    /** RN03 - valor minimo de caucao/garantia aceito ao efetuar a locacao. */
    public static BigDecimal getCaucaoMinima() {
        return lerDecimal("ALUCAR_CAUCAO_MINIMA", CAUCAO_MINIMA_PADRAO);
    }

    /** RN05 - numero de diarias a partir do qual o desconto passa a valer. */
    public static int getDiariasParaDesconto() {
        return lerInteiro("ALUCAR_DIARIAS_DESCONTO", DIAS_PARA_DESCONTO_PADRAO);
    }

    /** RN05 - percentual de desconto aplicado ao valor da diaria (0.10 = 10%). */
    public static BigDecimal getPercentualDescontoLongaDuracao() {
        return lerDecimal("ALUCAR_PERCENTUAL_DESCONTO", PERCENTUAL_DESCONTO_PADRAO);
    }

    /** RN06 - multa contratual fixa, cobrada uma vez quando ha atraso. */
    public static BigDecimal getMultaContratualPorAtraso() {
        return lerDecimal("ALUCAR_MULTA_CONTRATUAL", MULTA_CONTRATUAL_PADRAO);
    }

    /**
     * RN06 - taxa diaria de atraso: percentual sobre o valor da diaria,
     * cobrado por cada dia de atraso (0.20 = 20% da diaria por dia).
     */
    public static BigDecimal getTaxaDiariaDeAtraso() {
        return lerDecimal("ALUCAR_TAXA_ATRASO", TAXA_DIARIA_ATRASO_PADRAO);
    }

    private static int lerInteiro(String variavelDeAmbiente, int padrao) {
        String valor = System.getenv(variavelDeAmbiente);
        if (valor == null || valor.isBlank()) {
            return padrao;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return padrao;
        }
    }

    private static BigDecimal lerDecimal(String variavelDeAmbiente, BigDecimal padrao) {
        String valor = System.getenv(variavelDeAmbiente);
        if (valor == null || valor.isBlank()) {
            return padrao;
        }
        try {
            return new BigDecimal(valor.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return padrao;
        }
    }
}
