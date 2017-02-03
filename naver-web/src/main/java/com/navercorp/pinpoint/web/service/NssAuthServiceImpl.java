package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.dao.NssAuthDao;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;

/**
 * @author HyunGil Jeong
 */
@Service
public class NssAuthServiceImpl implements NssAuthService {

    private final NssAuthDao nssAuthDao;

    @Autowired
    public NssAuthServiceImpl(@Qualifier("nssAuthDaoFactory") NssAuthDao nssAuthDao) {
        Assert.notNull(nssAuthDao, "nssAuthDao must not be null");
        this.nssAuthDao = nssAuthDao;
    }

    @Override
    public Collection<String> getAuthorizedPrefixes() {
        Collection<String> authorizedPrefixes = nssAuthDao.selectAuthorizedPrefix();
        if (CollectionUtils.isEmpty(authorizedPrefixes)) {
            return Collections.emptyList();
        } else {
            return authorizedPrefixes;
        }
    }

    @Override
    public int addAuthorizedPrefix(String authorizedPrefix) {
        return nssAuthDao.insertAuthorizedPrefix(authorizedPrefix);
    }

    @Override
    public int removeAuthorizedPrefix(String authorizedPrefix) {
        return nssAuthDao.deleteAuthorizedPrefix(authorizedPrefix);
    }

    @Override
    public Collection<String> getOverrideUserIds() {
        Collection<String> overrideUserIds = nssAuthDao.selectOverrideUserId();
        if (CollectionUtils.isEmpty(overrideUserIds)) {
            return Collections.emptyList();
        } else {
            return overrideUserIds;
        }
    }

    @Override
    public int addOverrideUserId(String userId) {
        return nssAuthDao.insertOverrideUserId(userId);
    }

    @Override
    public int removeOverrideUserId(String userId) {
        return nssAuthDao.deleteOverrideUserId(userId);
    }
}
