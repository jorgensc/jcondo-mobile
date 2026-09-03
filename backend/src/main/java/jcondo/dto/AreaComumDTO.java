package jcondo.dto;

import java.time.format.DateTimeFormatter;

import jcondo.entity.AreaComum;

// Area comum no formato consumido pela tela de nova reserva
public record AreaComumDTO(
        Long id,
        String nome,
        String descricao,
        Integer capacidade,
        String horarioAbertura,
        String horarioFechamento,
        String regras) {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public static AreaComumDTO de(AreaComum a) {
        return new AreaComumDTO(
                a.getId(),
                a.getNome(),
                a.getDescricao(),
                a.getCapacidade(),
                a.getHorarioAbertura().format(HORA),
                a.getHorarioFechamento().format(HORA),
                a.getRegras());
    }
}
