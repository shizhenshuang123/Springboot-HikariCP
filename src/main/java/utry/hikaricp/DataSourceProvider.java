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
