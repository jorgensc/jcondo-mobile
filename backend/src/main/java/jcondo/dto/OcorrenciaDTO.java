package jcondo.dto;

import java.time.format.DateTimeFormatter;

import jcondo.entity.Ocorrencia;

// Ocorrencia no formato que o app consome
public record OcorrenciaDTO(
        Long id,
        String protocolo,
        String categoria,
        String titulo,
        String descricao,
        String status,
        String statusLabel,
        String dataAbertura,
        String dataAtualizacao,
        String resposta,
        String moradorNome) {

    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static OcorrenciaDTO de(Ocorrencia o) {
        return new OcorrenciaDTO(
                o.getId(),
                o.getProtocolo(),
                o.getCategoria(),
                o.getTitulo(),
                o.getDescricao(),
                o.getStatus(),
                label(o.getStatus()),
                o.getDataAbertura() == null ? null : o.getDataAbertura().format(BR),
                o.getDataAtualizacao() == null ? null : o.getDataAtualizacao().format(BR),
                o.getResposta(),
                o.getMorador().getNome());
    }

    // texto amigavel pra etiqueta de status na lista
    private static String label(String status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case Ocorrencia.STATUS_ABERTA -> "Aberta";
            case Ocorrencia.STATUS_EM_ANDAMENTO -> "Em andamento";
            case Ocorrencia.STATUS_RESOLVIDA -> "Resolvida";
            default -> status;
        };
    }
}
