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
package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.security.PinpointAuthentication;
import com.navercorp.pinpoint.web.vo.User;
import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.role.RoleInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author minwoo.jung
 */
@Service
@Transactional(rollbackFor = {Exception.class})
public class SecurityServiceImpl implements SecurityService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private ApplicationConfigService configService;

    @Autowired
    private RoleService roleService;

    @Override
    @Transactional(readOnly = true, rollbackFor = {Exception.class})
    public PinpointAuthentication createPinpointAuthentication(String userId) {
        User user = userService.selectUserByUserId(userId);

        if (user != null) {
            final List<UserGroup> userGroups = userGroupService.selectUserGroupByUserId(userId);
            final boolean pinpointManager = configService.isManager(userId);
            final RoleInformation roleInformation = roleService.getUserPermission(userId);
            return new PinpointAuthentication(user.getUserId(), user.getName(), userGroups, true, pinpointManager, roleInformation);
        } else {
            return new PinpointAuthentication();
        }
    }
}
