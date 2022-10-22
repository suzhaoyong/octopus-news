package com.github.suzhaoyong;

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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Crawler extends Thread {
    static final String INDEX_PAGE_URL = "https://sina.cn";
    private CrawlerDao dao;

    public Crawler(CrawlerDao dao) {
        this.dao = dao;
    }

    @Override
    public void run() {
        String link;
        try {
            while ((link = dao.getNextLinkThenRemove()) != null) {
                if (dao.isAlreadyProcessedLink(link)) {
                    continue;
                }
                if (isInterestLink(link)) {
                    System.out.println(link);
                    Document doc = httpGetAndParseHtml(link);
                    parseHtmlThenStoreLinkIntoToBeProcessedDataBase(doc);
                    storeIntoDataBaseIfItIsNewsPage(doc, link);
                    dao.storeLinkIntoAlreadyProcessedDataBase(link);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void parseHtmlThenStoreLinkIntoToBeProcessedDataBase(Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            if (isTerribleLink(href)) {
                continue;
            }
            if (href.startsWith("//")) {
                href = "https:" + href;
            }
            dao.storeLinkIntoToBeProcessedDataBase(href);
        }
    }

    private boolean isTerribleLink(String href) {
        return href.toLowerCase().startsWith("javascript") || "".equals(href.trim()) || href.startsWith("#");
    }

    private boolean isInterestLink(String link) {
        return (isNewsLink(link) || isIndexPageLink(link)) && isNotLoginLink(link);
    }

    private boolean isIndexPageLink(String link) {
        return INDEX_PAGE_URL.equals(link);
    }

    private boolean isNewsLink(String link) {
        return link.contains("news.sina.cn");
    }

    private boolean isNotLoginLink(String link) {
        return !link.contains("passport.sina.cn");
    }

    private Document httpGetAndParseHtml(String link) {
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

    private void storeIntoDataBaseIfItIsNewsPage(Document doc, String link) throws SQLException {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTags.get(0).child(0).text();
                String content = articleTag.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
                System.out.println(title);
                dao.storeNewsIntoDataBase(link, title, content);
            }
        }
    }


}
