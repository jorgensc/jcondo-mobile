package com.jcondo.mobile.api.model;

import java.io.Serializable;

public class Reserva implements Serializable {

    public static final String CONFIRMADA = "CONFIRMADA";
    public static final String CANCELADA = "CANCELADA";

    public Long id;
    public Long areaId;
    public String areaNome;
    public String data;
    public String dataFormatada;
    public String horaInicio;
    public String horaFim;
    public String status;
    public String moradorNome;
    public String unidade;

    public boolean isConfirmada() {
        return CONFIRMADA.equalsIgnoreCase(status);
    }

    public boolean isCancelada() {
        return CANCELADA.equalsIgnoreCase(status);
    }

    public String faixaHorario() {
        return horaInicio + " às " + horaFim;
    }
}
