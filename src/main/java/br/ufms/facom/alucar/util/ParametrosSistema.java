package br.ufms.facom.alucar.util;

//Fazer as configurações do sistema aqui
public final class ParametrosSistema {


    private static final int PRAZO_PADRAO_EM_DIAS = 90;

    private ParametrosSistema() {
    }

    public static int getPrazoExpiracaoSenhaEmDias() {
        String valor = System.getenv("ALUCAR_PRAZO_SENHA_DIAS");
        if (valor == null || valor.isBlank()) {
            return PRAZO_PADRAO_EM_DIAS;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return PRAZO_PADRAO_EM_DIAS;
        }
    }
}
