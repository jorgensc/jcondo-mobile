package com.jcondo.mobile.api.model;

import java.io.Serializable;

public class AtualizacaoPerfil implements Serializable {

    public String telefone;
    public String email;
    public String senhaAtual;
    public String novaSenha;

    public AtualizacaoPerfil(String telefone, String email) {
        this.telefone = telefone;
        this.email = email;
    }
}
