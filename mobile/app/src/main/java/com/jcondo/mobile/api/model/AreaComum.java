package com.jcondo.mobile.api.model;

import java.io.Serializable;

public class AreaComum implements Serializable {

    public Long id;
    public String nome;
    public String descricao;
    public Integer capacidade;
    public String horarioAbertura;
    public String horarioFechamento;
    public String regras;

    @Override
    public String toString() {
        return nome == null ? "" : nome;
    }
}
