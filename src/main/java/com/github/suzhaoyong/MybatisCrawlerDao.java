package com.github.suzhaoyong;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;

public class MybatisCrawlerDao implements CrawlerDao {
    private SqlSessionFactory sqlSessionFactory;

    public MybatisCrawlerDao() {
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized String getNextLinkThenRemove() throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            String link = session.selectOne("com.github.suzhaoyong.Crawler.selectNextAvailableLink");
            if (link != null) {
                session.delete("com.github.suzhaoyong.Crawler.deleteLink", link);
                return link;
            }
        }
        return null;
    }

    @Override
    public void storeLinkIntoToBeProcessedDataBase(String link) throws SQLException {
        insertIntoLink(link, "links_to_be_processed");
    }

    @Override
    public void storeLinkIntoAlreadyProcessedDataBase(String link) throws SQLException {
        insertIntoLink(link, "links_already_processed");
    }

    private void insertIntoLink(String link, String tableName) {
        HashMap<String, Object> param = new HashMap<>();
        param.put("tableName", tableName);
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.suzhaoyong.Crawler.storeLink", param);
        }
    }

    @Override
    public boolean isAlreadyProcessedLink(String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            int count = session.selectOne("com.github.suzhaoyong.Crawler.isAlreadyProcessed", link);
            return count != 0;
        }
    }

    @Override
    public void storeNewsIntoDataBase(String link, String title, String content) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.suzhaoyong.Crawler.storeNews", new News(title, content, link));
        }
    }
}
