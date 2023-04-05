package searchengine.parsers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.StatisticsIndex;
import searchengine.dto.statistics.StatisticsLemma;
import searchengine.dto.statistics.StatisticsPage;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@RequiredArgsConstructor
public class IndexingSite implements Runnable {

    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private static final int CORES_COUNT = Runtime.getRuntime().availableProcessors();
    private final IndexRepository indexSearchRepository;
    private final LemmaInterface lemmaParserInterface;
    private final IndexInterface indexParserInterface;
    private final String url;
    private final SitesList sitesList;

    private List<StatisticsPage> getPageEntityDtoList() throws InterruptedException {
        if (!Thread.interrupted()) {
            String urlFormat = url + "/";
            List<StatisticsPage> statisticsPageDtoVector = new Vector<>();
            List<String> urlList = new Vector<>();
            ForkJoinPool forkJoinPool = new ForkJoinPool(CORES_COUNT);
            List<StatisticsPage> pages = forkJoinPool.invoke(new UrlParser(urlFormat, statisticsPageDtoVector, urlList));
            return new CopyOnWriteArrayList<>(pages);
        } else throw new InterruptedException();
    }

    @Override
    public void run() {
        if (siteRepository.findByUrl(url) != null) {
            log.info("Start delete site data - " + url);
            deleteDataFromSite();
        }
        log.info("Indexing this - " + url + " " + getName());
        saveSiteInDataBase();
        try {
            List<StatisticsPage> statisticsPageDtoList = getPageEntityDtoList();
            saveInBase(statisticsPageDtoList);
            getLemmasPage();
            indexingWords();
        } catch (InterruptedException e) {
            log.error("Indexing was stopped - " + url);
            errorIndexingSite();
        }
    }

    private void getLemmasPage() {
        if (!Thread.interrupted()) {
            SitePage sitePage = siteRepository.findByUrl(url);
            sitePage.setStatusTime(new Date());
            lemmaParserInterface.run(sitePage);
            List<StatisticsLemma> statisticsLemmaDtoList = lemmaParserInterface.getLemmaDtoList();
            List<Lemma> lemmaList = new CopyOnWriteArrayList<>();
            for (StatisticsLemma statisticsLemmaDto : statisticsLemmaDtoList) {
                lemmaList.add(new Lemma(statisticsLemmaDto.getLemma(), statisticsLemmaDto.getFrequency(), sitePage));
            }
            lemmaRepository.flush();
            lemmaRepository.saveAll(lemmaList);
        } else {
            throw new RuntimeException();
        }
    }

    private void saveInBase(List<StatisticsPage> pages) throws InterruptedException {
        if (!Thread.interrupted()) {
            List<Page> pageList = new CopyOnWriteArrayList<>();
            SitePage site = siteRepository.findByUrl(url);
            for (StatisticsPage page : pages) {
                int first = page.getUrl().indexOf(url) + url.length();
                String format = page.getUrl().substring(first);
                pageList.add(new Page(site, format, page.getCode(),
                        page.getContent()));
            }
            pageRepository.flush();
            pageRepository.saveAll(pageList);
        } else {
            throw new InterruptedException();
        }
    }

    private void deleteDataFromSite() {
        SitePage sitePage = siteRepository.findByUrl(url);
        sitePage.setStatus(Status.INDEXING);
        sitePage.setName(getName());
        sitePage.setStatusTime(new Date());
        siteRepository.save(sitePage);
        siteRepository.flush();
        siteRepository.delete(sitePage);
    }

    private void indexingWords() throws InterruptedException {
        if (!Thread.interrupted()) {
            SitePage sitePage = siteRepository.findByUrl(url);
            indexParserInterface.run(sitePage);
            List<StatisticsIndex> statisticsIndexDtoList = new CopyOnWriteArrayList<>(indexParserInterface.getIndexList());
            List<Index> indexList = new CopyOnWriteArrayList<>();
            sitePage.setStatusTime(new Date());
            for (StatisticsIndex statisticsIndexDto : statisticsIndexDtoList) {
                Page page = pageRepository.getById(statisticsIndexDto.getPageID());
                Lemma lemma = lemmaRepository.getById(statisticsIndexDto.getLemmaID());
                indexList.add(new Index(page, lemma, statisticsIndexDto.getRank()));
            }
            indexSearchRepository.flush();
            indexSearchRepository.saveAll(indexList);
            log.info("Indexing is Done - " + url);
            sitePage.setStatusTime(new Date());
            sitePage.setStatus(Status.INDEXED);
            siteRepository.save(sitePage);
        } else {
            throw new InterruptedException();
        }
    }

    private void saveSiteInDataBase() {
        SitePage sitePage = new SitePage();
        sitePage.setUrl(url);
        sitePage.setName(getName());
        sitePage.setStatus(Status.INDEXING);
        sitePage.setStatusTime(new Date());
        siteRepository.flush();
        siteRepository.save(sitePage);
    }

    private String getName() {
        List<Site> sitesList_2 = sitesList.getSites();
        for (Site map : sitesList_2) {
            if (map.getUrl().equals(url)) {
                return map.getName();
            }
        }
        return "";
    }

    private void errorIndexingSite() {
        SitePage sitePage = new SitePage();
        sitePage.setLastError("Stop indexing");
        sitePage.setStatus(Status.FAILED);
        sitePage.setStatusTime(new Date());
        siteRepository.save(sitePage);
    }
}
