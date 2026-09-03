package jcondo.security;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jcondo.entity.Morador;
import jcondo.exception.NaoAutorizadoException;
import jcondo.repository.MoradorRepository;

/**
 * Intercepta as chamadas de /api/** e exige um token valido no header
 * Authorization ("Bearer <token>"). O login fica de fora, senao ninguem
 * conseguiria entrar. Ver WebConfig para as rotas liberadas.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;
    private final MoradorRepository moradorRepository;
    private final UsuarioLogado usuarioLogado;

    public AuthInterceptor(TokenService tokenService,
                           MoradorRepository moradorRepository,
                           UsuarioLogado usuarioLogado) {
        this.tokenService = tokenService;
        this.moradorRepository = moradorRepository;
        this.usuarioLogado = usuarioLogado;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        // o navegador manda um OPTIONS antes do POST (preflight de CORS);
        // ele nao carrega o token e por isso passa direto
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = extrairToken(request);
        Long moradorId = tokenService.moradorDoToken(token);
        if (moradorId == null) {
            throw new NaoAutorizadoException("Sessão expirada ou inválida. Faça login novamente.");
        }

        Optional<Morador> morador = moradorRepository.findById(moradorId);
        if (morador.isEmpty()) {
            tokenService.revogar(token);
            throw new NaoAutorizadoException("Usuário da sessão não encontrado.");
        }

        usuarioLogado.setMorador(morador.get());
        return true;
    }

    private String extrairToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || header.isBlank()) {
            return null;
        }
        if (header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return header.substring(7).trim();
        }
        return header.trim();
    }
}
