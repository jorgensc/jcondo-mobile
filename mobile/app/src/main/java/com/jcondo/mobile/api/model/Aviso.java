package com.jcondo.mobile.api.model;

import java.io.Serializable;

public class Aviso implements Serializable {

    public static final String URGENTE = "URGENTE";
    public static final String ALTA = "ALTA";
    public static final String NORMAL = "NORMAL";

    public Long id;
    public String titulo;
    public String conteudo;
    public String autor;
    public String prioridade;
    public String dataPublicacao;

    public boolean isUrgente() {
        return URGENTE.equalsIgnoreCase(prioridade);
    }

    public boolean isAlta() {
        return ALTA.equalsIgnoreCase(prioridade);
    }
}
