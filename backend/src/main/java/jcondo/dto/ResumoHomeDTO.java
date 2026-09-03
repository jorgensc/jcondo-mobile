package jcondo.dto;

import java.util.List;

// Payload unico da tela inicial. Em vez de a Home fazer 3 chamadas
// (avisos + reservas + ocorrencias), ela faz uma so - menos trafego,
// menos bateria e a tela carrega de uma vez.
public record ResumoHomeDTO(
        MoradorDTO morador,
        String saudacao,
        List<AvisoDTO> avisosRecentes,
        ReservaDTO proximaReserva,
        int totalAvisos,
        int reservasAtivas,
        int ocorrenciasAbertas) {
}
