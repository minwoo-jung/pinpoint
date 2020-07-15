package com.navercorp.pinpoint.bootstrap.agentdir;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JarDescriptionTest {

    @Test
    public void simpleTest() {
//        static final String VERSION_PATTERN = "(-[0-9]+\\.[0-9]+\\.[0-9]+((-[a-zA-Z]*)|())+((\\-SNAPSHOT)|(-RC[0-9]+))?)?";


        Pattern compile = Pattern.compile(JarDescription.VERSION_PATTERN);
        Matcher matcher = compile.matcher("-1.1.0-BETA-RC1");
        Assert.assertTrue(matcher.matches());

        matcher = compile.matcher("-1.1.0-NCP-RC4");
        Assert.assertTrue(matcher.matches());

        matcher = compile.matcher("-1.1.0-RC1");
        Assert.assertTrue(matcher.matches());

        matcher = compile.matcher("-1.1.0");
        Assert.assertTrue(matcher.matches());

        matcher = compile.matcher("-RC1");
        Assert.assertFalse(matcher.matches());
    }

}
