package jcondo.dto;

import jakarta.validation.constraints.NotBlank;

// Corpo do POST /api/auth/login enviado pelo app
public class LoginRequest {

    @NotBlank(message = "Informe o e-mail")
    private String email;

    @NotBlank(message = "Informe a senha")
    private String senha;

    public LoginRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
