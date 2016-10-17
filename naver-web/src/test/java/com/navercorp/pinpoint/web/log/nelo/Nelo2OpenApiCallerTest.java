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

package com.navercorp.pinpoint.web.log.nelo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author minwoo.jung
 */
//@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextHierarchy({@ContextConfiguration({"/applicationContext-web-nelo.xml"})})
// 아무것도 test메소드가 없으면 ci에서 실패남.
@Ignore
public class Nelo2OpenApiCallerTest {

    @Autowired
    Nelo2OpenApiCaller nelo2OpenApiCaller;
    
//    @Test
    public void testConnect() throws Exception {
        try {
            List<NeloRawLog> logs = nelo2OpenApiCaller.requestNeloLog("minwoo_local_tomcat^1425368654938^10", null, 0);
            
            if (logs != null) {
                int i = 1;
                for (NeloRawLog neloRawLog : logs) {
                    System.out.println(i++ + " : " + neloRawLog.get_source() + "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
//    @Test
    public void parseResultData() throws JsonParseException, JsonMappingException, IOException {
        String returnData = "{\"status\":200,\"url\":\"/search?query=projectName%3A%22PINPOINT%22%20AND%20logLevel%3A%22INFO%22&from=1423791332376&to=1424050532375\",\"took\":471,\"total\":1746,\"timed_out\":false,\"limit\":10,\"offset\":0,\"logs\":[{\"_source\":{\"Location\":\"com.navercorp.pinpoint.testweb.controller.LoggingController.writeLog():72\",\"PeerIP\":\"10.64.53.100\",\"Platform\":\"Windows 7\",\"RequestHeader\":\"Referer : \\nUser-Agent : \\nCookie :\",\"body\":\"loggingWithMDC.pinpoint api is called\",\"errorCode\":\"loggingWithMDC.pinpoint api is called\",\"host\":\"AD00033719\",\"logLevel\":\"INFO\",\"logSource\":\"test\",\"logTime\":\"1423809539592\",\"logType\":\"testtest\",\"projectName\":\"PINPOINT\",\"projectVersion\":\"1.0\",\"time\":\"!!!Fri Feb 13 15:38:59 KST 2015\"},\"sort\":[1423809539592],\"id\":\"3tVlvQRwGECAKYqRn1euEg\",\"url\":\"/search/3tVlvQRwGECAKYqRn1euEg\",\"score\":null},{\"_source\":{\"Location\":\"org.springframework.web.servlet.FrameworkServlet.initServletBean():473\",\"PeerIP\":\"10.64.53.100\",\"Platform\":\"Windows 7\",\"RequestHeader\":\"Referer : \\nUser-Agent : \\nCookie :\",\"body\":\"FrameworkServlet 'pinpoint-testweb': initialization completed in 1123 ms\",\"errorCode\":\"FrameworkServlet 'pinpoint-testweb': initialization completed in 1123 ms\",\"host\":\"AD00033719\",\"logLevel\":\"INFO\",\"logSource\":\"test\",\"logTime\":\"1423809525024\",\"logType\":\"testtest\",\"projectName\":\"PINPOINT\",\"projectVersion\":\"1.0\"},\"sort\":[1423809525024],\"id\":\"PsQisZ5Jabk2sM2dsoEWaA\",\"url\":\"/search/PsQisZ5Jabk2sM2dsoEWaA\",\"score\":null},{\"_source\":{\"Location\":\"org.springframework.web.servlet.handler.AbstractHandlerMethodMapping.registerHandlerMethod():179\",\"PeerIP\":\"10.64.53.100\",\"Platform\":\"Windows 7\",\"RequestHeader\":\"Referer : \\nUser-Agent : \\nCookie :\",\"body\":\"Mapped \\\"{[/redis/jedis/pipeline],methods=[],params=[],headers=[],consumes=[],produces=[],custom=[]}\\\" onto public java.lang.String com.navercorp.pinpoint.testweb.controller.RedisController.jedisPipeline(org.springframework.ui.Model)\",\"errorCode\":\"Mapped \\\"{[/redis/jedis/pipeline],methods=[],params=[],headers=[],consumes=[],produces=[],custom=[]}\\\" onto public java.lang.Strin\",\"host\":\"AD00033719\",\"logLevel\":\"INFO\",\"logSource\":\"test\",\"logTime\":\"1423809524696\",\"logType\":\"testtest\",\"projectName\":\"PINPOINT\",\"projectVersion\":\"1.0\"},\"sort\":[1423809524696],\"id\":\"9N/lDTuYyuDlB9BfLYXFMw\",\"url\":\"/search/9N%2FlDTuYyuDlB9BfLYXFMw\",\"score\":null},{\"_source\":{\"Location\":\"org.springframework.web.servlet.handler.AbstractHandlerMethodMapping.registerHandlerMethod():179\",\"PeerIP\":\"10.64.53.100\",\"Platform\":\"Windows 7\",\"RequestHeader\":\"Referer : \\nUser-Agent : \\nCookie :\",\"body\":\"Mapped \\\"{[/redis/nBaseArc],methods=[],params=[],headers=[],consumes=[],produces=[],custom=[]}\\\" onto public java.lang.String com.navercorp.pinpoint.testweb.controller.RedisController.nBaseArc(org.springframework.ui.Model)\",\"errorCode\":\"Mapped \\\"{[/redis/nBaseArc],methods=[],params=[],headers=[],consumes=[],produces=[],custom=[]}\\\" onto public java.lang.String com.\",\"host\":\"AD00033719\",\"logLevel\":\"INFO\",\"logSource\":\"test\",\"logTime\":\"1423809524696\",\"logType\":\"testtest\",\"projectName\":\"PINPOINT\",\"projectVersion\":\"1.0\"},\"sort\":[1423809524696],\"id\":\"BYPbHdnVaU+olA89YtPF3A\",\"url\":\"/search/BYPbHdnVaU%2BolA89YtPF3A\",\"score\":null},{\"_source\":{\"Location\":\"org.springframework.web.servlet.handler.AbstractHandlerMethodMapping.registerHandlerMethod():179\",\"PeerIP\":\"10.64.53.100\",\"Platform\":\"Windows 7\",\"RequestHeader\":\"Referer : \\nUser-Agent : \\nCookie :\",\"body\":\"Mapped \\\"{[/redis/jedis],methods=[],params=[],headers=[],consumes=[],produces=[],custom=[]}\\\" onto public java.lang.String com.navercorp.pinpoint.testweb.controller.RedisController.jedis(org.springframework.ui.Model)\",\"errorCode\":\"Mapped \\\"{[/redis/jedis],methods=[],params=[],headers=[],consumes=[],produces=[],custom=[]}\\\" onto public java.lang.String com.nav\",\"host\":\"AD00033719\",\"logLevel\":\"INFO\",\"logSource\":\"test\",\"logTime\":\"1423809524681\",\"logType\":\"testtest\",\"projectName\":\"PINPOINT\",\"projectVersion\":\"1.0\"},\"sort\":[1423809524681],\"id\":\"NEu3dEFXxUzAD6BIGr2VYw\",\"url\":\"/search/NEu3dEFXxUzAD6BIGr2VYw\",\"score\":null},{\"_source\":{\"Location\":\"org.springframework.web.servlet.handler.AbstractHandlerMethodMapping.registerHandlerMethod():179\",\"PeerIP\":\"10.64.53.100\",\"Platform\":\"Windows 7\",\"RequestHeader\":\"Referer : \\nUser-Agent : \\nCookie :\",\"body\":\"Mapped \\\"{[/redis/nBaseArc/pipeline],methods=[],params=[],headers=[],consumes=[],produces=[],custom=[]}\\\" onto public java.lang.String com.navercorp.pinpoint.testweb.controller.RedisController.nBaseArcPipeline(org.springframework.ui.Model)\",\"errorCode\":\"Mapped \\\"{[/redis/nBaseArc/pipeline],methods=[],params=[],headers=[],consumes=[],produces=[],custom=[]}\\\" onto public java.lang.St\",\"host\":\"AD00033719\",\"logLevel\":\"INFO\",\"logSource\":\"test\",\"logTime\":\"1423809524681\",\"logType\":\"testtest\",\"projectName\":\"PINPOINT\",\"projectVersion\":\"1.0\"},\"sort\":[1423809524681],\"id\":\"p37ERbqMFA5u/vQBqXmxHA\",\"url\":\"/search/p37ERbqMFA5u%2FvQBqXmxHA\",\"score\":null},{\"_source\":{\"Location\":\"org.springframework.web.servlet.handler.AbstractHandlerMethodMapping.registerHandlerMethod():179\",\"PeerIP\":\"10.64.53.100\",\"Platform\":\"Windows 7\",\"RequestHeader\":\"Referer : \\nUser-Agent : \\nCookie :\",\"body\":\"Mapped \\\"{[/orm/mybatis/sqlSessionTemplate/transaction],methods=[],params=[],headers=[],consumes=[],produces=[],custom=[]}\\\" onto public java.lang.String com.navercorp.pinpoint.testweb.controller.OrmController.myBatisSqlSessionTemplateTransaction()\",\"errorCode\":\"Mapped \\\"{[/orm/mybatis/sqlSessionTemplate/transaction],methods=[],params=[],headers=[],consumes=[],produces=[],custom=[]}\\\" onto\",\"host\":\"AD00033719\",\"logLevel\":\"INFO\",\"logSource\":\"test\",\"logTime\":\"1423809524665\",\"logType\":\"testtest\",\"projectName\":\"PINPOINT\",\"projectVersion\":\"1.0\"},\"sort\":[1423809524665],\"id\":\"Bxm7VCk1QCfDNGIbkHhnaQ\",\"url\":\"/search/Bxm7VCk1QCfDNGIbkHhnaQ\",\"score\":null},{\"_source\":{\"Location\":\"org.springframework.web.servlet.handler.AbstractHandlerMethodMapping.registerHandlerMethod():179\",\"PeerIP\":\"10.64.53.100\",\"Platform\":\"Windows 7\",\"RequestHeader\":\"Referer : \\nUser-Agent : \\nCookie :\",\"body\":\"Mapped \\\"{[/orm/mybatis/sqlSessionTemplate/invalid],methods=[],params=[],headers=[],consumes=[],produces=[],custom=[]}\\\" onto public java.lang.String com.navercorp.pinpoint.testweb.controller.OrmController.myBatisSqlSessionTemplateInvalid()\",\"errorCode\":\"Mapped \\\"{[/orm/mybatis/sqlSessionTemplate/invalid],methods=[],params=[],headers=[],consumes=[],produces=[],custom=[]}\\\" onto publ\",\"host\":\"AD00033719\",\"logLevel\":\"INFO\",\"logSource\":\"test\",\"logTime\":\"1423809524665\",\"logType\":\"testtest\",\"projectName\":\"PINPOINT\",\"projectVersion\":\"1.0\"},\"sort\":[1423809524665],\"id\":\"s8P021am8+r/VyM+3uPBqA\",\"url\":\"/search/s8P021am8%2Br%2FVyM%2B3uPBqA\",\"score\":null},{\"_source\":{\"Location\":\"org.springframework.web.servlet.handler.AbstractHandlerMethodMapping.registerHandlerMethod():179\",\"PeerIP\":\"10.64.53.100\",\"Platform\":\"Windows 7\",\"RequestHeader\":\"Referer : \\nUser-Agent : \\nCookie :\",\"body\":\"Mapped \\\"{[/orm/mybatis/sqlSessionTemplate/query],methods=[],params=[],headers=[],consumes=[],produces=[],custom=[]}\\\" onto public java.lang.String com.navercorp.pinpoint.testweb.controller.OrmController.myBatisSqlSessionTemplateQuery()\",\"errorCode\":\"Mapped \\\"{[/orm/mybatis/sqlSessionTemplate/query],methods=[],params=[],headers=[],consumes=[],produces=[],custom=[]}\\\" onto public\",\"host\":\"AD00033719\",\"logLevel\":\"INFO\",\"logSource\":\"test\",\"logTime\":\"1423809524665\",\"logType\":\"testtest\",\"projectName\":\"PINPOINT\",\"projectVersion\":\"1.0\"},\"sort\":[1423809524665],\"id\":\"iYmvtDtXTFq2MUycDknKQA\",\"url\":\"/search/iYmvtDtXTFq2MUycDknKQA\",\"score\":null},{\"_source\":{\"Location\":\"org.springframework.web.servlet.handler.AbstractHandlerMethodMapping.registerHandlerMethod():179\",\"PeerIP\":\"10.64.53.100\",\"Platform\":\"Windows 7\",\"RequestHeader\":\"Referer : \\nUser-Agent : \\nCookie :\",\"body\":\"Mapped \\\"{[/orm/ibatis/sqlMapSession/transaction],methods=[],params=[],headers=[],consumes=[],produces=[],custom=[]}\\\" onto public java.lang.String com.navercorp.pinpoint.testweb.controller.OrmController.iBatisSqlMapSessionTransaction()\",\"errorCode\":\"Mapped \\\"{[/orm/ibatis/sqlMapSession/transaction],methods=[],params=[],headers=[],consumes=[],produces=[],custom=[]}\\\" onto public\",\"host\":\"AD00033719\",\"logLevel\":\"INFO\",\"logSource\":\"test\",\"logTime\":\"1423809524650\",\"logType\":\"testtest\",\"projectName\":\"PINPOINT\",\"projectVersion\":\"1.0\"},\"sort\":[1423809524650],\"id\":\"6cfxrPtYAtAjvKaA5J89bg\",\"url\":\"/search/6cfxrPtYAtAjvKaA5J89bg\",\"score\":null}],\"aggregations\":{},\"highlights\":{},\"histogram\":[]}";
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        NeloRawData neloRawData = objectMapper.readValue(returnData, new TypeReference<NeloRawData>(){ });
        
        List<NeloRawLog> logs = neloRawData.getLogs();
        
        if (logs != null) {
            int i = 1;
            for (NeloRawLog neloRawLog : logs) {
                System.out.println(i++ + " : " + neloRawLog.get_source() + "\n");
            }
        }
    }
    
//    @Test
    public void encodeTest() throws UnsupportedEncodingException {
        System.out.println(URLEncoder.encode("minwoo_local_tomcat^1425368654938^10", "UTF-8"));
        System.out.println(URLEncoder.encode("transactionid=\"minwoo_local_tomcat^1425368654938^10\"", "UTF-8"));
//        minwoo_local_tomcat%5E1425368654938%5E10
    }
}
