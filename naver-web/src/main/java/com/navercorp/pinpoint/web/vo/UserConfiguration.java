/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class UserConfiguration {
    private final static List<ApplicationModel> EMPTY_LIST = new ArrayList<>(0);
    private String userId = "";
    private List<ApplicationModel> favoriteApplications = EMPTY_LIST;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setFavoriteApplications(List<ApplicationModel> favoriteApplications) {
        this.favoriteApplications = favoriteApplications;
    }

    public List<ApplicationModel> getFavoriteApplications() {
        return favoriteApplications;
    }

    @Override
    public String toString() {
        return "UserConfiguration{" +
            "userId='" + userId + '\'' +
            ", favoriteApplications=" + favoriteApplications +
            '}';
    }
}
