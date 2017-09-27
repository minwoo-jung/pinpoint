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

package com.navercorp.pinpoint.web.dao.rest;

import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.web.dao.AgentDownloadInfoDao;
import com.navercorp.pinpoint.web.vo.AgentDownloadInfo;
import com.navercorp.pinpoint.web.vo.NaverAgentDownloadInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Taejin Koo
 */
@Repository
public class NaverAgentDownloadInfoDao implements AgentDownloadInfoDao {

    @Value("#{pinpointWebProps['agent.repository.url']}")
    private String repositoryUrl;

    @Value("#{pinpointWebProps['agent.repository.restapi.url']}")
    private String repositoryRestApiUrl;

    private static String FILE_PATH = "/%s/pinpoint-agent-%s.tar.gz";

    @Override
    public List<AgentDownloadInfo> getDownloadInfoList() {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> responseBody = restTemplate.getForObject(repositoryRestApiUrl, Map.class);

        List<String> versionList = getVersionList(responseBody.get("children"));

        List<AgentDownloadInfo> agentDownloadInfoList = new ArrayList<>(versionList.size());
        for (String version : versionList) {
            NaverAgentDownloadInfo naverAgentDownloadInfo = new NaverAgentDownloadInfo(version, String.format(repositoryUrl + FILE_PATH, version, version));
            agentDownloadInfoList.add(naverAgentDownloadInfo);
        }

        return agentDownloadInfoList;
    }

    private List<String> getVersionList(Object jsonNode) {
        Pattern stableVersionPattern = Pattern.compile(IdValidateUtils.STABLE_VERSION_PATTERN_VALUE);

        if (jsonNode instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object eachVersionUri : (List) jsonNode) {
                if (eachVersionUri instanceof Map) {
                    Object path = ((Map) eachVersionUri).get("uri");
                    if (path == null) {
                        continue;
                    }

                    String pathString = String.valueOf(path);
                    if (StringUtils.isEmpty(pathString) || !pathString.startsWith("/")) {
                        continue;
                    }

                    Object isExist = ((Map) eachVersionUri).get("folder");
                    if (!(isExist instanceof Boolean) || ((Boolean) isExist).booleanValue() == false) {
                        continue;
                    }

                    String version = pathString.replace("/", "");
                    if (!stableVersionPattern.matcher(version).matches()) {
                        continue;
                    }

                    result.add(version);
                }
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

}
