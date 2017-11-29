package com.navercorp.pinpoint.web.applicationmap.link;

import org.junit.Assert;

import org.junit.Test;

public class DefaultNsightMatcherTest {

    @Test
    public void success() {
        String sampleString = "dev-pinpoint-workload03.ncl";
        ServerMatcher matcher = new DefaultNSightMatcher();

        Assert.assertTrue(matcher.isMatched(sampleString));
        LinkInfo linkInfo = matcher.getLinkInfo(sampleString);
        Assert.assertEquals("http://nsight.navercorp.com/dashboard_server/dev-pinpoint-workload03.ncl", linkInfo.getLinkUrl());
    }
    
}