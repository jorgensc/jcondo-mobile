package jcondo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jcondo.entity.Morador;

@Repository
public interface MoradorRepository extends JpaRepository<Morador, Long> {

    // lista ordenada por nome pra tabela ja vir em ordem alfabetica
    List<Morador> findAllByOrderByNomeAsc();

    // usado no login do app - o e-mail e a credencial de acesso
    Optional<Morador> findByEmailIgnoreCase(String email);
}
