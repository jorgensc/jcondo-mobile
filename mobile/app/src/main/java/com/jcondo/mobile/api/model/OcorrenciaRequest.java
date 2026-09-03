package com.jcondo.mobile.api.model;

import java.io.Serializable;

public class OcorrenciaRequest implements Serializable {

    public String categoria;
    public String titulo;
    public String descricao;

    public OcorrenciaRequest(String categoria, String titulo, String descricao) {
        this.categoria = categoria;
        this.titulo = titulo;
        this.descricao = descricao;
    }
}
