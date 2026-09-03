package com.jcondo.mobile.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Conversao entre o formato que a API usa (ISO: 2026-09-10) e o formato que
 * o morador brasileiro le (10/09/2026, quinta-feira).
 */
public final class Datas {

    private static final Locale BR = new Locale("pt", "BR");
    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter CURTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Datas() {
    }

    public static String paraIso(LocalDate data) {
        return data == null ? "" : data.format(ISO);
    }

    public static String paraBr(LocalDate data) {
        return data == null ? "" : data.format(CURTA);
    }

    public static LocalDate deIso(String iso) {
        try {
            return LocalDate.parse(iso, ISO);
        } catch (Exception e) {
            return null;
        }
    }

    /** Ex.: "quinta-feira, 10/09/2026" - o dia da semana ajuda na conferencia. */
    public static String porExtenso(LocalDate data) {
        if (data == null) {
            return "";
        }
        String diaSemana = data.getDayOfWeek().getDisplayName(TextStyle.FULL, BR);
        return diaSemana + ", " + data.format(CURTA);
    }

    /** "Hoje" e "Amanhã" comunicam melhor que a data crua numa lista. */
    public static String relativa(String dataIso) {
        LocalDate data = deIso(dataIso);
        if (data == null) {
            return dataIso == null ? "" : dataIso;
        }
        LocalDate hoje = LocalDate.now();
        if (data.isEqual(hoje)) {
            return "Hoje";
        }
        if (data.isEqual(hoje.plusDays(1))) {
            return "Amanhã";
        }
        return porExtenso(data);
    }

    public static boolean jaPassou(String dataIso, String horaFim) {
        LocalDate data = deIso(dataIso);
        if (data == null) {
            return false;
        }
        if (data.isBefore(LocalDate.now())) {
            return true;
        }
        if (data.isAfter(LocalDate.now())) {
            return false;
        }
        try {
            String[] hm = horaFim.split(":");
            java.time.LocalTime fim = java.time.LocalTime.of(
                    Integer.parseInt(hm[0]), Integer.parseInt(hm[1]));
            return fim.isBefore(java.time.LocalTime.now());
        } catch (Exception e) {
            return false;
        }
    }
}
