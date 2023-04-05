package searchengine.parsers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import searchengine.dto.statistics.StatisticsIndex;
import searchengine.morphology.MorphologyInterface;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SitePage;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
@Slf4j
@Component
@RequiredArgsConstructor
public class IndexParser implements IndexInterface {
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final MorphologyInterface getLemmaInterface;
    private List<StatisticsIndex> statisticsIndexList;

    @Override
    public List<StatisticsIndex> getIndexList() {
        return statisticsIndexList;
    }

    @Override
    public void run(SitePage site) {
        Iterable<Page> pageList = pageRepository.findBySiteId(site);
        List<Lemma> lemmaList = lemmaRepository.findBySitePageId(site);
        statisticsIndexList = new ArrayList<>();

        for (Page page : pageList) {
            if (page.getCode() < 400) {
                int pageId = page.getId();
                String pageContent = page.getContent();
                String title = HTMLCleaning.clear(pageContent, "title");
                String body = HTMLCleaning.clear(pageContent, "body");
                HashMap<String, Integer> titleList = getLemmaInterface.getLemmaList(title);
                HashMap<String, Integer> bodyList = getLemmaInterface.getLemmaList(body);

                for (Lemma lemma : lemmaList) {
                    Integer lemmaId = lemma.getId();
                    String theExactLemma = lemma.getLemma();
                    if (titleList.containsKey(theExactLemma) || bodyList.containsKey(theExactLemma)) {
                        float rank = 0.0F;
                        if (titleList.get(theExactLemma) != null) {
                            Float titleRank = Float.valueOf(titleList.get(theExactLemma));
                            rank += titleRank;
                        }
                        if (bodyList.get(theExactLemma) != null) {
                            float bodyRank = (float) (bodyList.get(theExactLemma) * 0.8);
                            rank += bodyRank;
                        }
                        statisticsIndexList.add(new StatisticsIndex(pageId, lemmaId, rank));
                    } else {
                        log.debug("Don't found the lemma");
                    }
                }
            } else {
                log.debug("Status code is bad - " + page.getCode());
            }
        }
    }


}