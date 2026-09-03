package jcondo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jcondo.entity.AreaComum;

@Repository
public interface AreaComumRepository extends JpaRepository<AreaComum, Long> {

    List<AreaComum> findByAtivaTrueOrderByNomeAsc();
}
