package jcondo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jcondo.entity.AreaComum;
import jcondo.exception.RecursoNaoEncontradoException;
import jcondo.repository.AreaComumRepository;

@Service
public class AreaComumService {

    private final AreaComumRepository repository;

    public AreaComumService(AreaComumRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AreaComum> listarAtivas() {
        return repository.findByAtivaTrueOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public AreaComum buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Área comum não encontrada (id " + id + ")."));
    }
}
