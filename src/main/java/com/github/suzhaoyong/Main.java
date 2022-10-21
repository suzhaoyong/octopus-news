package com.github.suzhaoyong;

import config.DBConnection;
import constants.db.LinksAlreadyProcessedDBConstants;
import constants.db.LinksToBeProcessedDBConstants;
import constants.db.NewsDBConstants;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Main {
    static final String INDEX_PAGE_URL = "https://sina.cn";

    public static void main(String[] args) {
        String link;
        while ((link = getNextLinkThenRemove()) != null) {
            if (isAlreadyProcessedLink(link)) {
                continue;
            }
            if (isInterestLink(link)) {
                if (link.startsWith("//")) {
                    link = "https:" + link;
                }
                System.out.println(link);
                Document doc = httpGetAndParseHtml(link);
                parseHtmlThenStoreLinkIntoToBeProcessedDataBase(doc);
                storeIntoDataBaseIfItIsNewsPage(doc, link);
                storeLinkIntoAlreadyProcessedDataBase(link);
            }
        }
    }

    private static String getNextLinkThenRemove() {
        String link = getNextLink();
        if (link != null) {
            removeToBeProcessedDataBaseEndLink(link);
            return link;
        }
        return null;
    }

    private static void storeLinkIntoAlreadyProcessedDataBase(String link) {
        PreparedStatement statement = null;
        try (Connection con = DBConnection.getCon();) {
            statement = con.prepareStatement("insert into " + LinksAlreadyProcessedDBConstants.TABLE_LINKS_ALREADY_PROCESSED + " (link) values (?)");
            statement.setString(1, link);
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            closePreparedStatement(statement);
        }
    }

    private static void parseHtmlThenStoreLinkIntoToBeProcessedDataBase(Document doc) {
        PreparedStatement statement = null;
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            try (Connection con = DBConnection.getCon();) {
                statement = con.prepareStatement("insert into " + LinksToBeProcessedDBConstants.TABLE_LINKS_TO_BE_PROCESSED + " (link) values (?)");
                statement.setString(1, href);
                statement.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } finally {
                closePreparedStatement(statement);
            }
        }
    }

    private static boolean isAlreadyProcessedLink(String link) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try (Connection con = DBConnection.getCon();) {
            statement = con.prepareStatement("select link from " + LinksAlreadyProcessedDBConstants.TABLE_LINKS_ALREADY_PROCESSED + " where link = ?");
            statement.setString(1, link);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            closePreparedStatement(statement);
            closeResultSet(resultSet);
        }
        return false;
    }

    private static void removeToBeProcessedDataBaseEndLink(String link) {
        PreparedStatement statement = null;
        try (Connection con = DBConnection.getCon();) {
            statement = con.prepareStatement("delete from " + LinksToBeProcessedDBConstants.TABLE_LINKS_TO_BE_PROCESSED + " where link = ?");
            statement.setString(1, link);
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            closePreparedStatement(statement);
        }
    }

    private static String getNextLink() {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try (Connection con = DBConnection.getCon()) {
            statement = con.prepareStatement("select link from " + LinksToBeProcessedDBConstants.TABLE_LINKS_TO_BE_PROCESSED + " limit 1");
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            closePreparedStatement(statement);
            closeResultSet(resultSet);
        }
        return null;
    }

    private static void closeResultSet(ResultSet resultSet) {
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


    private static boolean isInterestLink(String link) {
        return (isNewsLink(link) || isIndexPageLink(link)) && isNotLoginLink(link);
    }

    private static boolean isIndexPageLink(String link) {
        return INDEX_PAGE_URL.equals(link);
    }

    private static boolean isNewsLink(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isNotLoginLink(String link) {
        return !link.contains("passport.sina.cn");
    }

    private static Document httpGetAndParseHtml(String link) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1");
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            String html = EntityUtils.toString(entity);
            return Jsoup.parse(html);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void storeIntoDataBaseIfItIsNewsPage(Document doc, String link) {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            String title = articleTags.get(0).child(0).text();
            String content = articleTags.get(0).child(3).text();
            System.out.println(title);
            PreparedStatement statement = null;
            try (Connection con = DBConnection.getCon();) {
                statement = con.prepareStatement("insert into " + NewsDBConstants.TABLE_NEWS + " (title, content, url) values (?, ?, ?)");
                statement.setString(1, title);
                statement.setString(2, content);
                statement.setString(3, link);
                statement.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } finally {
                closePreparedStatement(statement);
            }
        }
    }

}
