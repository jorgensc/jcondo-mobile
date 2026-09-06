package jcondo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jcondo.dto.HorarioDTO;
import jcondo.entity.AreaComum;
import jcondo.entity.Morador;
import jcondo.entity.Reserva;
import jcondo.exception.ConflitoReservaException;
import jcondo.exception.RecursoNaoEncontradoException;
import jcondo.exception.RegraNegocioException;
import jcondo.repository.ReservaRepository;

/**
 * Regras de reserva de area comum.
 *
 * Toda validacao esta aqui, no servidor. O app esconde os horarios ocupados
 * para facilitar, mas quem decide se a reserva pode ser gravada e este service:
 * se dois moradores enviarem o mesmo horario ao mesmo tempo, um recebe 409.
 */
@Service
public class ReservaService {

    // duracao das faixas mostradas na agenda da area
    private static final int BLOCO_HORAS = 2;
    private static final int MINUTOS_DO_DIA = 24 * 60;

    private final ReservaRepository repository;
    private final AreaComumService areaService;

    public ReservaService(ReservaRepository repository, AreaComumService areaService) {
        this.repository = repository;
        this.areaService = areaService;
    }

    @Transactional(readOnly = true)
    public List<Reserva> listarDoMorador(Long moradorId) {
        return repository.findByMoradorIdOrderByDataDescHoraInicioDesc(moradorId);
    }

    @Transactional(readOnly = true)
    public Reserva proximaDoMorador(Long moradorId) {
        List<Reserva> proximas = repository.proximasDoMorador(moradorId, LocalDate.now());
        return proximas.isEmpty() ? null : proximas.get(0);
    }

    @Transactional(readOnly = true)
    public int contarAtivas(Long moradorId) {
        return repository.proximasDoMorador(moradorId, LocalDate.now()).size();
    }

    /** Agenda do dia: faixas de 2h dentro do funcionamento, marcando as ocupadas. */
    @Transactional(readOnly = true)
    public List<HorarioDTO> agenda(Long areaId, LocalDate data) {
        AreaComum area = areaService.buscarPorId(areaId);
        List<Reserva> ocupadas = repository.findByAreaIdAndDataAndStatus(
                areaId, data, Reserva.STATUS_CONFIRMADA);

        List<HorarioDTO> faixas = new ArrayList<>();

        // A contagem e feita em minutos desde a meia-noite, e nao somando horas
        // direto no LocalTime: 22:00 mais duas horas volta para 00:00, que nunca
        // e "depois" do fechamento, e o laco jamais terminaria.
        int bloco = BLOCO_HORAS * 60;
        int abertura = minutos(area.getHorarioAbertura());
        int fechamento = minutos(area.getHorarioFechamento());
        if (fechamento <= abertura) {
            // area que atravessa a meia-noite (ex.: 18:00 as 02:00)
            fechamento += MINUTOS_DO_DIA;
        }

        for (int minuto = abertura; minuto + bloco <= fechamento; minuto += bloco) {
            LocalTime inicio = emHora(minuto);
            LocalTime fim = emHora(minuto + bloco);

            boolean livre = true;
            for (Reserva r : ocupadas) {
                if (inicio.isBefore(r.getHoraFim()) && fim.isAfter(r.getHoraInicio())) {
                    livre = false;
                    break;
                }
            }
            // faixa que ja passou no dia de hoje tambem entra como indisponivel
            if (livre && data.isEqual(LocalDate.now())
                    && inicio.isBefore(LocalTime.now())) {
                livre = false;
            }
            faixas.add(new HorarioDTO(hhmm(inicio), hhmm(fim), livre));
        }
        return faixas;
    }

    @Transactional
    public Reserva criar(Morador morador, Long areaId, LocalDate data,
                         LocalTime horaInicio, LocalTime horaFim) {

        AreaComum area = areaService.buscarPorId(areaId);

        if (!area.isAtiva()) {
            throw new RegraNegocioException(
                    "A área " + area.getNome() + " está indisponível para reservas.");
        }
        if (!horaFim.isAfter(horaInicio)) {
            throw new RegraNegocioException(
                    "O horário de término precisa ser depois do horário de início.");
        }
        if (data.isBefore(LocalDate.now())) {
            throw new RegraNegocioException("Não é possível reservar uma data que já passou.");
        }
        if (data.isEqual(LocalDate.now()) && horaInicio.isBefore(LocalTime.now())) {
            throw new RegraNegocioException("Escolha um horário posterior ao horário atual.");
        }
        if (horaInicio.isBefore(area.getHorarioAbertura())
                || horaFim.isAfter(area.getHorarioFechamento())) {
            throw new RegraNegocioException(String.format(
                    "A área %s funciona das %s às %s.",
                    area.getNome(), hhmm(area.getHorarioAbertura()),
                    hhmm(area.getHorarioFechamento())));
        }
        if (data.isAfter(LocalDate.now().plusDays(90))) {
            throw new RegraNegocioException("As reservas são liberadas com até 90 dias de antecedência.");
        }

        // a checagem de conflito e a ultima coisa antes de gravar
        long conflitos = repository.contarConflitos(areaId, data, horaInicio, horaFim);
        if (conflitos > 0) {
            throw new ConflitoReservaException(String.format(
                    "O horário das %s às %s já está reservado em %s. Escolha outro horário.",
                    hhmm(horaInicio), hhmm(horaFim), area.getNome()));
        }

        Reserva reserva = new Reserva(morador, area, data, horaInicio, horaFim);
        reserva.setStatus(Reserva.STATUS_CONFIRMADA);
        reserva.setCriadaEm(LocalDateTime.now());
        return repository.save(reserva);
    }

    @Transactional
    public Reserva cancelar(Long reservaId, Morador morador) {
        Reserva reserva = repository.findById(reservaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Reserva não encontrada (id " + reservaId + ")."));

        // um morador so pode mexer nas proprias reservas (o admin pode em todas)
        boolean dono = reserva.getMorador().getId().equals(morador.getId());
        boolean admin = Morador.PERFIL_ADMIN.equalsIgnoreCase(morador.getPerfil());
        if (!dono && !admin) {
            throw new RegraNegocioException("Esta reserva pertence a outro morador.");
        }
        if (Reserva.STATUS_CANCELADA.equals(reserva.getStatus())) {
            throw new RegraNegocioException("Esta reserva já foi cancelada.");
        }
        LocalDateTime inicio = LocalDateTime.of(reserva.getData(), reserva.getHoraInicio());
        if (inicio.isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("Não é possível cancelar uma reserva que já ocorreu.");
        }

        reserva.setStatus(Reserva.STATUS_CANCELADA);
        return repository.save(reserva);
    }

    private static String hhmm(LocalTime hora) {
        return String.format("%02d:%02d", hora.getHour(), hora.getMinute());
    }

    private static int minutos(LocalTime hora) {
        return hora.getHour() * 60 + hora.getMinute();
    }

    /** Volta de "minutos desde a meia-noite" para LocalTime, dando a volta no dia. */
    private static LocalTime emHora(int minutoDoDia) {
        int normalizado = minutoDoDia % MINUTOS_DO_DIA;
        return LocalTime.of(normalizado / 60, normalizado % 60);
    }
}
