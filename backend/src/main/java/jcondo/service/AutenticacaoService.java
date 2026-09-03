package jcondo.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jcondo.dto.LoginResponse;
import jcondo.dto.MoradorDTO;
import jcondo.entity.Morador;
import jcondo.exception.NaoAutorizadoException;
import jcondo.repository.MoradorRepository;
import jcondo.security.SenhaUtil;
import jcondo.security.TokenService;

@Service
public class AutenticacaoService {

    private final MoradorRepository moradorRepository;
    private final TokenService tokenService;

    public AutenticacaoService(MoradorRepository moradorRepository, TokenService tokenService) {
        this.moradorRepository = moradorRepository;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(String email, String senha) {
        Optional<Morador> encontrado =
                moradorRepository.findByEmailIgnoreCase(email == null ? "" : email.trim());

        // mesma mensagem pros dois casos (e-mail inexistente ou senha errada),
        // pra nao revelar quais e-mails estao cadastrados
        if (encontrado.isEmpty() || !SenhaUtil.confere(senha, encontrado.get().getSenha())) {
            throw new NaoAutorizadoException("E-mail ou senha incorretos.");
        }

        Morador morador = encontrado.get();
        String token = tokenService.emitir(morador.getId());
        return new LoginResponse(token, MoradorDTO.de(morador));
    }

    public void logout(String token) {
        tokenService.revogar(token);
    }
}
