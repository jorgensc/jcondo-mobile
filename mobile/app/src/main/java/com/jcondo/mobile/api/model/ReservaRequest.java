package com.jcondo.mobile.api.model;

import java.io.Serializable;

public class ReservaRequest implements Serializable {

    public Long areaId;
    public String data;
    public String horaInicio;
    public String horaFim;

    public ReservaRequest(Long areaId, String data, String horaInicio, String horaFim) {
        this.areaId = areaId;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }
}
