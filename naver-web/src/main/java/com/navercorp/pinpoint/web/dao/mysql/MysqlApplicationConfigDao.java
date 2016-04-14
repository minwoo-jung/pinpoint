package com.navercorp.pinpoint.web.dao.mysql;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.navercorp.pinpoint.web.dao.ApplicationConfigDao;
import com.navercorp.pinpoint.web.vo.ApplicationAuthority;

@Repository
public class MysqlApplicationConfigDao implements ApplicationConfigDao {
    private static final String NAMESPACE = ApplicationConfigDao.class.getPackage().getName() + "." + ApplicationConfigDao.class.getSimpleName() + ".";

    @Autowired
    @Qualifier("sqlSessionTemplate")
    private SqlSessionTemplate sqlSessionTemplate;

    
    @Override
    public String insertAuthority(ApplicationAuthority appAuth) {
        sqlSessionTemplate.insert(NAMESPACE + "insertAuthority", appAuth);
        return appAuth.getNumber();
    }


    @Override
    public void deleteAuthority(ApplicationAuthority appAuth) {
        sqlSessionTemplate.delete(NAMESPACE + "deleteAuthority", appAuth);
    }


    @Override
    public void updateAuthority(ApplicationAuthority appAuth) {
        sqlSessionTemplate.update(NAMESPACE + "updateAuthority", appAuth);
    }


    @Override
    public List<ApplicationAuthority> selectAuthority(String applicationId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectAuthorityList", applicationId);
    }
    
}
