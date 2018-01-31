/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.batch;

import com.navercorp.pinpoint.web.service.UserConfigService;
import com.navercorp.pinpoint.web.service.UserGroupService;
import com.navercorp.pinpoint.web.vo.UserConfiguration;
import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.UserGroupMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class UpdateUserIdTasklet implements Tasklet {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    UserConfigService userConfigService;

    @Autowired
    UserGroupService userGroupService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("###############startUpdate userId###############");
        List<UserInfo> userInfoList = createUserInfoList();
        int number = 0;

        logger.info("###############startUpdate user_configuration###############");
        for (UserInfo userInfo : userInfoList) {
            try {
                logger.info(++number + " : ========== " + userInfo + " ===========");
                UserConfiguration userConfiguration = userConfigService.selectUserConfiguration(userInfo.getUserId());
                if (userInfo.getUserId().equals(userConfiguration.getUserId())) {
                    UserConfiguration newUserConfiguration = new UserConfiguration();
                    newUserConfiguration.setUserId(userInfo.getChangedId());
                    newUserConfiguration.setFavoriteApplications(userConfiguration.getFavoriteApplications());
                    //insert
                    userConfigService.insertUserConfiguration(newUserConfiguration);
                    //delete
                    userConfigService.deleteUserConfiguration(userInfo.getUserId());
                    logger.info(number + " : ========== " + "find and insert/delete(before) : " + userConfiguration);
                    logger.info(number + " : ========== " + "find and insert/delete(after ) : " + newUserConfiguration);
                } else {
//                    logger.info(number + " : ========== " + "can't find in user_configuration table : " + userInfo );
                }
                logger.info("");
            } catch (Exception e) {
                logger.error("Exception while update user configuration : " + userInfo, e);
                throw e;
            }
        }

        logger.info("###############startUpdate user_group_member###############");
        number = 0;
        for(UserInfo userInfo : userInfoList) {
            List<UserGroup> userGroups = userGroupService.selectUserGroupByUserId(userInfo.getUserId());
            logger.info(++number + " : ========== " + userInfo + " ===========");
            for (UserGroup userGroup : userGroups) {
                try {
                    UserGroupMember userGroupMember = new UserGroupMember(userGroup.getId(), userInfo.getUserId());
                    UserGroupMember newUserGroupMember = new UserGroupMember(userGroup.getId(), userInfo.getChangedId());
                    //insert
                    userGroupService.insertMember(newUserGroupMember);
                    //delete
                    userGroupService.deleteMember(userGroupMember);
                    logger.info(number + " : ========== " + "find and insert/delete(before) : " + userGroupMember);
                    logger.info(number + " : ========== " + "find and insert/delete(after ) : " + newUserGroupMember);
                } catch (Exception e) {
                    logger.error("Exception while update user group member : " + userInfo, e);
                    throw e;
                }
            }
            logger.info("");
        }

        return RepeatStatus.FINISHED;
    }

    public static class UserInfo {
        private String userId;
        private String changedId;
        private String name;

        public UserInfo(String userId, String changedId, String name) {
            this.userId = userId;
            this.changedId = changedId;
            this.name = name;
        }

        public String getChangedId() {
            return changedId;
        }

        public void setChangedId(String changedId) {
            this.changedId = changedId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        @Override
        public String toString() {
            return "UserInfo{" +
                "userId='" + userId + '\'' +
                ", changedId='" + changedId + '\'' +
                ", name='" + name + '\'' +
                '}';
        }
    }

    private List<UserInfo> createUserInfoList() {
        List<UserInfo> userInfoList = new ArrayList<>();
        return userInfoList;
    }

}
