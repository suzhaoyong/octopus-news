package com.github.suzhaoyong;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MockData {
    private static final Instant SQL_DEFAULT_TIME = Instant.parse("1970-01-01T00:00:01.00Z");
    private static final Random RANDOM = new Random();
    public static void main(String[] args) {
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            maxMockData(sqlSessionFactory, 200_000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void maxMockData(SqlSessionFactory sqlSessionFactory, int max) {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            List<News> newsList = session.selectList("com.github.suzhaoyong.MockData.selectNews");
            System.out.println(newsList.size());
            int count = max - newsList.size();
            try {
                while (count-- > 0) {
                    int index = RANDOM.nextInt(newsList.size());
                    News newsToBeInserted = new News(newsList.get(index));

                    Instant currentTime = newsToBeInserted.getCreatedAt();
                    currentTime = currentTime.minusSeconds(RANDOM.nextInt(3600 * 24 * 365));
                    newsToBeInserted.setCreatedAt(currentTime);
                    newsToBeInserted.setModifiedAt(currentTime);

                    session.insert("com.github.suzhaoyong.MockData.insertNews", newsToBeInserted);
                    System.out.println("Left: " + count);
                    if (count % 2000 == 0) {
                        session.flushStatements();
                    }
                }
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException(e);
            }
        }
    }
}
