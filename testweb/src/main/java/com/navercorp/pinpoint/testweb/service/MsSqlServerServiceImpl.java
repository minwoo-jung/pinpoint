package com.navercorp.pinpoint.testweb.service;

import com.navercorp.pinpoint.testweb.repository.MsSqlServerDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Service
@Transactional("msSqlServerTransactionManager")
public class MsSqlServerServiceImpl implements MsSqlServerService {

    @Autowired
    private MsSqlServerDao msSqlServerDao;

    @Override
    public int selectOne() {
        return msSqlServerDao.selectOne();
    }

    @Override
    public int selectOneWithParam(int id) {
        return msSqlServerDao.selectOneWithParam(id);
    }
    
    @Override
    public void createStatement() {
        msSqlServerDao.createStatement();
    }
}
