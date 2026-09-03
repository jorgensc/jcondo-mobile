package com.jcondo.mobile.api.model;

import java.io.Serializable;

public class Ocorrencia implements Serializable {

    public static final String ABERTA = "ABERTA";
    public static final String EM_ANDAMENTO = "EM_ANDAMENTO";
    public static final String RESOLVIDA = "RESOLVIDA";

    public Long id;
    public String protocolo;
    public String categoria;
    public String titulo;
    public String descricao;
    public String status;
    public String statusLabel;
    public String dataAbertura;
    public String dataAtualizacao;
    public String resposta;
    public String moradorNome;

    public boolean isResolvida() {
        return RESOLVIDA.equalsIgnoreCase(status);
    }

    public boolean isEmAndamento() {
        return EM_ANDAMENTO.equalsIgnoreCase(status);
    }
}
