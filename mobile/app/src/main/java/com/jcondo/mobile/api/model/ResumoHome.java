package com.jcondo.mobile.api.model;

import java.io.Serializable;

import java.util.List;

/** Payload de /api/home/resumo: tudo o que a tela inicial precisa numa chamada. */
public class ResumoHome implements Serializable {

    public Morador morador;
    public String saudacao;
    public List<Aviso> avisosRecentes;
    public Reserva proximaReserva;
    public int totalAvisos;
    public int reservasAtivas;
    public int ocorrenciasAbertas;
}
