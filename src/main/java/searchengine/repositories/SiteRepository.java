package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SitePage;
@Repository
public interface SiteRepository extends JpaRepository<SitePage, Integer> {
    SitePage findByUrl(String url);
    SitePage findByUrl(int id);
    SitePage findByUrl(SitePage site);
}