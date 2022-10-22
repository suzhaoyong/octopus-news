package com.github.suzhaoyong;

import java.sql.SQLException;

public interface CrawlerDao {
    String getNextLinkThenRemove() throws SQLException;
    void storeLinkIntoToBeProcessedDataBase(String href) throws SQLException;
    void storeLinkIntoAlreadyProcessedDataBase(String link) throws SQLException;
    boolean isAlreadyProcessedLink(String link) throws SQLException;
    void storeNewsIntoDataBase(String link, String title, String content) throws SQLException;
}
