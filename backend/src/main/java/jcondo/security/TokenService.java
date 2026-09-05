package jcondo.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

/**
 * Sessao por token, guardada em memoria. Cada token vale 12h.
 * Reiniciar o servidor derruba os logins; a evolucao natural e JWT assinado
 * ou uma tabela de sessoes.
 */
@Service
public class TokenService {

    private static final Duration VALIDADE = Duration.ofHours(12);

    private record Sessao(Long moradorId, Instant expiraEm) {
    }

    private final Map<String, Sessao> sessoes = new ConcurrentHashMap<>();

    public String emitir(Long moradorId) {
        limparExpirados();
        String token = UUID.randomUUID().toString().replace("-", "");
        sessoes.put(token, new Sessao(moradorId, Instant.now().plus(VALIDADE)));
        return token;
    }

    public Long moradorDoToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        Sessao sessao = sessoes.get(token);
        if (sessao == null) {
            return null;
        }
        if (Instant.now().isAfter(sessao.expiraEm())) {
            sessoes.remove(token);
            return null;
        }
        return sessao.moradorId();
    }

    public void revogar(String token) {
        if (token != null) {
            sessoes.remove(token);
        }
    }

    private void limparExpirados() {
        Instant agora = Instant.now();
        sessoes.entrySet().removeIf(e -> agora.isAfter(e.getValue().expiraEm()));
    }
}
