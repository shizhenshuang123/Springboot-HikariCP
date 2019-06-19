
#Springboot+HikariCP实现动态数据源
---
开发环境：
``jdk``：JDK1.8+
``gradle``：Gradle4.6+
``Spring``：2.1.3.RELEASE+
---
当数据库数据达到一定数量的时候，数据库的响应将会有所缓慢，一般都会采取一些措施。例如，读写分离、分表分库、主从服务、缓存技术等等。这里采用的是多库多表。

test库中user_info表：

![image.png](https://upload-images.jianshu.io/upload_images/15706831-fb881969f645c8a2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
---
test2库中user_info表：

![image.png](https://upload-images.jianshu.io/upload_images/15706831-4720918c68a71439.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
---

#####导入依赖

```
// https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-web
    compile group: 'org.springframework.boot', name: 'spring-boot-starter-web', version: '2.1.3.RELEASE'
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-jdbc
    compile group: 'org.springframework.boot', name: 'spring-boot-starter-jdbc', version: '2.1.3.RELEASE'
    // https://mvnrepository.com/artifact/org.projectlombok/lombok
    compileOnly group: 'org.projectlombok', name: 'lombok', version: '1.18.6'
    // https://mvnrepository.com/artifact/mysql/mysql-connector-java
    compile group: 'mysql', name: 'mysql-connector-java', version: '8.0.15'
    //mybatis插件
    compile group: 'org.mybatis.generator', name: 'mybatis-generator-core', version: '1.3.5'
    compile group: 'tk.mybatis', name: 'mapper', version: '4.0.2'
    // https://mvnrepository.com/artifact/org.mybatis.spring.boot/mybatis-spring-boot-starter
    compile group: 'org.mybatis.spring.boot', name: 'mybatis-spring-boot-starter', version: '2.0.0'
```

HikariCP在spring-boot-starter-jdbc中已经被引入（Spring默认数据源）

![image.png](https://upload-images.jianshu.io/upload_images/15706831-da0d7d18daa6aa43.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#####项目结构

![image.png](https://upload-images.jianshu.io/upload_images/15706831-b0a24e43dd8e49ca.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#####配置文件配置（application.yaml）默认数据源信息

```
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=Asia/Shanghai&allowMultiQueries=true
    username: root
    password: admin
    sql-script-encoding: UTF-8
```

#####读取配置信息DataSourceConfig.java，创建默认的数据源

```
package utry.hikaricp.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import utry.hikaricp.DataSourceProvider;
import utry.hikaricp.info.DataSourceInfo;
import utry.hikaricp.source.MyHikariDataSource;

/**
 * @author szs
 * @date 2019/3/14 14:22
 */
@Configuration
@EnableTransactionManagement
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String userName;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    @ConditionalOnMissingBean(MyHikariDataSource.class)
    public MyHikariDataSource dataSource() {
        DataSourceInfo info = new DataSourceInfo();
        info.setUrl(url);
        info.setUsername(userName);
        info.setPassword(password);
        HikariDataSource dataSource = DataSourceProvider.create(info);
        MyHikariDataSource hikariCPDataSource = new MyHikariDataSource();
        hikariCPDataSource.updateDataSourceMap("1", dataSource);
        return hikariCPDataSource;
    }
}
```

#####通过DataSourceProvider创建HikariDataSource

```
package utry.hikaricp;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import utry.hikaricp.info.DataSourceInfo;

/**
 * @author szs
 * @date 2019/3/14 15:07
 */
public class DataSourceProvider {
    public static HikariDataSource create(DataSourceInfo sourceInfo) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setUsername(sourceInfo.getUsername());
        hikariConfig.setPassword(sourceInfo.getPassword());
        hikariConfig.setJdbcUrl(sourceInfo.getUrl());
        hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
        return new HikariDataSource(hikariConfig);
    }
}
```

#####最核心的数据源的类MyHikariDataSource.java

```
package utry.hikaricp.source;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author szs
 * @date 2019/3/14 14:30
 */
public class MyHikariDataSource extends AbstractDataSource {

    private static Map<String, HikariDataSource> dataSourceMap = new HashMap<>(1);

    @Override
    public Connection getConnection() throws SQLException {
        return dataSourceMap.get("1").getConnection();
    }


    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return dataSourceMap.get("1").getConnection(username, password);
    }

    private void destroy(){
        DataSource dataSource = dataSourceMap.get("1");
        Closeable closeable = (Closeable) dataSource;
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateDataSourceMap(String key, HikariDataSource value) {
        destroy();
        dataSourceMap.put(key, value);
    }
}
```

只要将上面这个类中的DataSourceMap集合中的HikariDataSource修改就能实现数据源的切换了，调用updateDataSourceMap()方法修改即可（修改之前，先关闭之前的数据源，这里写死的map中key=1，读者可以实现动态设置）

---
#####实体类UserInfo.java

```
package utry.hikaricp.model;

public class UserInfo {
    private Integer id;

    private String remarks;

    public UserInfo() {
    }

    public UserInfo(Integer id, String remarks) {
        this.id = id;
        this.remarks = remarks;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
```

######HikariCpController.java两个接口：
1. 更新数据源
2. 查询用户信息

```
package utry.hikaricp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utry.hikaricp.factory.HikariCPDataSourceFactory;
import utry.hikaricp.mapper.UserInfoMapper;
import utry.hikaricp.model.UserInfo;

/**
 * @author szs
 * @date 2019/3/18 11:50
 */
@RestController
public class HikariCpController {

    @Autowired
    private UserInfoMapper mapper;

    @Autowired
    private HikariCPDataSourceFactory factory;

    @RequestMapping("update")
    public void info(String username, String password, String url) {
        factory.reload(username, password, url);
    }

    @RequestMapping("get")
    public UserInfo get() {
        try {
            UserInfo userInfo = mapper.selectByPrimaryKey(1);
            return userInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
```

#####启动项目

![image.png](https://upload-images.jianshu.io/upload_images/15706831-a5c9b65083db9a47.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

1. 调用get接口，返回的数据是test库中的

![image.png](https://upload-images.jianshu.io/upload_images/15706831-0103c6d0e9bb187e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


2. 调用update请求更新数据源

![image.png](https://upload-images.jianshu.io/upload_images/15706831-bcf7469f2c701161.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

调用成功之后，可以看到，之前的数据源已经被shutdown，初始化了一个新的数据源

![image.png](https://upload-images.jianshu.io/upload_images/15706831-34c2b5478eea6cb4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

3. 再次调用get请求，可以看到获取的test2数据库中的数据

![image.png](https://upload-images.jianshu.io/upload_images/15706831-3236f3d38d956cb9.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
