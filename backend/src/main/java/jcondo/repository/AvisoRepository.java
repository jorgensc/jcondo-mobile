package jcondo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jcondo.entity.Aviso;

@Repository
public interface AvisoRepository extends JpaRepository<Aviso, Long> {

    List<Aviso> findByAtivoTrueOrderByDataPublicacaoDesc();
}
