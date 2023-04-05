package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.ModelIndex;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<ModelIndex, Integer> {
    @Query(value = "SELECT i.* FROM `index` i WHERE i.lemma_id IN :lemmas AND i.page_id IN :pages", nativeQuery = true)
    List<ModelIndex> findByPagesAndLemmas(@Param("lemmas") List<Lemma> lemmaListId, @Param("pages") List<Page> pageListId);
}
