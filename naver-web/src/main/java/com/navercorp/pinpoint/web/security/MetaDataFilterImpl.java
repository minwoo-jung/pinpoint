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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.web.calltree.span.CallTreeNode;
import com.navercorp.pinpoint.web.calltree.span.SpanAlign;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth;
import com.navercorp.pinpoint.web.vo.callstacks.Record;
import com.navercorp.pinpoint.web.vo.callstacks.RecordFactory;

/**
 * @author minwoo.jung
 */
public class MetaDataFilterImpl extends AppConfigOrganizer implements MetaDataFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public boolean filter(SpanAlign spanAlign, MetaData metaData) {
        return isAuthorized(spanAlign, metaData) ? false : true;
    }

    private boolean isAuthorized(SpanAlign spanAlign, MetaData metaData) {
        PinpointAuthentication authentication = (PinpointAuthentication)SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            logger.info("Authorization is fail. Because authentication is null.");
            return false;
        }
        if (isPinpointManager(authentication)) {
            return true;
        }
        
        String applicationId = spanAlign.getApplicationId();
        if(isEmptyUserGroup(authentication, applicationId)) {
            return true;
        }

        List<AppUserGroupAuth> userGroupAuths = userGroupAuth(authentication, applicationId);
        if (MetaData.SQL.equals(metaData)) {
            for(AppUserGroupAuth auth : userGroupAuths) {
                if (auth.getConfiguration().getSqlMetaData() == false) {
                    return true;
                }
            }

            logger.info("User({}) don't have {} authorization for {}.",authentication.getPrincipal(), MetaData.SQL,  applicationId);
            return false;
        } else if (MetaData.API.equals(metaData)) {
            for(AppUserGroupAuth auth : userGroupAuths) {
                if (auth.getConfiguration().getApiMetaData() == false) {
                    return true;
                }
            }

            logger.info("User({}) don't have {} authorization for {}.",authentication.getPrincipal(), MetaData.API,  applicationId);
            return false;
        } else if (MetaData.PARAM.equals(metaData)) {
            for(AppUserGroupAuth auth : userGroupAuths) {
                if (auth.getConfiguration().getParamMetaData() == false) {
                    return true;
                }
            }

            logger.info("User({}) don't have {} authorization for {}.",authentication.getPrincipal(), MetaData.PARAM,  applicationId);
            return false;
        }

        logger.info("User({}) don't have {} authorization for {}.", authentication.getPrincipal(), metaData, applicationId);
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
