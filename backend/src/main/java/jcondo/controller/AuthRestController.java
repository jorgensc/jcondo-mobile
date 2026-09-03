package jcondo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jcondo.dto.LoginRequest;
import jcondo.dto.LoginResponse;
import jcondo.service.AutenticacaoService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Login e encerramento de sessão do app mobile")
public class AuthRestController {

    private final AutenticacaoService service;

    public AuthRestController(AutenticacaoService service) {
        this.service = service;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar morador e receber o token de sessão")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request.getEmail(), request.getSenha()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Encerrar a sessão atual")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = authorization == null ? null
                : authorization.replaceFirst("(?i)^Bearer\\s+", "").trim();
        service.logout(token);
        return ResponseEntity.noContent().build();
    }
}
