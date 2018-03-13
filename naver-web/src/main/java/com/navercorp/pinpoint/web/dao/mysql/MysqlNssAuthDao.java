package com.navercorp.pinpoint.web.dao.mysql;

import com.navercorp.pinpoint.web.dao.NssAuthDao;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * @author HyunGil Jeong
 */
@Repository
public class MysqlNssAuthDao implements NssAuthDao {

    private static final String NAMESPACE = NssAuthDao.class.getPackage().getName() + "." + NssAuthDao.class.getSimpleName() + ".";

    @Autowired
    @Qualifier("metaDataSqlSessionTemplate")
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public Collection<String> selectAuthorizedPrefix() {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectAuthorizedPrefix");
    }

    @Override
    public int insertAuthorizedPrefix(String authorizedPrefix) {
        return sqlSessionTemplate.insert(NAMESPACE + "insertAuthorizedPrefix", authorizedPrefix);
    }

    @Override
    public int deleteAuthorizedPrefix(String authorizedPrefix) {
        return sqlSessionTemplate.delete(NAMESPACE + "deleteAuthorizedPrefix", authorizedPrefix);
    }

    @Override
    public Collection<String> selectOverrideUserId() {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectOverrideUserId");
    }

    @Override
    public int insertOverrideUserId(String userId) {
        return sqlSessionTemplate.insert(NAMESPACE + "insertOverrideUserId", userId);
    }

    @Override
    public int deleteOverrideUserId(String userId) {
        return sqlSessionTemplate.delete(NAMESPACE + "deleteOverrideUserId", userId);
    }
}
