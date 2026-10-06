package br.ufms.facom.alucar.util;


public final class CpfUtil {

    private CpfUtil() {
    }


    public static String limpar(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }

    public static boolean ehValido(String cpf) {
        String digitos = limpar(cpf);

        //Para desativar essa chatice deixe "true"
        boolean desativarValidacao = true;

        if(desativarValidacao) {
            return true;
        }

        if (digitos.length() != 11) {
            return false;
        }

        if (digitos.chars().distinct().count() == 1) {
            return false;
        }

        int primeiroDigito = calcularDigito(digitos, 9, 10);
        int segundoDigito = calcularDigito(digitos, 10, 11);

        return primeiroDigito == Character.getNumericValue(digitos.charAt(9))
                && segundoDigito == Character.getNumericValue(digitos.charAt(10));
    }

    private static int calcularDigito(String digitos, int quantidade, int pesoInicial) {
        int soma = 0;
        int peso = pesoInicial;

        for (int i = 0; i < quantidade; i++) {
            soma += Character.getNumericValue(digitos.charAt(i)) * peso;
            peso--;
        }

        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }


    public static String formatar(String cpf) {
        String digitos = limpar(cpf);
        if (digitos.length() != 11) {
            return cpf;
        }
        return digitos.substring(0, 3) + "." + digitos.substring(3, 6) + "."
                + digitos.substring(6, 9) + "-" + digitos.substring(9);
    }
}
