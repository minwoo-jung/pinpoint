/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.test.pinpoint.testweb.controller;

import com.navercorp.test.pinpoint.testweb.domain.ControllerMappingInfo;
import com.navercorp.test.pinpoint.testweb.util.Description;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author netspider
 */
@Controller
public class MainController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public MainController(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }


    @RequestMapping(value = "/docs", method = RequestMethod.GET)
    public String getEndPointsInView(Model model) {
        model.addAttribute("mapping", getMappingInfo());
        return "docs";
    }


    public Map<String, List<ControllerMappingInfo>> getMappingInfo() {
        Map<String, List<ControllerMappingInfo>> info = new TreeMap<String, List<ControllerMappingInfo>>();

        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethodEntry : handlerMethods.entrySet()) {
            RequestMappingInfo requestMappingInfoKey = requestMappingInfoHandlerMethodEntry.getKey();
            HandlerMethod handlerMethod = requestMappingInfoHandlerMethodEntry.getValue();
            Method method = handlerMethod.getMethod();
            Class<?> declaringClass = method.getDeclaringClass();

            List<ControllerMappingInfo> controllerMappingInfoList = info.get(declaringClass.getSimpleName());
            if (controllerMappingInfoList == null) {
                controllerMappingInfoList = new ArrayList<ControllerMappingInfo>();
                info.put(declaringClass.getSimpleName(), controllerMappingInfoList);
            }

            List<ControllerMappingInfo> requestInfo = createRequestMappingInfo(requestMappingInfoKey, handlerMethod);
            controllerMappingInfoList.addAll(requestInfo);
        }
        sort(info);

        logger.debug("{}", info);
        return info;
    }

    private void sort(Map<String, List<ControllerMappingInfo>> info) {
        for (List<ControllerMappingInfo> controllerMappingInfos : info.values()) {
            Collections.sort(controllerMappingInfos, new Comparator<ControllerMappingInfo>() {
                @Override
                public int compare(ControllerMappingInfo o1, ControllerMappingInfo o2) {
                    return o1.getUrl().compareTo(o2.getUrl());
                }
            });
        }
    }

    private List<ControllerMappingInfo> createRequestMappingInfo(RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {
        List<ControllerMappingInfo> requestInfo = new ArrayList<ControllerMappingInfo>();

        Set<String> patterns = requestMappingInfo.getPatternsCondition().getPatterns();
        for (String pattern : patterns) {
            Description description = getDescription(handlerMethod);
            ControllerMappingInfo info = new ControllerMappingInfo(pattern, (description == null) ? "" : description.value());
            requestInfo.add(info);
        }

        return requestInfo;
    }

    private Description getDescription(HandlerMethod handlerMethod) {
        Annotation[] annotations = handlerMethod.getMethod().getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof Description) {
                return (Description) annotation;
            }
        }

        return null;
    }

}