package com.navercorp.pinpoint.testweb.controller;

import com.navercorp.pinpoint.testweb.service.MsSqlServerService;
import com.navercorp.pinpoint.testweb.util.Description;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 */
@Controller
public class MsSqlServerController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MsSqlServerService msSqlServerService;


    @Description("preparedStatement 테스트. resultset은 가지고 오지 않음.")
    @RequestMapping(value = "/mssqlserver/selectOne")
    @ResponseBody
    public String selectOne() {
        logger.info("selectOne start");

        int i = msSqlServerService.selectOne();

        logger.info("selectOne end:{}", i);
        return "OK";
    }

    @Description("preparedStatement 테스트 w/ 파라미터. resultset은 가지고 오지 않음.")
    @RequestMapping(value = "/mssqlserver/selectOneWithParam")
    @ResponseBody
    public String selectOneWithParam() {
        logger.info("selectOneWithParam start");

        int id = 99;
        msSqlServerService.selectOneWithParam(id);

        logger.info("selectOneWithParam end. id:{}", id);
        return "OK";
    }

    @Description("stored procedure IN, OUT 테스트")
    @RequestMapping(value = "/mssqlserver/callConcatCharacters")
    @ResponseBody
    public String callConcatCharacters() {
        logger.info("callConcatCharacters start");
        char a = 'a';
        char b = 'b';
        String concat = msSqlServerService.concat(a, b);

        logger.info("callConcatCharacters end. concat:{}", concat);
        if ("ab".equals(concat)) {
            return "OK";
        } else {
            return "FAIL";
        }
    }

    @Description("stored procedure INOUT 테스트")
    @RequestMapping(value = "/mssqlserver/callSwapAndGetSum")
    @ResponseBody
    public String callSwapAndGetSum() {
        logger.info("callSwapAndGetSum start");
        int a = 1;
        int b = 2;
        int sum = msSqlServerService.swapAndGetSum(a, b);

        logger.info("callSwapAndGetSum end. sum:{}", sum);
        if (sum != a + b) {
            return "FAIL";
        } else {
            return "OK";
        }
    }

    @Description("statement 테스트. resultset은 가지고 오지 않음.")
    @RequestMapping(value = "/mssqlserver/createStatement")
    @ResponseBody
    public String createStatement() {
        logger.info("createStatement start");

        msSqlServerService.createStatement();

        logger.info("createStatement end:{}");

        return "OK";
    }
}
