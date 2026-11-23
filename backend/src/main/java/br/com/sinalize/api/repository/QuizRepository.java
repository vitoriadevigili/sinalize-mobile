package br.com.sinalize.api.repository;

import br.com.sinalize.api.entity.Quiz;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @EntityGraph(attributePaths = {"usuario", "itens", "itens.palavra"})
    Optional<Quiz> findDetalhadoById(Long id);

    @EntityGraph(attributePaths = {"usuario", "itens", "itens.palavra"})
    List<Quiz> findAllByOrderByCriadoEmAscIdAsc();
}
