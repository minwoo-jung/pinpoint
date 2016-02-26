package com.navercorp.pinpoint.testweb.service;

import com.navercorp.pinpoint.testweb.repository.OracleDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Service
@Transactional("oracleTransactionManager")
public class OracleServiceImpl implements OracleService {
    @Autowired
    private OracleDao oracleDao;

    @Override
    public int selectOne() {
        return oracleDao.selectOne();
    }

    @Override
    public int selectOneWithParam(int id) {
        return oracleDao.selectOneWithParam(id);
    }

    @Override
    public String concat(char a, char b) {
        return oracleDao.callConcat(a, b);
    }

    @Override
    public int swapAndGetSum(int a, int b) {
        return oracleDao.callSwapAndGetSum(a, b);
    }

    @Override
    public void createStatement() {
        oracleDao.createStatement();
    }
}
