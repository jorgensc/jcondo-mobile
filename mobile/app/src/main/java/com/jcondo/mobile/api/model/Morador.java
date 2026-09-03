package com.jcondo.mobile.api.model;

import java.io.Serializable;

/** Dados do morador autenticado, como vem de /api/perfil e do login. */
public class Morador implements Serializable {

    public Long id;
    public String nome;
    public String email;
    public String telefone;
    public String bloco;
    public String apartamento;
    public String unidade;
    public String perfil;

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(perfil);
    }

    /** Primeiro nome, usado na saudacao da tela inicial. */
    public String primeiroNome() {
        if (nome == null || nome.trim().isEmpty()) {
            return "";
        }
        String limpo = nome.trim();
        int espaco = limpo.indexOf(' ');
        return espaco > 0 ? limpo.substring(0, espaco) : limpo;
    }

    /** Iniciais para o avatar do perfil (ex.: "Ana Paula Souza" -> "AS"). */
    public String iniciais() {
        if (nome == null || nome.trim().isEmpty()) {
            return "?";
        }
        String[] partes = nome.trim().split("\\s+");
        String primeira = partes[0].substring(0, 1);
        if (partes.length == 1) {
            return primeira.toUpperCase();
        }
        String ultima = partes[partes.length - 1].substring(0, 1);
        return (primeira + ultima).toUpperCase();
    }
}
