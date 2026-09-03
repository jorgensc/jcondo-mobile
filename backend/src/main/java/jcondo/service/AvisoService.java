package jcondo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jcondo.entity.Aviso;
import jcondo.exception.RecursoNaoEncontradoException;
import jcondo.repository.AvisoRepository;

@Service
public class AvisoService {

    private final AvisoRepository repository;

    public AvisoService(AvisoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Aviso> listarAtivos() {
        return repository.findByAtivoTrueOrderByDataPublicacaoDesc();
    }

    @Transactional(readOnly = true)
    public List<Aviso> listarRecentes(int quantidade) {
        List<Aviso> todos = listarAtivos();
        return todos.size() <= quantidade ? todos : todos.subList(0, quantidade);
    }

    @Transactional(readOnly = true)
    public Aviso buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Aviso não encontrado (id " + id + ")."));
    }

    @Transactional
    public Aviso publicar(Aviso aviso, String autor) {
        aviso.setId(null);
        aviso.setAtivo(true);
        aviso.setDataPublicacao(LocalDateTime.now());
        if (aviso.getAutor() == null || aviso.getAutor().isBlank()) {
            aviso.setAutor(autor);
        }
        if (aviso.getPrioridade() == null || aviso.getPrioridade().isBlank()) {
            aviso.setPrioridade("NORMAL");
        }
        return repository.save(aviso);
    }
}
