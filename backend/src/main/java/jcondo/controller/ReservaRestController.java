package jcondo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jcondo.dto.ReservaDTO;
import jcondo.dto.ReservaRequest;
import jcondo.entity.Reserva;
import jcondo.security.UsuarioLogado;
import jcondo.service.ReservaService;

@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Reserva de áreas comuns pelo morador autenticado")
public class ReservaRestController {

    private final ReservaService service;
    private final UsuarioLogado usuarioLogado;

    public ReservaRestController(ReservaService service, UsuarioLogado usuarioLogado) {
        this.service = service;
        this.usuarioLogado = usuarioLogado;
    }

    @GetMapping("/minhas")
    @Operation(summary = "Listar as reservas do morador autenticado")
    public List<ReservaDTO> minhas() {
        return service.listarDoMorador(usuarioLogado.getId())
                .stream().map(ReservaDTO::de).toList();
    }

    @PostMapping
    @Operation(summary = "Criar uma reserva (409 quando o horário já está ocupado)")
    public ResponseEntity<ReservaDTO> criar(@Valid @RequestBody ReservaRequest request) {
        Reserva reserva = service.criar(
                usuarioLogado.getMorador(),
                request.getAreaId(),
                request.getData(),
                request.getHoraInicio(),
                request.getHoraFim());
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservaDTO.de(reserva));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar uma reserva própria ainda não realizada")
    public ReservaDTO cancelar(@PathVariable Long id) {
        return ReservaDTO.de(service.cancelar(id, usuarioLogado.getMorador()));
    }
}
