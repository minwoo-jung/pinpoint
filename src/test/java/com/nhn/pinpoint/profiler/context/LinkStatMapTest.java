package com.nhn.pinpoint.profiler.context;

import junit.framework.Assert;
import org.junit.Test;
import sun.util.logging.resources.logging_it;

import java.util.List;

/**
 * @author emeroad
 */
public class LinkStatMapTest {

    @Test
    public void testRecord() throws Exception {
        LinkStatMap map = new LinkStatMap();

        map.recordLink("aa", (short) 0, "test", 0, true);
        map.recordLink("aa", (short) 0, "test", 0, true);
//        List<LinkData> all = map.getAll();
//
//        Assert.assertEquals(all.size(), 1);
//        Assert.assertEquals(all., 1);


    }
}
