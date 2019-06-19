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
