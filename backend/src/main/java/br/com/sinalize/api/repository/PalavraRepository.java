package br.com.sinalize.api.repository;

import br.com.sinalize.api.entity.Palavra;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PalavraRepository extends JpaRepository<Palavra, Long> {

    @EntityGraph(attributePaths = {"categorias", "criadoPor", "atualizadoPor"})
    List<Palavra> findAllByOrderByCriadoEmAscIdAsc();

    @EntityGraph(attributePaths = {"categorias", "criadoPor", "atualizadoPor"})
    Optional<Palavra> findDetalhadaById(Long id);

    boolean existsByPalavraIgnoreCase(String palavra);

    boolean existsByPalavraIgnoreCaseAndIdNot(String palavra, Long id);

    boolean existsByCategorias_Id(Long categoriaId);

    @Query(value = """
            SELECT p.* FROM palavras p
            WHERE p.id IN (SELECT pc.palavra_id FROM palavras_categorias pc
                           WHERE pc.categoria_id IN (:categoriaIds))
            ORDER BY random()
            LIMIT :limite
            """, nativeQuery = true)
    List<Palavra> sortearPorCategorias(@Param("categoriaIds") Collection<Long> categoriaIds,
                                       @Param("limite") int limite);

    @Query(value = "SELECT p.* FROM palavras p ORDER BY random() LIMIT :limite", nativeQuery = true)
    List<Palavra> sortear(@Param("limite") int limite);

    @Query(value = """
            SELECT p.* FROM palavras p
            WHERE p.id NOT IN (:excluir)
            ORDER BY random()
            LIMIT :limite
            """, nativeQuery = true)
    List<Palavra> sortearExcluindo(@Param("excluir") Collection<Long> excluir,
                                   @Param("limite") int limite);
}
