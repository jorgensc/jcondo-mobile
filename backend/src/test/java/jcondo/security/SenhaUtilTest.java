package jcondo.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SenhaUtilTest {

    @Test
    void hashNaoPodeDevolverASenhaEmTextoPuro() {
        String hash = SenhaUtil.hash("123456");
        assertNotEquals("123456", hash);
        assertEquals(64, hash.length());
    }

    @Test
    void aMesmaSenhaGeraSempreOMesmoHash() {
        assertEquals(SenhaUtil.hash("123456"), SenhaUtil.hash("123456"));
    }

    @Test
    void confereDeveAceitarASenhaCorretaERecusarAsDemais() {
        String gravado = SenhaUtil.hash("admin123");

        assertTrue(SenhaUtil.confere("admin123", gravado));
        assertFalse(SenhaUtil.confere("Admin123", gravado));
        assertFalse(SenhaUtil.confere("", gravado));
        assertFalse(SenhaUtil.confere(null, gravado));
        assertFalse(SenhaUtil.confere("admin123", null));
    }
}
