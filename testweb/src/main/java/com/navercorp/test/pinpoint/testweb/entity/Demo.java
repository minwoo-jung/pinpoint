/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.test.pinpoint.testweb.entity;

import com.frameworkset.orm.annotation.ESId;
import org.frameworkset.elasticsearch.entity.ESBaseData;

import java.util.Date;

/**
 * @author Roy Kim
 */
public class Demo extends ESBaseData {
    private Object dynamicPriceTemplate;
    //Set the document identity field
    @ESId(readSet = true,persistent = false)
    private Long demoId;
    private String contentbody;
    /**  When the date format is specified in the mapping definition,
     *  the following two annotations need to be specified, for example:
     *
     "agentStarttime": {
     "type": "date",
     ###Specify multiple date formats
     "format":"yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||epoch_millis"
     }
     @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
     @Column(dataformat = "yyyy-MM-dd HH:mm:ss.SSS")
     */

    protected Date agentStarttime;
    private String applicationName;
    private String orderId;
    private int contrastStatus;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    public String getContentbody() {
        return contentbody;
    }

    public void setContentbody(String contentbody) {
        this.contentbody = contentbody;
    }

    public Date getAgentStarttime() {
        return agentStarttime;
    }

    public void setAgentStarttime(Date agentStarttime) {
        this.agentStarttime = agentStarttime;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Long getDemoId() {
        return demoId;
    }

    public void setDemoId(Long demoId) {
        this.demoId = demoId;
    }

    public Object getDynamicPriceTemplate() {
        return dynamicPriceTemplate;
    }

    public void setDynamicPriceTemplate(Object dynamicPriceTemplate) {
        this.dynamicPriceTemplate = dynamicPriceTemplate;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getContrastStatus() {
        return contrastStatus;
    }

    public void setContrastStatus(int contrastStatus) {
        this.contrastStatus = contrastStatus;
    }
}
