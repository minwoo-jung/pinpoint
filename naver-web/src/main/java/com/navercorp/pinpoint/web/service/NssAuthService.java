package com.navercorp.pinpoint.web.service;

import java.util.Collection;

/**
 * @author HyunGil Jeong
 */
public interface NssAuthService {

    Collection<String> getAuthorizedPrefixes();

    int addAuthorizedPrefix(String authorizedPrefix);

    int removeAuthorizedPrefix(String authorizedPrefix);

    Collection<String> getOverrideUserIds();

    int addOverrideUserId(String userId);

    int removeOverrideUserId(String userId);

}
