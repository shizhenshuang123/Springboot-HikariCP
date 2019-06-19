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
