package com.github.suzhaoyong;

import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElasticsearchDao {
    public static void main(String[] args) {
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

            List<News> newsList = getNewsFromMysql(sqlSessionFactory);

            for (int i = 0; i < 10; i++) {
                new Thread(() -> writeSingleThread(newsList)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeSingleThread(List<News> newsList) {
        try {
            //High Level Client init
            try (RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost("localhost", 9200, "http")))
            ) {
                for (News news : newsList) {
                    IndexRequest request = new IndexRequest("news");
                    Map<String, Object> param = new HashMap<>();
                    param.put("title", news.getTitle());
                    param.put("content", news.getContent().length() > 10 ? news.getContent().substring(0, 10) : news.getContent());
                    param.put("url", news.getUrl());
                    param.put("createAt", news.getContent());
                    param.put("modifiedAt", news.getModifiedAt());

                    request.source(param, XContentType.JSON);
                    IndexResponse response = client.index(request, RequestOptions.DEFAULT);
                    System.out.println(
                            response.status().getStatus()
                    );
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<News> getNewsFromMysql(SqlSessionFactory sqlSessionFactory) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectList("com.github.suzhaoyong.MockData.selectNews");
        }
    }
}
