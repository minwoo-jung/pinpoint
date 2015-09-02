package com.navercorp.pinpoint.web.batch;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.web.dao.UserDao;

public class UserListener implements StepExecutionListener {

    @Autowired
    UserDao userDao;
    
    @Override
    public void beforeStep(StepExecution stepExecution) {
        userDao.dropAndCreateUserTable();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }

}
