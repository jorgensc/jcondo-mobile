package jcondo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jcondo.entity.Ocorrencia;

@Repository
public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {

    List<Ocorrencia> findByMoradorIdOrderByDataAberturaDesc(Long moradorId);

    long countByMoradorIdAndStatusNot(Long moradorId, String status);

    boolean existsByProtocolo(String protocolo);

    List<Ocorrencia> findAllByOrderByDataAberturaDesc();
}
