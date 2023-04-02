package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.SearchIndex;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.List;

@Repository
public interface SearchIndexRepository extends JpaRepository<SearchIndex, Long> {
    @Query(value = "SELECT i.* FROM index_search i WHERE i.lemma_id IN :lemmas AND i.page_id IN :pages", nativeQuery = true)
    List<SearchIndex> findByPagesAndLemmas(@Param("lemmas") List<Lemma> lemmaListId, @Param("pages") List<Page> pageListId);
}
