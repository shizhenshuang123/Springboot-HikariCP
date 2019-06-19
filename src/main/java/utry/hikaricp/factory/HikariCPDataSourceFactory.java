package utry.hikaricp.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import utry.hikaricp.DataSourceProvider;
import utry.hikaricp.info.DataSourceInfo;
import utry.hikaricp.source.MyHikariDataSource;

/**
 * @author szs
 * @date 2019/3/14 14:24
 */
@Component
public class HikariCPDataSourceFactory{

    @Autowired
    private MyHikariDataSource dataSource;

    public void reload(String username, String password, String url) {
        //加载数据源
        DataSourceInfo info = new DataSourceInfo();
        info.setUrl(url);
        info.setUsername(username);
        info.setPassword(password);

        dataSource.updateDataSourceMap("1", DataSourceProvider.create(info));
    }
}
