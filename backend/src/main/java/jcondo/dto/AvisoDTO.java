package jcondo.dto;

import java.time.format.DateTimeFormatter;

import jcondo.entity.Aviso;

// Aviso pronto pra lista do app, com data ja formatada em pt-BR
public record AvisoDTO(
        Long id,
        String titulo,
        String conteudo,
        String autor,
        String prioridade,
        String dataPublicacao) {

    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static AvisoDTO de(Aviso a) {
        return new AvisoDTO(
                a.getId(),
                a.getTitulo(),
                a.getConteudo(),
                a.getAutor(),
                a.getPrioridade(),
                a.getDataPublicacao() == null ? null : a.getDataPublicacao().format(BR));
    }
}
