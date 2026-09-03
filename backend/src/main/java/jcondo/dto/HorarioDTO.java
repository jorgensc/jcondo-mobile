package jcondo.dto;

// Uma faixa de horario da agenda de uma area em um dia.
// "disponivel = false" significa que ja existe reserva confirmada no periodo.
public record HorarioDTO(String horaInicio, String horaFim, boolean disponivel) {
}
