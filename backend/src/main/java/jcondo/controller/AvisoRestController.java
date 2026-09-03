package jcondo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jcondo.dto.AvisoDTO;
import jcondo.entity.Aviso;
import jcondo.exception.RegraNegocioException;
import jcondo.security.UsuarioLogado;
import jcondo.service.AvisoService;

@RestController
@RequestMapping("/api/avisos")
@Tag(name = "Avisos", description = "Comunicados publicados pela administração")
public class AvisoRestController {

    private final AvisoService service;
    private final UsuarioLogado usuarioLogado;

    public AvisoRestController(AvisoService service, UsuarioLogado usuarioLogado) {
        this.service = service;
        this.usuarioLogado = usuarioLogado;
    }

    @GetMapping
    @Operation(summary = "Listar avisos ativos, do mais recente para o mais antigo")
    public List<AvisoDTO> listar() {
        return service.listarAtivos().stream().map(AvisoDTO::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar um aviso")
    public AvisoDTO buscar(@PathVariable Long id) {
        return AvisoDTO.de(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Publicar um aviso (somente perfil ADMIN)")
    public ResponseEntity<AvisoDTO> publicar(@Valid @RequestBody Aviso aviso) {
        if (!usuarioLogado.isAdmin()) {
            throw new RegraNegocioException(
                    "Apenas a administração do condomínio pode publicar avisos.");
        }
        Aviso salvo = service.publicar(aviso, usuarioLogado.getMorador().getNome());
        return ResponseEntity.status(HttpStatus.CREATED).body(AvisoDTO.de(salvo));
    }
}
