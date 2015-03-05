package com.navercorp.pinpoint.web.applicationmap.link;

import org.junit.Assert;

import org.junit.Test;

public class DefaultNsightMatcherTest {

    @Test
    public void success() {
        String sampleString = "dev-pinpoint-workload003.ncl";
        ServerMatcher matcher = new DefaultNSightMatcher();

        Assert.assertTrue(matcher.isMatched(sampleString));
        String link = matcher.getLink(sampleString);
        Assert.assertEquals("http://nsight.nhncorp.com/dashboard_server/dev-pinpoint-workload003.ncl", link);
    }

}