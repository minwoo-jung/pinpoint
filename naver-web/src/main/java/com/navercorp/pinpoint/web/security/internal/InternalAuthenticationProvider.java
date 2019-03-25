/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.security.internal;

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.security.PinpointAuthentication;
import com.navercorp.pinpoint.web.service.MetaDataService;
import com.navercorp.pinpoint.web.service.SecurityService;
import com.navercorp.pinpoint.web.service.UserAccountService;
import com.navercorp.pinpoint.web.service.UserInformationService;
import com.navercorp.pinpoint.web.vo.UserAccount;
import com.navercorp.pinpoint.web.vo.exception.UserPrincipalNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author minwoo.jung
 */
public class InternalAuthenticationProvider implements AuthenticationProvider {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MetaDataService metaDataService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserInformationService userInformationService;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        final String userId = String.valueOf(auth.getPrincipal());
        final String organizationName = ((WebAuthenticationDetails)auth.getDetails()).getOrgnanizationName();

        allocatePaaSOrganizationInfoRequestScope(userId, organizationName);
        validateUser(userId);
        checkPassword(auth);
        return createPinpointAuthentication(userId);
    }

    public void allocatePaaSOrganizationInfoRequestScope(String userId, String organizationName) {
        try {
            boolean allocateSuccess = metaDataService.allocatePaaSOrganizationInfoRequestScope(userId, organizationName);

            if (allocateSuccess == false) {
                String message = String.format("error occurred in allocatePaaSOrganizationInfo userId(%s), organizationName(%s).", userId, organizationName);
                logger.error(message);
                throw new InternalAuthenticationServiceException(message);
            }
        } catch (Exception e) {
            logger.error("can't not found Organization info.", e);
            throw new InternalAuthenticationServiceException("can't not found Organization.", e);
        }
    }

    public PinpointAuthentication createPinpointAuthentication(String userId) {
        try {
            PinpointAuthentication authentication = securityService.createPinpointAuthentication(userId);

            if (authentication.getPrincipal().isEmpty() == false) {
                authentication.addAuthority(new SimpleGrantedAuthority("ROLE_USER"));
            }

            return authentication;
        } catch (Exception e) {
            logger.error("can't not authenticate.", e);
            throw new InternalAuthenticationServiceException("can't not authenticate.",e);
        }
    }

    private void checkPassword(Authentication auth) throws AuthenticationException {
        String password = (String) auth.getCredentials();
        UserAccount userAccount = userAccountService.selectUserAccount((String) auth.getPrincipal());
        if (passwordEncoder.matches(password, userAccount.getPassword()) == false) {
            String message = String.format("Password is invalid for userId(%s).", auth.getPrincipal());
            logger.error(message);
            throw new BadCredentialsException(message);
        }
    }

    private void validateUser(String userId) {
        //TODO : (minwoo) 특수문자등에 대한 validation 체크. 사용자가 존재하는지
        if (StringUtils.isEmpty(userId)) {
            String message = String.format("UserId(%s) is invalid.", userId);
            logger.error(message);
            throw new BadCredentialsException(message);
        }

        if (userInformationService.isExistUserId(userId) == false) {
            String message = String.format("UserId(%s) isn't exist.", userId);
            logger.error(message);
            throw new UserPrincipalNotFoundException(message);
        }
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }
}
