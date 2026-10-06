package br.ufms.facom.alucar.util;

/**
 * Parametros de configuracao do sistema.
 *
 * RNF03 exige que o prazo de expiracao das senhas seja configuravel. Nesta
 * iteracao o valor e lido de variavel de ambiente, com um padrao sensato -
 * suficiente para atender ao requisito sem acoplar a uma tela de
 * configuracao que ainda nao existe.
 *
 * Quando a tela de parametros do sistema for implementada, basta trocar a
 * origem do valor aqui (por exemplo, uma tabela parametro_sistema): nenhuma
 * outra classe precisa mudar, porque todas consultam este ponto unico.
 */
public final class ParametrosSistema {

    /** Zero ou negativo desliga a expiracao de senhas. */
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
