package jcondo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jcondo.dto.HorarioDTO;
import jcondo.entity.AreaComum;
import jcondo.entity.Morador;
import jcondo.entity.Reserva;
import jcondo.exception.ConflitoReservaException;
import jcondo.exception.RegraNegocioException;
import jcondo.repository.ReservaRepository;

/**
 * Testes da regra de reserva - a parte do sistema com mais chance de erro,
 * porque e onde dois moradores podem disputar o mesmo horario.
 * Repositorio mockado, entao os testes rodam sem banco.
 */
@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository repository;

    @Mock
    private AreaComumService areaService;

    @InjectMocks
    private ReservaService service;

    private Morador morador;
    private AreaComum salao;
    private LocalDate amanha;

    @BeforeEach
    void setUp() {
        morador = new Morador("Ana Paula Souza", "101", "A",
                "123.456.789-01", "(49) 99911-1111", "ana.souza@email.com");
        morador.setId(1L);
        morador.setPerfil(Morador.PERFIL_MORADOR);

        salao = new AreaComum("Salão de Festas", "Espaço coberto", 60,
                LocalTime.of(10, 0), LocalTime.of(22, 0), "Devolver limpo.");
        salao.setId(10L);

        amanha = LocalDate.now().plusDays(1);
    }

    @Test
    void deveCriarReservaQuandoNaoHaConflito() {
        when(areaService.buscarPorId(10L)).thenReturn(salao);
        when(repository.contarConflitos(eq(10L), eq(amanha), any(), any())).thenReturn(0L);
        when(repository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        Reserva reserva = service.criar(morador, 10L, amanha,
                LocalTime.of(18, 0), LocalTime.of(20, 0));

        assertEquals(Reserva.STATUS_CONFIRMADA, reserva.getStatus());
        assertEquals(salao, reserva.getArea());
        verify(repository).save(any(Reserva.class));
    }

    @Test
    void deveRecusarReservaQuandoHorarioJaEstaOcupado() {
        when(areaService.buscarPorId(10L)).thenReturn(salao);
        when(repository.contarConflitos(eq(10L), eq(amanha), any(), any())).thenReturn(1L);

        // o servidor e quem barra: mesmo que o app deixasse passar, nao grava
        assertThrows(ConflitoReservaException.class, () ->
                service.criar(morador, 10L, amanha, LocalTime.of(18, 0), LocalTime.of(20, 0)));

        verify(repository, never()).save(any(Reserva.class));
    }

    @Test
    void deveRecusarReservaEmDataPassada() {
        when(areaService.buscarPorId(10L)).thenReturn(salao);

        assertThrows(RegraNegocioException.class, () ->
                service.criar(morador, 10L, LocalDate.now().minusDays(1),
                        LocalTime.of(18, 0), LocalTime.of(20, 0)));

        verify(repository, never()).save(any(Reserva.class));
    }

    @Test
    void deveRecusarHorarioForaDoFuncionamentoDaArea() {
        when(areaService.buscarPorId(10L)).thenReturn(salao);

        // salao funciona das 10h as 22h
        assertThrows(RegraNegocioException.class, () ->
                service.criar(morador, 10L, amanha, LocalTime.of(8, 0), LocalTime.of(10, 0)));
    }

    @Test
    void deveRecusarQuandoTerminoNaoEDepoisDoInicio() {
        when(areaService.buscarPorId(10L)).thenReturn(salao);

        assertThrows(RegraNegocioException.class, () ->
                service.criar(morador, 10L, amanha, LocalTime.of(20, 0), LocalTime.of(18, 0)));
    }

    @Test
    void agendaDeveMarcarComoIndisponivelApenasAFaixaOcupada() {
        Reserva ocupada = new Reserva(morador, salao, amanha,
                LocalTime.of(14, 0), LocalTime.of(16, 0));
        ocupada.setStatus(Reserva.STATUS_CONFIRMADA);

        when(areaService.buscarPorId(10L)).thenReturn(salao);
        when(repository.findByAreaIdAndDataAndStatus(10L, amanha, Reserva.STATUS_CONFIRMADA))
                .thenReturn(List.of(ocupada));

        List<HorarioDTO> agenda = service.agenda(10L, amanha);

        // das 10h as 22h em blocos de 2h = 6 faixas
        assertEquals(6, agenda.size());
        HorarioDTO faixaOcupada = agenda.stream()
                .filter(h -> h.horaInicio().equals("14:00")).findFirst().orElseThrow();
        HorarioDTO faixaSeguinte = agenda.stream()
                .filter(h -> h.horaInicio().equals("16:00")).findFirst().orElseThrow();

        assertFalse(faixaOcupada.disponivel());
        // faixa encostada na reserva continua livre: 16:00 nao conflita com 14:00-16:00
        assertTrue(faixaSeguinte.disponivel());
    }

    @Test
    void naoDeveCancelarReservaDeOutroMorador() {
        Morador outro = new Morador("Bruno Oliveira Lima", "202", "B",
                "234.567.890-12", "(49) 99922-2222", "bruno.lima@email.com");
        outro.setId(2L);
        outro.setPerfil(Morador.PERFIL_MORADOR);

        Reserva reserva = new Reserva(morador, salao, amanha,
                LocalTime.of(18, 0), LocalTime.of(20, 0));
        reserva.setId(99L);
        when(repository.findById(99L)).thenReturn(java.util.Optional.of(reserva));

        assertThrows(RegraNegocioException.class, () -> service.cancelar(99L, outro));
        verify(repository, never()).save(any(Reserva.class));
    }

    @Test
    void deveCancelarReservaFuturaDoProprioMorador() {
        Reserva reserva = new Reserva(morador, salao, amanha,
                LocalTime.of(18, 0), LocalTime.of(20, 0));
        reserva.setId(99L);
        when(repository.findById(99L)).thenReturn(java.util.Optional.of(reserva));
        when(repository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        Reserva cancelada = service.cancelar(99L, morador);

        assertEquals(Reserva.STATUS_CANCELADA, cancelada.getStatus());
    }

    @Test
    void contarAtivasDeveUsarSomenteAsReservasFuturasConfirmadas() {
        when(repository.proximasDoMorador(anyLong(), any(LocalDate.class)))
                .thenReturn(List.of(new Reserva(morador, salao, amanha,
                        LocalTime.of(18, 0), LocalTime.of(20, 0))));

        assertEquals(1, service.contarAtivas(1L));
    }
}
