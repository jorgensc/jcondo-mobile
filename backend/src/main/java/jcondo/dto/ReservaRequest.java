package jcondo.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;

// Corpo do POST /api/reservas. As datas chegam como texto ISO
// ("2026-09-10" e "19:00") e o Jackson converte pros tipos do java.time.
public class ReservaRequest {

    @NotNull(message = "Selecione a área comum")
    private Long areaId;

    @NotNull(message = "Informe a data da reserva")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate data;

    @NotNull(message = "Informe o horário de início")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaInicio;

    @NotNull(message = "Informe o horário de término")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaFim;

    public ReservaRequest() {
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(LocalTime horaFim) {
        this.horaFim = horaFim;
    }
}
