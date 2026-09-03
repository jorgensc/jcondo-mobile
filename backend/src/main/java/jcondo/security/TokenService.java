package jcondo.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

/**
 * Controle de sessao por token, guardado em memoria.
 *
 * O app faz login uma vez, guarda o token no SharedPreferences e manda em
 * todas as chamadas seguintes no header Authorization. Cada token vale 12h.
 * Como a sessao fica em memoria, reiniciar o servidor derruba os logins -
 * a evolucao natural aqui seria JWT assinado ou uma tabela de sessoes.
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

    /** Devolve o id do morador dono do token, ou null se invalido/expirado. */
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
