package searchengine.parsers;

import searchengine.dto.statistics.StatisticsLemma;
import searchengine.model.SitePage;

import java.util.List;

public interface LemmaInterface {
    void run(SitePage site);
    List<StatisticsLemma> getLemmaList();
}