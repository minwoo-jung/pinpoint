package com.navercorp.pinpoint.testweb.controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.navercorp.pinpoint.testweb.domain.ControllerMappingInfo;
import com.navercorp.pinpoint.testweb.util.Description;

@Controller
public class LoggingController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public LoggingController(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }


    @RequestMapping(value = "/logging", method = RequestMethod.GET)
    public String logging(Model model) {
        model.addAttribute("mapping", getMappingInfo());
        return "docs";
    }
    
    @RequestMapping(value = "/loggingWithMDC", method = RequestMethod.GET)
    public String logging2(Model model) {
        settingMDCValue();
        model.addAttribute("mapping", getMappingInfo());
        return "docs";
    }


    private void settingMDCValue() {
        MDC.put("ProjectName", "pinpoint-testweb");
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
        logger.info(" {}", info);
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
            if (annotation instanceof com.navercorp.pinpoint.testweb.util.Description) {
                return (Description) annotation;
            }
        }

        return null;
    }
}
