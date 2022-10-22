package com.github.suzhaoyong;

public interface CrawlerDao {
    String getNextLink();
    void storeLinkIntoToBeProcessedDataBase(String href);
    void storeLinkIntoAlreadyProcessedDataBase(String link);
    boolean isAlreadyProcessedLink(String link);
    void removeToBeProcessedDataBaseEndLink(String link);
    void storeNewsIntoDataBase(String link, String title, String content);
}
