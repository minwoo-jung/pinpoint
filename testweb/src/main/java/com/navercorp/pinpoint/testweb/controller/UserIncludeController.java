package com.navercorp.pinpoint.testweb.controller;

import com.test.include.StandAloneCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Naver on 2015-11-27.
 */
@Controller
public class UserIncludeController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value="/include/standalone/execute")
    @ResponseBody
    public String execute() {
        logger.info("execute");

        StandAloneCase testcase = new StandAloneCase();
        return testcase.execute();
    }

    @RequestMapping(value="/include/standalone/nested")
    @ResponseBody
    public String nested() {
        logger.info("nested");

        StandAloneCase testcase = new StandAloneCase();
        return testcase.nested();
    }
}
