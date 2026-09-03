package jcondo.dto;

import java.time.format.DateTimeFormatter;

import jcondo.entity.Reserva;

// Reserva achatada pro app: sem objeto aninhado de morador/area e
// com as datas ja formatadas, pra tela nao precisar fazer conversao.
public record ReservaDTO(
        Long id,
        Long areaId,
        String areaNome,
        String data,
        String dataFormatada,
        String horaInicio,
        String horaFim,
        String status,
        String moradorNome,
        String unidade) {

    private static final DateTimeFormatter ISO_DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter BR_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public static ReservaDTO de(Reserva r) {
        return new ReservaDTO(
                r.getId(),
                r.getArea().getId(),
                r.getArea().getNome(),
                r.getData().format(ISO_DATA),
                r.getData().format(BR_DATA),
                r.getHoraInicio().format(HORA),
                r.getHoraFim().format(HORA),
                r.getStatus(),
                r.getMorador().getNome(),
                r.getMorador().getUnidade());
    }
}
