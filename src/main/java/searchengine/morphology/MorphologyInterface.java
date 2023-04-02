package searchengine.morphology;

import java.util.HashMap;
import java.util.List;

public interface MorphologyInterface {
    HashMap<String, Integer> getLemmaList(String content);
    List<String> getLemma(String word);
    List<Integer> findIndexLemmaInText(String content, String lemma);
}