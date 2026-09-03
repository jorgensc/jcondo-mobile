package com.jcondo.mobile.api.model;

import java.io.Serializable;

import java.util.Map;

/** Corpo de erro padronizado devolvido pelo ApiExceptionHandler do backend. */
public class ErroApi implements Serializable {

    public int status;
    public String erro;
    public String mensagem;
    public Map<String, String> campos;
    public String momento;
}
