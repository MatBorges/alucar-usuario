package br.ufms.facom.alucar.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;


public final class MoedaUtil {

    private static final Locale BRASIL = Locale.forLanguageTag("pt-BR");

    private MoedaUtil() {
    }


    public static BigDecimal converter(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Valor nao informado");
        }

        String limpo = texto.replace("R$", "").replace(" ", "").trim();

        if (limpo.contains(".") && limpo.contains(",")) {

            limpo = limpo.replace(".", "").replace(",", ".");
        } else if (limpo.contains(",")) {

            limpo = limpo.replace(",", ".");
        }


        try {
            return new BigDecimal(limpo).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor invalido: " + texto, e);
        }
    }

//    Para exibição
    public static String formatar(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        return NumberFormat.getCurrencyInstance(BRASIL).format(valor);
    }


    public static String formatarSemSimbolo(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        NumberFormat formato = NumberFormat.getNumberInstance(BRASIL);
        formato.setMinimumFractionDigits(2);
        formato.setMaximumFractionDigits(2);
        return formato.format(valor);
    }
}
