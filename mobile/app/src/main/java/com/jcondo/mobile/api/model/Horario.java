package com.jcondo.mobile.api.model;

import java.io.Serializable;

/** Faixa de 2h da agenda de uma area num dia. */
public class Horario implements Serializable {

    public String horaInicio;
    public String horaFim;
    public boolean disponivel;

    public String faixa() {
        return horaInicio + " - " + horaFim;
    }
}
