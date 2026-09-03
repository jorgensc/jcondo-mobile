package jcondo.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hash de senha com SHA-256 e um sal fixo da aplicacao.
 *
 * A senha nunca e gravada em texto puro no banco e nunca volta numa resposta
 * da API (ver @JsonProperty WRITE_ONLY em Morador). Para um sistema em
 * producao o correto seria BCrypt/Argon2 com sal por usuario, que resiste a
 * ataque de forca bruta em GPU - aqui o SHA-256 foi escolhido para manter o
 * escopo academico sem adicionar o Spring Security ao projeto.
 */
public final class SenhaUtil {

    private static final String SAL = "JCondo$";

    private SenhaUtil() {
    }

    public static String hash(String senhaPura) {
        if (senhaPura == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest((SAL + senhaPura).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 e obrigatorio em toda JVM, entao isso nao acontece na pratica
            throw new IllegalStateException("Algoritmo SHA-256 indisponível", e);
        }
    }

    // comparacao de tempo constante pra nao vazar informacao pelo tempo de resposta
    public static boolean confere(String senhaPura, String hashGravado) {
        if (senhaPura == null || hashGravado == null) {
            return false;
        }
        return MessageDigest.isEqual(
                hash(senhaPura).getBytes(StandardCharsets.UTF_8),
                hashGravado.getBytes(StandardCharsets.UTF_8));
    }
}
