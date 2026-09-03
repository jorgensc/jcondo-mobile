package jcondo.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TokenServiceTest {

    @Test
    void tokenEmitidoDeveApontarParaOMorador() {
        TokenService service = new TokenService();
        String token = service.emitir(7L);

        assertEquals(7L, service.moradorDoToken(token));
    }

    @Test
    void cadaLoginDeveGerarUmTokenDiferente() {
        TokenService service = new TokenService();

        assertNotEquals(service.emitir(1L), service.emitir(1L));
    }

    @Test
    void tokenInvalidoOuRevogadoNaoDeveAutenticar() {
        TokenService service = new TokenService();
        String token = service.emitir(1L);

        assertNull(service.moradorDoToken("token-que-nao-existe"));
        assertNull(service.moradorDoToken(null));
        assertNull(service.moradorDoToken("  "));

        service.revogar(token);
        assertNull(service.moradorDoToken(token));
    }
}
