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
import jcondo.dto.OcorrenciaDTO;
import jcondo.dto.OcorrenciaRequest;
import jcondo.entity.Ocorrencia;
import jcondo.security.UsuarioLogado;
import jcondo.service.OcorrenciaService;

@RestController
@RequestMapping("/api/ocorrencias")
@Tag(name = "Ocorrências", description = "Abertura e acompanhamento de chamados do morador")
public class OcorrenciaRestController {

    // as mesmas opcoes aparecem no seletor da tela de nova ocorrencia do app
    private static final List<String> CATEGORIAS = List.of(
            "Iluminação", "Limpeza", "Elevador", "Manutenção", "Segurança",
            "Barulho", "Área comum", "Outros");

    private final OcorrenciaService service;
    private final UsuarioLogado usuarioLogado;

    public OcorrenciaRestController(OcorrenciaService service, UsuarioLogado usuarioLogado) {
        this.service = service;
        this.usuarioLogado = usuarioLogado;
    }

    @GetMapping("/categorias")
    @Operation(summary = "Listar as categorias aceitas na abertura de ocorrência")
    public List<String> categorias() {
        return CATEGORIAS;
    }

    @GetMapping("/minhas")
    @Operation(summary = "Listar as ocorrências do morador autenticado")
    public List<OcorrenciaDTO> minhas() {
        return service.listarDoMorador(usuarioLogado.getId())
                .stream().map(OcorrenciaDTO::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar uma ocorrência própria")
    public OcorrenciaDTO buscar(@PathVariable Long id) {
        return OcorrenciaDTO.de(service.buscarDoMorador(id, usuarioLogado.getMorador()));
    }

    @PostMapping
    @Operation(summary = "Abrir uma ocorrência e receber o protocolo")
    public ResponseEntity<OcorrenciaDTO> abrir(@Valid @RequestBody OcorrenciaRequest request) {
        Ocorrencia o = service.abrir(
                usuarioLogado.getMorador(),
                request.getCategoria(),
                request.getTitulo(),
                request.getDescricao());
        return ResponseEntity.status(HttpStatus.CREATED).body(OcorrenciaDTO.de(o));
    }
}
