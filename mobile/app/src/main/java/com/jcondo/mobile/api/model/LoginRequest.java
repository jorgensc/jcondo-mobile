package com.jcondo.mobile.api.model;

import java.io.Serializable;

public class LoginRequest implements Serializable {

    public String email;
    public String senha;

    public LoginRequest(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }
}
