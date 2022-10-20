# 爬虫爬取新闻信息📰
## 获取到网页数据并打印出新闻标题
通过`IndexPage`的链接解析出页面的所有a标签中的链接并放入链接池中 <br>
不断遍历链接池中最后一个链接并放入已遍历池中（避免重复遍历）<br>
解析遍历的页面判断是否包含article标签  <br>
如果有就说明是新闻页面，打印出新闻标题 <br>

## === 数据库初始化 ===
步骤一：docker 中下载 mysql 镜像，版本号`5.7.40`  
步骤二：在命名行中进入当前项目并创建 `octopus-news` 容器，绑定 `3306` 端口，数据存储在本地  
``` shell script
docker run --name octopus-news -p 3306:3306 -v `pwd`/mysql:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=qwerdfgh -d mysql:5.7.40`
```
步骤三：初始化数据库  
查看项目中`src/main/resources/db/Database_Initialization.sql`语句并连接上数据库执行  
步骤四：gitignore 文件忽略本地数据库文件夹`mysql`  


