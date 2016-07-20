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
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapBuilder;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapWithScatterData;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapWithScatterScanResult;
import com.navercorp.pinpoint.web.applicationmap.DefaultApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.Link;
import com.navercorp.pinpoint.web.applicationmap.LinkList;
import com.navercorp.pinpoint.web.applicationmap.Node;
import com.navercorp.pinpoint.web.applicationmap.NodeList;
import com.navercorp.pinpoint.web.applicationmap.ServerInstance;
import com.navercorp.pinpoint.web.applicationmap.ServerInstanceList;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.navercorp.pinpoint.web.exception.AuthorityException;
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
        
        if(isEmptyUserGroup(authentication, applicationId)) {
            return true;
        }
        List<AppUserGroupAuth> userGroupAuths = userGroupAuth(authentication, applicationId);
        for(AppUserGroupAuth auth : userGroupAuths) {
            if (auth.getConfiguration().getServerMapData() == false) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public ApplicationMap dataFiltering(final ApplicationMap map) {
        PinpointAuthentication authentication = (PinpointAuthentication)SecurityContextHolder.getContext().getAuthentication();
        if (isPinpointManager(authentication)) {
            return map;
        }
        
        return createApplicationMap(map);
    }

    private ApplicationMap createApplicationMap(final ApplicationMap map) {
        if (map instanceof DefaultApplicationMap) {
            return createDefaultApplicationMap((DefaultApplicationMap)map);
        } else if (map instanceof ApplicationMapWithScatterData) {
            ApplicationMap appMap = createApplicationMap(((ApplicationMapWithScatterData)map).getApplicationMap());
            return new ApplicationMapWithScatterData(appMap, ((ApplicationMapWithScatterData)map).getApplicationScatterDataMap());
        } else if (map instanceof ApplicationMapWithScatterScanResult) {
            ApplicationMap appMap = createApplicationMap(((ApplicationMapWithScatterScanResult)map).getApplicationMap());
            return new ApplicationMapWithScatterScanResult(appMap, ((ApplicationMapWithScatterScanResult)map).getApplicationScatterScanResultList());
        } else {
            throw new AuthorityException("Can't create ApplicationMap class while filtering data");
        }
    }
    
    private ApplicationMap createDefaultApplicationMap(final DefaultApplicationMap map) {
        Collection<Node> nodes = map.getNodes();
        NodeList nodeList = new NodeList();
        for(Node node : nodes) {
            nodeList.addNode(nodeDataFiltering(node));
        }
        
        LinkList linkList = new LinkList();
        Collection<Link> links = map.getLinks();
        for(Link link : links) {
            linkList.addLink(linkDataFiltering(link, nodeList));
        }
        
        ApplicationMapBuilder builder = new ApplicationMapBuilder(((DefaultApplicationMap)map).getRange());
        return builder.build(nodeList, linkList);
    }

    private Node nodeDataFiltering(Node node) {
        final boolean authorized = isAuthorized(node.getApplication());
        if (authorized) {
            return node;
        }
        return createUnauthorizedNode(node);
    }
    
    private Node createUnauthorizedNode(Node node) {
        Application unAuthApp = new Application(node.getApplication().getName(), ServiceType.UNAUTHORIZED);
        Node newNode = new Node(unAuthApp);
        newNode.setAuthorized(false);
        newNode.setServerInstanceList(unAuthServerInstanceList);
        newNode.setNodeHistogram(new NodeHistogram(unAuthApp, node.getNodeHistogram().getRange()));
        return newNode;
    }

    private Link linkDataFiltering(Link link, NodeList nodeList) {
      final boolean isAuthFromApp = isAuthorized(link.getFrom().getApplication());
      final boolean isAuthToApp = isAuthorized(link.getTo().getApplication());
      
      if (isAuthFromApp && isAuthToApp) {
          return link;
      }
      
      Node from = link.getFrom();
      Node to = link.getTo();
      
      if (isAuthFromApp == false) {
          from = createOrFindUnauthorizedNode(from, nodeList);
      }
      if (isAuthToApp == false) {
          to = createOrFindUnauthorizedNode(to, nodeList);
      }
      
      Link newLink = new Link(link.getCreateType(), from, to, link.getRange());

      if (isAuthFromApp == false || isAuthToApp == false) {
          LinkCallDataMap newSourceLinkCallDataMap = createLinkCallDataMap(link.getSourceLinkCallDataMap(), isAuthFromApp, isAuthToApp);
          newLink.addSource(newSourceLinkCallDataMap);
          LinkCallDataMap newTargetLinkCallDataMap = createLinkCallDataMap(link.getTargetLinkCallDataMap(), isAuthFromApp, isAuthToApp);
          newLink.addTarget(newTargetLinkCallDataMap);
      }
      
      return newLink;
    }

    private Node createOrFindUnauthorizedNode(Node node, NodeList nodeList) {
        Application unAuthApp = new Application(node.getApplication().getName(), ServiceType.UNAUTHORIZED);
        Node newNode = nodeList.findNode(unAuthApp);
        if (newNode == null) {
            return createUnauthorizedNode(node);
        } else {
            return newNode;
        }
    }

    private LinkCallDataMap createLinkCallDataMap(LinkCallDataMap linkCallDataMap, final boolean isAuthFromApp, final boolean isAuthToApp) {
        Collection<LinkCallData> sourceLinkDataList = linkCallDataMap.getLinkDataList();
        LinkCallDataMap newLinkCallDataMap = new LinkCallDataMap(linkCallDataMap.getTimeWindow());

        for (LinkCallData linkCallData : sourceLinkDataList) {
            String fromId;
            ServiceType fromServiceType;
            if (isAuthFromApp) {
                fromId = linkCallData.getSource();
                fromServiceType = linkCallData.getSourceServiceType();
            } else {
                fromId = UNAUTHORIZED_AGENT;
                fromServiceType = ServiceType.UNAUTHORIZED;
            }
            
            String toId;
            ServiceType toServiceType;
            if (isAuthToApp) {
                toId = linkCallData.getTarget();
                toServiceType = linkCallData.getTargetServiceType();
            } else {  
                toId = UNAUTHORIZED_AGENT;
                toServiceType = ServiceType.UNAUTHORIZED;
            }
            
            newLinkCallDataMap.addCallData(fromId, fromServiceType, toId, toServiceType, linkCallData.getTimeHistogram());
        }
        
        return newLinkCallDataMap;
    }
}
