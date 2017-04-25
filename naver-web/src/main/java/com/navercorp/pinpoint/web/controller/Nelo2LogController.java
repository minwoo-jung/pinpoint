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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author minwoo.jung
 */
@Controller
public class Nelo2LogController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private static final long SEARCH_INTERVAL = 1000*60*60*12;
    
    @Value("#{pinpointWebProps['log.nelo.url']}")
    private String neloSightUrl;
    
//    @Autowired
//    Nelo2OpenApiCaller nelo2OpenApiCaller;
    
    @RequestMapping(value = "/neloLog", method = RequestMethod.GET)
//    @ResponseBody
    public RedirectView neloLog(@RequestParam(value= "transactionId", required=true) String transactionId,
                                            @RequestParam(value= "spanId", required=false) String spanId,
                                            @RequestParam(value="time", required=true) long time) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(neloSightUrl);
        urlBuilder.append("/search?");
        urlBuilder.append(makeParameter(transactionId, time));
        RedirectView view = new RedirectView(urlBuilder.toString());
        
        return view;
        
//        try {
//            List<NeloRawLog> logs = nelo2OpenApiCaller.requestNeloLog(transactionId, spanId, time);
//            StringBuilder sb = new StringBuilder();
//
//            if (logs != null) {
//                int i = 1;
//                for (Iterator<NeloRawLog> iterator = logs.iterator(); iterator.hasNext();) {
//                    NeloRawLog neloRawLog = (NeloRawLog) iterator.next();
//                    sb.append("<br/><br/>===============================================================<br/>");
//                    sb.append("==========================="+ i++ +"===============================<br/>");
//                    Map<String, String> log = neloRawLog.get_source();
//                    
//                    for(Entry<String, String> entry : log.entrySet()) {
//                        sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- " +  entry.getKey() + " : " + entry.getValue() + "<br/>");
//                    }
//                }
//            }
//            
//            return sb.toString();
//            
//        } catch (Exception e) {
//            logger.error("fail to require Nelo2 server to Log.", e);
//            return "FAIL";
//        }
    }

    private String makeParameter(String transactionId, long time) {
        StringBuilder paramsBuilder = new StringBuilder();

        try {
            paramsBuilder.append("cmd=" + "PtxId%3A%22" + URLEncoder.encode(transactionId, StandardCharsets.UTF_8.name()) + "%22");
        } catch (UnsupportedEncodingException e) {
            logger.error("Error while build nelo link url.", e);
            return null;
        }
        
        long from  = time - SEARCH_INTERVAL;
        long to = time + SEARCH_INTERVAL;
        final String interval = "&st=" + from + "&et=" + to;
        paramsBuilder.append(interval);

        return paramsBuilder.toString();
    }
}
