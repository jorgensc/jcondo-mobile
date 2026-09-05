package jcondo.controller;

import java.time.LocalTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jcondo.dto.AvisoDTO;
import jcondo.dto.MoradorDTO;
import jcondo.dto.ReservaDTO;
import jcondo.dto.ResumoHomeDTO;
import jcondo.entity.Morador;
import jcondo.entity.Reserva;
import jcondo.exception.RegraNegocioException;
import jcondo.security.SenhaUtil;
import jcondo.security.UsuarioLogado;
import jcondo.service.AvisoService;
import jcondo.service.MoradorService;
import jcondo.service.OcorrenciaService;
import jcondo.service.ReservaService;

/**
 * Perfil e resumo da tela inicial. Nenhum endpoint aqui recebe id por
 * parametro: quem responde e sempre o dono do token.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Perfil e início", description = "Dados do morador autenticado e resumo da tela inicial")
public class PerfilRestController {

    private final UsuarioLogado usuarioLogado;
    private final MoradorService moradorService;
    private final AvisoService avisoService;
    private final ReservaService reservaService;
    private final OcorrenciaService ocorrenciaService;

    public PerfilRestController(UsuarioLogado usuarioLogado,
                                MoradorService moradorService,
                                AvisoService avisoService,
                                ReservaService reservaService,
                                OcorrenciaService ocorrenciaService) {
        this.usuarioLogado = usuarioLogado;
        this.moradorService = moradorService;
        this.avisoService = avisoService;
        this.reservaService = reservaService;
        this.ocorrenciaService = ocorrenciaService;
    }

    @GetMapping("/perfil")
    @Operation(summary = "Dados do morador autenticado")
    public MoradorDTO perfil() {
        return MoradorDTO.de(usuarioLogado.getMorador());
    }

    /**
     * Atualiza somente o que o morador pode mudar sozinho.
     * Bloco, apartamento, CPF e perfil ficam a cargo da administracao.
     */
    @PutMapping("/perfil")
    @Operation(summary = "Atualizar telefone, e-mail e senha do próprio cadastro")
    public MoradorDTO atualizarPerfil(@RequestBody AtualizacaoPerfil dados) {
        Morador morador = usuarioLogado.getMorador();

        if (dados.telefone() != null && !dados.telefone().isBlank()) {
            morador.setTelefone(dados.telefone().trim());
        }
        if (dados.email() != null && !dados.email().isBlank()) {
            String email = dados.email().trim();
            if (!email.contains("@")) {
                throw new RegraNegocioException("Informe um e-mail válido.");
            }
            morador.setEmail(email);
        }
        if (dados.novaSenha() != null && !dados.novaSenha().isBlank()) {
            if (!SenhaUtil.confere(dados.senhaAtual(), morador.getSenha())) {
                throw new RegraNegocioException("A senha atual informada está incorreta.");
            }
            if (dados.novaSenha().trim().length() < 6) {
                throw new RegraNegocioException("A nova senha deve ter ao menos 6 caracteres.");
            }
            morador.setSenha(SenhaUtil.hash(dados.novaSenha().trim()));
        }

        return MoradorDTO.de(moradorService.salvar(morador));
    }

    @GetMapping("/home/resumo")
    @Operation(summary = "Resumo da tela inicial em uma única chamada")
    public ResumoHomeDTO resumo() {
        Morador morador = usuarioLogado.getMorador();

        List<AvisoDTO> recentes = avisoService.listarRecentes(3)
                .stream().map(AvisoDTO::de).toList();

        Reserva proxima = reservaService.proximaDoMorador(morador.getId());

        return new ResumoHomeDTO(
                MoradorDTO.de(morador),
                saudacao(),
                recentes,
                proxima == null ? null : ReservaDTO.de(proxima),
                avisoService.listarAtivos().size(),
                reservaService.contarAtivas(morador.getId()),
                ocorrenciaService.contarAbertas(morador.getId()));
    }

    // saudacao calculada no servidor pra ficar igual em qualquer aparelho,
    // independente de fuso ou relogio do celular
    private String saudacao() {
        LocalTime agora = LocalTime.now();
        if (agora.isBefore(LocalTime.NOON)) {
            return "Bom dia";
        }
        if (agora.isBefore(LocalTime.of(18, 0))) {
            return "Boa tarde";
        }
        return "Boa noite";
    }

    public record AtualizacaoPerfil(String telefone, String email,
                                    String senhaAtual, String novaSenha) {
    }
}
