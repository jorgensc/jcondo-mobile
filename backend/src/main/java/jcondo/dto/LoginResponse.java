package jcondo.dto;

// Resposta do login: o token vai no header Authorization das proximas chamadas
public record LoginResponse(String token, MoradorDTO morador) {
}
