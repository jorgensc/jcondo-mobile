package jcondo.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jcondo.entity.Morador;
import jcondo.entity.Ocorrencia;
import jcondo.exception.RecursoNaoEncontradoException;
import jcondo.exception.RegraNegocioException;
import jcondo.repository.OcorrenciaRepository;

@Service
public class OcorrenciaService {

    private static final DateTimeFormatter PREFIXO = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OcorrenciaRepository repository;

    public OcorrenciaService(OcorrenciaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Ocorrencia> listarDoMorador(Long moradorId) {
        return repository.findByMoradorIdOrderByDataAberturaDesc(moradorId);
    }

    @Transactional(readOnly = true)
    public int contarAbertas(Long moradorId) {
        return (int) repository.countByMoradorIdAndStatusNot(
                moradorId, Ocorrencia.STATUS_RESOLVIDA);
    }

    @Transactional(readOnly = true)
    public Ocorrencia buscarDoMorador(Long id, Morador morador) {
        Ocorrencia o = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Ocorrência não encontrada (id " + id + ")."));
        boolean dono = o.getMorador().getId().equals(morador.getId());
        boolean admin = Morador.PERFIL_ADMIN.equalsIgnoreCase(morador.getPerfil());
        if (!dono && !admin) {
            throw new RegraNegocioException("Esta ocorrência pertence a outro morador.");
        }
        return o;
    }

    @Transactional
    public Ocorrencia abrir(Morador morador, String categoria, String titulo, String descricao) {
        Ocorrencia o = new Ocorrencia(gerarProtocolo(), morador,
                categoria, titulo.trim(), descricao.trim());
        o.setStatus(Ocorrencia.STATUS_ABERTA);
        o.setDataAbertura(LocalDateTime.now());
        return repository.save(o);
    }

    // protocolo no formato 20260902-4817: data de abertura + sufixo aleatorio
    private String gerarProtocolo() {
        for (int tentativa = 0; tentativa < 10; tentativa++) {
            String candidato = LocalDateTime.now().format(PREFIXO)
                    + "-" + ThreadLocalRandom.current().nextInt(1000, 10000);
            if (!repository.existsByProtocolo(candidato)) {
                return candidato;
            }
        }
        return LocalDateTime.now().format(PREFIXO) + "-" + System.currentTimeMillis() % 100000;
    }
}
