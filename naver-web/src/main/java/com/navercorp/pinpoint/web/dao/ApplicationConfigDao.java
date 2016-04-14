package com.navercorp.pinpoint.web.dao;

import java.util.List;

import com.navercorp.pinpoint.web.vo.ApplicationAuthority;

public interface ApplicationConfigDao {

    String insertAuthority(ApplicationAuthority appAuth);

    void deleteAuthority(ApplicationAuthority appAuth);

    void updateAuthority(ApplicationAuthority appAuth);

    List<ApplicationAuthority> selectAuthority(String applicationId);
}
