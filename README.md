# 爬虫爬取新闻信息📰
## 获取到网页数据并打印出新闻标题
通过`IndexPage`的链接解析出页面的所有a标签中的链接并放入链接池中 <br>
不断遍历链接池中最后一个链接并放入已遍历池中（避免重复遍历）<br>
解析遍历的页面判断是否包含article标签  <br>
如果有就说明是新闻页面，打印出新闻标题 <br>

## === 数据库初始化 ===
步骤一：docker 中下载 mysql 镜像，版本号`5.7.40`  
步骤二：在命名行中进入当前项目并创建 `octopus-news` 容器，绑定 `3306` 端口，数据存储在本地  
```shell script
docker run --name octopus-news -p 3306:3306 -v `pwd`/mysql:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=qwerdfgh -d mysql:5.7.40 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
```
步骤三：初始化数据库  
查看项目中`src/main/resources/db/Database_Initialization.sql`语句并连接上数据库执行  
步骤四：gitignore 文件忽略本地数据库文件夹`mysql`  

## === 数据迁移到 ElasticSearch ===
步骤一：docker 下载并安装 elasticsearch 镜像，注意版本号，不然可能会出错。  
启动容器，在本地作为服务端  
```shell script
docker run --name elasticsearch -p 9200:9200 -p 9300:9300 -v `pwd`/elasticsearch:/usr/share/elasticsearch/data -e "discovery.type=single-node" -d elasticsearch:7.4.0
```
步骤二：在 `pom.xml` 添加客户端依赖，注意版本号  
```xml
        <!-- https://mvnrepository.com/artifact/org.elasticsearch.client/elasticsearch-rest-high-level-client -->
        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-high-level-client</artifactId>
            <version>7.4.0</version>
        </dependency>
```
步骤三：在 `ElasticsearchDao.java` 中，建立链接发送请求  
```java
public class ElasticsearchDao {
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
}
```

