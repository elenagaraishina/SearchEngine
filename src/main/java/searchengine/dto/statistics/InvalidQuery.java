package searchengine.dto.statistics;

import lombok.Value;

@Value
public class InvalidQuery {
    boolean getResult;
    String errorMessage;

}