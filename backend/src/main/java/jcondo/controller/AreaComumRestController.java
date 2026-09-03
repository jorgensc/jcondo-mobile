package jcondo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jcondo.dto.AreaComumDTO;
import jcondo.dto.HorarioDTO;
import jcondo.service.AreaComumService;
import jcondo.service.ReservaService;

@RestController
@RequestMapping("/api/areas")
@Tag(name = "Áreas comuns", description = "Áreas reserváveis e sua agenda por dia")
public class AreaComumRestController {

    private final AreaComumService areaService;
    private final ReservaService reservaService;

    public AreaComumRestController(AreaComumService areaService, ReservaService reservaService) {
        this.areaService = areaService;
        this.reservaService = reservaService;
    }

    @GetMapping
    @Operation(summary = "Listar áreas comuns disponíveis para reserva")
    public List<AreaComumDTO> listar() {
        return areaService.listarAtivas().stream().map(AreaComumDTO::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar uma área comum")
    public AreaComumDTO buscar(@PathVariable Long id) {
        return AreaComumDTO.de(areaService.buscarPorId(id));
    }

    @GetMapping("/{id}/horarios")
    @Operation(summary = "Agenda da área em uma data (faixas de 2h com disponibilidade)")
    public List<HorarioDTO> horarios(
            @PathVariable Long id,
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return reservaService.agenda(id, data);
    }
}
