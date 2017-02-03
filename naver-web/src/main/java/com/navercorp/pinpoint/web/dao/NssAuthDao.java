package com.navercorp.pinpoint.web.dao;

import java.util.Collection;

/**
 * @author HyunGil Jeong
 */
public interface NssAuthDao {

    Collection<String> selectAuthorizedPrefix();

    int insertAuthorizedPrefix(String authorizedPrefix);

    int deleteAuthorizedPrefix(String authorizedPrefix);

    Collection<String> selectOverrideUserId();

    int insertOverrideUserId(String userId);

    int deleteOverrideUserId(String userId);

}
