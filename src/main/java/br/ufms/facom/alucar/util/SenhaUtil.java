package br.ufms.facom.alucar.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Gera o resumo (hash) da senha antes de gravar no banco, atendendo ao RNF04
 * (armazenamento seguro de credenciais). A senha em texto puro nunca e
 * persistida: a coluna senha guarda apenas o hash hexadecimal.
 */
public final class SenhaUtil {

    private SenhaUtil() {
    }

    public static String gerarHash(String senhaEmTextoPuro) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] resumo = digest.digest(senhaEmTextoPuro.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexadecimal = new StringBuilder(resumo.length * 2);
            for (byte b : resumo) {
                hexadecimal.append(String.format("%02x", b));
            }
            return hexadecimal.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponivel na JVM", e);
        }
    }
}
