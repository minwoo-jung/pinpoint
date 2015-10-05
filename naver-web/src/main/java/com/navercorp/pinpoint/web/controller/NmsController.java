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
package com.navercorp.pinpoint.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

/**
 * @author minwoo.jung
 */
@Controller
public class NmsController {

    private final static String NMS_CONN_CPS_API_URL = "http://view.nms.navercorp.com/nms-api/topology.php?cmd=vsByRsip&rsIp={hostIp}&rsPort={rsPort}";
    
    @Autowired  
    private RestTemplate restTemplate;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RequestMapping(value = "/nms", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> nms(@RequestParam(value= "hostIp", required=true) String hostIp) {
  
        //TODO call rport api 
  
        hostIp = "61.247.193.104";
        String rsPort = "80";
        ResponseEntity<Map> responseEntity = this.restTemplate.exchange(NMS_CONN_CPS_API_URL, HttpMethod.GET, null, Map.class, hostIp, rsPort);
        
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            Map<String, String> error = new HashMap<>();
            error.put("errorCode", "500");
            error.put("errorMessage", "Can not communicate NMS server.");
            return error;
        }

        Map<String, Object> responseData = responseEntity.getBody();
        Map<String, String> nmsUrlInfo = (Map<String, String>) responseData.get("result");
        Map<String, String> result= new HashMap<>();
        result.put("rsName", nmsUrlInfo.get("rsName"));
        result.put("rsIp", nmsUrlInfo.get("rsIp"));
        result.put("rsPort", nmsUrlInfo.get("rsPort"));
        result.put("vsIP", nmsUrlInfo.get("vsIp"));
        result.put("vsPort", nmsUrlInfo.get("vsPort"));
        result.put("vsConnUrl", nmsUrlInfo.get("vsConnUrl"));
        result.put("vsCpsUrl", nmsUrlInfo.get("vsCpsUrl"));
        result.put("rsConnUrl", nmsUrlInfo.get("rsConnUrl"));
        result.put("rsCpsUrl", nmsUrlInfo.get("rsCpsUrl"));
        
        return result;
  }
}
