package com.navercorp.pinpoint.testweb.service;

import com.navercorp.pinpoint.testweb.repository.MySqlDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Service
@Transactional("mysqlTransactionManager")
public class MySqlServiceImpl implements MySqlService {

    @Autowired
    private MySqlDao mysqlDao;

    @Override
    public int selectOne() {
        return mysqlDao.selectOne();
    }

    @Override
    public int selectOneWithParam(int id) {
        return mysqlDao.selectOneWithParam(id);
    }

    @Override
    public String concat(char a, char b) {
        return mysqlDao.callConcat(a, b);
    }

    @Override
    public int swapAndGetSum(int a, int b) {
        return mysqlDao.callSwapAndGetSum(a, b);
    }

    @Override
    public void createStatement() {
        mysqlDao.createStatement();
    }
}
