package com.github.suzhaoyong;

import config.DBConnection;
import constants.db.LinksAlreadyProcessedDBConstants;
import constants.db.LinksToBeProcessedDBConstants;
import constants.db.NewsDBConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcCrawlerDao implements CrawlerDao {
     private String getNextLink() throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try (Connection con = DBConnection.getCon()) {
            statement = con.prepareStatement("select link from " + LinksToBeProcessedDBConstants.TABLE_LINKS_TO_BE_PROCESSED + " limit 1");
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
        } finally {
            closePreparedStatement(statement);
            closeResultSet(resultSet);
        }
        return null;
    }
    public String getNextLinkThenRemove() throws SQLException {
        String link = getNextLink();
        if (link != null) {
            removeToBeProcessedDataBaseEndLink(link);
            return link;
        }
        return null;
    }
    public void storeLinkIntoToBeProcessedDataBase(String href) throws SQLException {
        PreparedStatement statement = null;
        try (Connection con = DBConnection.getCon();) {
            statement = con.prepareStatement("insert into " + LinksToBeProcessedDBConstants.TABLE_LINKS_TO_BE_PROCESSED + " (link) values (?)");
            statement.setString(1, href);
            statement.executeUpdate();
        } finally {
            closePreparedStatement(statement);
        }
    }
    public void storeLinkIntoAlreadyProcessedDataBase(String link) throws SQLException {
        PreparedStatement statement = null;
        try (Connection con = DBConnection.getCon();) {
            statement = con.prepareStatement("insert into " + LinksAlreadyProcessedDBConstants.TABLE_LINKS_ALREADY_PROCESSED + " (link) values (?)");
            statement.setString(1, link);
            statement.executeUpdate();
        } finally {
            closePreparedStatement(statement);
        }
    }
    public boolean isAlreadyProcessedLink(String link) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try (Connection con = DBConnection.getCon();) {
            statement = con.prepareStatement("select link from " + LinksAlreadyProcessedDBConstants.TABLE_LINKS_ALREADY_PROCESSED + " where link = ?");
            statement.setString(1, link);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } finally {
            closePreparedStatement(statement);
            closeResultSet(resultSet);
        }
        return false;
    }
    private void removeToBeProcessedDataBaseEndLink(String link) throws SQLException {
        PreparedStatement statement = null;
        try (Connection con = DBConnection.getCon();) {
            statement = con.prepareStatement("delete from " + LinksToBeProcessedDBConstants.TABLE_LINKS_TO_BE_PROCESSED + " where link = ?");
            statement.setString(1, link);
            statement.executeUpdate();
        } finally {
            closePreparedStatement(statement);
        }
    }
    public void storeNewsIntoDataBase(String link, String title, String content) throws SQLException {
        PreparedStatement statement = null;
        try (Connection con = DBConnection.getCon();) {
            statement = con.prepareStatement("insert into " + NewsDBConstants.TABLE_NEWS + " (title, content, url) values (?, ?, ?)");
            statement.setString(1, title);
            statement.setString(2, content);
            statement.setString(3, link);
            statement.executeUpdate();
        } finally {
            closePreparedStatement(statement);
        }
    }
    private static void closeResultSet(ResultSet resultSet){
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

    }
    private static void closePreparedStatement(PreparedStatement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

}
