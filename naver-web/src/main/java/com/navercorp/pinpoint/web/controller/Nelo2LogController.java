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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.web.log.nelo.Nelo2OpenApiCaller;
import com.navercorp.pinpoint.web.log.nelo.NeloRawLog;

/**
 * @author minwoo.jung
 */
@Controller
public class Nelo2LogController {

    private static Logger logger = LoggerFactory.getLogger(Nelo2LogController.class);
    
    @Autowired
    Nelo2OpenApiCaller nelo2OpenApiCaller;
    
    @RequestMapping(value = "/NeloLogWithTransactionId", method = RequestMethod.GET)
    @ResponseBody
    public String NeloLogForTransactionID(String transactionId) {
        try {
            List<NeloRawLog> logs = nelo2OpenApiCaller.requestNeloLog(transactionId);
            StringBuilder sb = new StringBuilder();
            
            if (logs != null) {
                int i = 1;
                for (Iterator<NeloRawLog> iterator = logs.iterator(); iterator.hasNext();) {
                    NeloRawLog neloRawLog = (NeloRawLog) iterator.next();
                    sb.append("<br/><br/>===============================================================<br/>");
                    sb.append("==========================="+ i +"===============================<br/>");
                    Map<String, String> log = neloRawLog.get_source();
                    
                    for(Entry<String, String> entry : log.entrySet()) {
                        sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- " +  entry.getKey() + " : " + entry.getValue() + "<br/>");
                    }
                }
            }
            
            return sb.toString();
            
        } catch (Exception e) {
            logger.error("fail to require Nelo2 server to Log.", e);
            return "FAIL";
        }
    }
}
