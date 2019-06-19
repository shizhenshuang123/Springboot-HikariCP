package utry.hikaricp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author szs
 * @date 2019/3/14 13:29
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@MapperScan("utry.hikaricp.mapper")
public class APP {
    public static void main(String[] args) {
        SpringApplication.run(APP.class, args);
    }
}
