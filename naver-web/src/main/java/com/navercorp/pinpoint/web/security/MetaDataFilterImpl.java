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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.web.calltree.span.CallTreeNode;
import com.navercorp.pinpoint.web.calltree.span.SpanAlign;
import com.navercorp.pinpoint.web.service.ApplicationConfigService;
import com.navercorp.pinpoint.web.vo.AppAuthConfiguration;
import com.navercorp.pinpoint.web.vo.ApplicationConfiguration;
import com.navercorp.pinpoint.web.vo.callstacks.Record;
import com.navercorp.pinpoint.web.vo.callstacks.RecordFactory;

/**
 * @author minwoo.jung
 */
public class MetaDataFilterImpl implements MetaDataFilter {

    @Autowired
    ApplicationConfigService applicationConfigService;
    
    @Override
    public boolean filter(SpanAlign spanAlign, MetaData metaData) {
        return isAuthorized(spanAlign, metaData) ? false : true;
    }

    private boolean isAuthorized(SpanAlign spanAlign, MetaData metaData) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) { 
            return false;
        }
        
        PinpointAuthentication authentication = (PinpointAuthentication)SecurityContextHolder.getContext().getAuthentication();

        if (authentication.isPinpointManager()) {
            return true;
        }
        
        String applicationId = spanAlign.getApplicationId();
        ApplicationConfiguration appConfig = authentication.getApplicationConfiguration(applicationId);
        
        if (appConfig == null) {
            appConfig = applicationConfigService.selectApplicationConfiguration(applicationId);
            authentication.addApplicationConfiguration(appConfig);
        }
        
        AppAuthConfiguration appAuthConfig = null;
        //TODO : 개선 필요
//        AppAuthConfiguration appAuthConfig = appConfig.getAppAuthConfiguration();
        
        if (MetaData.SQL.equals(metaData) && !appAuthConfig.getSqlMetaData()) {
            return true;
        } else if (MetaData.API.equals(metaData) && !appAuthConfig.getApiMetaData()) {
            return true;
        } else if (MetaData.PARAM.equals(metaData) && !appAuthConfig.getParamMetaData()) {
            return true;
        } 
        
        if (appConfig.isAffiliatedAppUserGroup(authentication.getUserGroupList())) {
            return true;
        }
        
        return false;
    }

    @Override
    public AnnotationBo createAnnotationBo(SpanAlign spanAlign, MetaData metaData) {
        AnnotationBo annotationBo = new AnnotationBo();
        
        if (MetaData.SQL.equals(metaData)) {
            annotationBo.setKey(AnnotationKey.SQL.getCode());
            annotationBo.setValue("you don't have authorization for " + spanAlign.getApplicationId() + ".");
            annotationBo.setAuthorized(false);
            return annotationBo;
        }
        
        return null;
    }

    @Override
    public void replaceAnnotationBo(SpanAlign spanAlign, MetaData metaData) {
        final List<AnnotationBo> annotationBoList = spanAlign.getAnnotationBoList();
        
        if (MetaData.PARAM.equals(metaData)) {
            for(AnnotationBo annotationBo : annotationBoList) {
                if(AnnotationKey.HTTP_URL.getCode() == annotationBo.getKey()) {
                    String url = InterceptorUtils.getHttpUrl(String.valueOf(annotationBo.getValue()), false);
                    annotationBo.setValue(url);
                } else if(AnnotationKey.HTTP_PARAM.getCode() == annotationBo.getKey()) {
                    annotationBo.setValue("you don't have authorization for " + spanAlign.getApplicationId() + ".");
                }
                annotationBo.setAuthorized(false);
            }
        }
        
        
    }

    @Override
    public Record createRecord(CallTreeNode node, RecordFactory factory) {
        return factory.getFilteredRecord(node, "you don't have authorization for " + node.getValue().getApplicationId() + ".");
    }

}
