package com.test.include;

/**
 * Created by Naver on 2015-11-27.
 */
public class StandAloneCase {

    public String execute() {
        return "OK";
    }

    public String nested() {
        return execute();
    }
}
