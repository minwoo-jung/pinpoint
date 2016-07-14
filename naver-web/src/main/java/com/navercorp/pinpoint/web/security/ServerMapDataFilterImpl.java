/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.web.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.Link;
import com.navercorp.pinpoint.web.applicationmap.Node;
import com.navercorp.pinpoint.web.applicationmap.ServerInstance;
import com.navercorp.pinpoint.web.applicationmap.ServerInstanceList;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.service.ApplicationConfigService;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth;
import com.navercorp.pinpoint.web.vo.Application;

/**
 * @author minwoo.jung
 */
public class ServerMapDataFilterImpl extends AppConfigOrganizer implements ServerMapDataFilter {

    private final static String UNAUTHORIZED_AGENT = "UNAUTHORIZED_AGENT";
    private final static ServerInstanceList unAuthServerInstanceList;
    static {
        unAuthServerInstanceList = new ServerInstanceList();
        Map<String, List<ServerInstance>> serverInstanceList = unAuthServerInstanceList.getServerInstanceList();
        List<ServerInstance> serverInstances = new ArrayList<>();
        serverInstances.add(new ServerInstance(UNAUTHORIZED_AGENT, UNAUTHORIZED_AGENT, ServiceType.UNAUTHORIZED.getCode()));
        serverInstanceList.put("UNKNOWN_AGENT", serverInstances);
    }
    
    @Autowired
    ApplicationConfigService applicationConfigService;
    
    @Override
    public boolean filter(Application application) {
        return isAuthorized(application) ? false : true;
    }
    
    private boolean isAuthorized(Application application) {
        PinpointAuthentication authentication = (PinpointAuthentication)SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) { 
            return false;
        }
        if (isPinpointManager(authentication)) {
            return true;
        }
        
        String applicationId = application.getName();
        List<AppUserGroupAuth> userGroupAuths = userGroupAuth(authentication, applicationId);
        
        if (userGroupAuths.size() == 0) {
            return true;
        }
        for(AppUserGroupAuth auth : userGroupAuths) {
            if (auth.getAppAuthConfiguration().getServerMapData() == false) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public ApplicationMap dataFiltering(ApplicationMap map) {
        Collection<Node> nodes = map.getNodes();
//        for(Node node : nodes) {
//            nodeDataFiltering(node);
//        }
//
//        Collection<Link> links = map.getLinks();
//        for(Link link : links) {
//            linkDataFiltering(link);
//        }
        return map;
    }

    private void nodeDataFiltering(Node node) {
        final boolean authorized = isAuthorized(node.getApplication());
        node.setAuthorized(authorized);
        
        if (authorized == false) {
            Application unAuthApp = new Application(node.getApplication().getName(), ServiceType.UNAUTHORIZED);
            node.setApplication(unAuthApp);
            
            NodeHistogram nodeHistogram = node.getNodeHistogram();
            node.setNodeHistogram(new NodeHistogram(unAuthApp, nodeHistogram.getRange()));
        }
    }


    private void linkDataFiltering(Link link) {
      final boolean isAuthFromApp = isAuthorized(link.getFrom().getApplication());
      final boolean isAuthToApp = isAuthorized(link.getTo().getApplication());
      
      if (isAuthFromApp == false) {
          Node from = link.getFrom();
          from.setAuthorized(isAuthFromApp);
          
          Application unAuthApp = new Application(from.getApplication().getName(), ServiceType.UNAUTHORIZED);
          from.setApplication(unAuthApp);
          
          from.setServerInstanceList(unAuthServerInstanceList);
          
      }
      
      if (isAuthToApp == false) {
          Node to = link.getTo();
          to.setAuthorized(isAuthFromApp);
          
          Application unAuthApp = new Application(to.getApplication().getName(), ServiceType.UNAUTHORIZED);
          to.setApplication(unAuthApp);
          
          to.setServerInstanceList(unAuthServerInstanceList);
      }
    }


}
