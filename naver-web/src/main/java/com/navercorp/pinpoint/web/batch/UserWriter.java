package com.navercorp.pinpoint.web.batch;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.web.dao.UserDao;
import com.navercorp.pinpoint.web.vo.User;

public class UserWriter implements ItemWriter<User> {
	
    @Autowired
    UserDao userDao;
	
	@Override
	public void write(List<? extends User> users) throws Exception {
	    for(User user : users) {
	        user.removeHyphenForPhoneNumber();
	    }
	    
		userDao.inserUserList((List<User>) users);
	}
}
