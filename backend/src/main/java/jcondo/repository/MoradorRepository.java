package jcondo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jcondo.entity.Morador;

@Repository
public interface MoradorRepository extends JpaRepository<Morador, Long> {

    List<Morador> findAllByOrderByNomeAsc();

    Optional<Morador> findByEmailIgnoreCase(String email);
}
