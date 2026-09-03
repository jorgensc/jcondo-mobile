package jcondo.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jcondo.entity.Reserva;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByMoradorIdOrderByDataDescHoraInicioDesc(Long moradorId);

    List<Reserva> findByAreaIdAndDataAndStatus(Long areaId, LocalDate data, String status);

    // Regra de conflito, resolvida no banco e nao no aplicativo:
    // duas faixas se sobrepoem quando inicio < fimExistente E fim > inicioExistente.
    // O toque encostado (14:00-16:00 e 16:00-18:00) nao conta como conflito.
    @Query("""
            select count(r) from Reserva r
             where r.area.id = :areaId
               and r.data = :data
               and r.status = 'CONFIRMADA'
               and r.horaInicio < :horaFim
               and r.horaFim > :horaInicio
            """)
    long contarConflitos(@Param("areaId") Long areaId,
                         @Param("data") LocalDate data,
                         @Param("horaInicio") LocalTime horaInicio,
                         @Param("horaFim") LocalTime horaFim);

    // proximas reservas confirmadas do morador, da mais proxima pra mais distante
    @Query("""
            select r from Reserva r
             where r.morador.id = :moradorId
               and r.status = 'CONFIRMADA'
               and r.data >= :hoje
             order by r.data asc, r.horaInicio asc
            """)
    List<Reserva> proximasDoMorador(@Param("moradorId") Long moradorId,
                                    @Param("hoje") LocalDate hoje);
}
