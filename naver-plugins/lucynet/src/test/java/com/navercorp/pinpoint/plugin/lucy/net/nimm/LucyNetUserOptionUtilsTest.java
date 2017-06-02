package com.navercorp.pinpoint.plugin.lucy.net.nimm;

import com.navercorp.pinpoint.plugin.lucy.net.LucyNetUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Taejin Koo
 */
public class LucyNetUserOptionUtilsTest {

    RandomString randomString = new RandomString();

    @Test(expected = IllegalArgumentException.class)
    public void checkIllegalArgumentTest1() throws Exception {
        LucyNetUtils.getParameterAsString(null, 1000, 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkIllegalArgumentTest2() throws Exception {
        LucyNetUtils.getParameterAsString(null, -1, 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkIllegalArgumentTest3() throws Exception {
        LucyNetUtils.getParameterAsString(null, 1000, -1);
    }

    @Test
    public void listTest1() throws Exception {
        String parameterAsString = LucyNetUtils.getParameterAsString(null, 10, 100);

        Assert.assertEquals("null", parameterAsString);
    }

    @Test
    public void listTest2() throws Exception {
        int objectLength = 3;

        Object[] params = new Object[objectLength];

        int eachParamSize = 7;
        for (int i = 0; i < params.length; i++) {
            params[i] = RandomString.getRandomString(eachParamSize);
        }

        String parameterAsString = LucyNetUtils.getParameterAsString(params, 10, 100);
        System.out.println(parameterAsString);
        Assert.assertEquals((eachParamSize * objectLength + (objectLength + 1) * 2), parameterAsString.length());
    }

    @Test
    public void listTest3() throws Exception {
        int objectLength = 3;

        Object[] params = new Object[objectLength];

        int eachParamSize = 20;
        for (int i = 0; i < params.length; i++) {
            params[i] = RandomString.getRandomString(eachParamSize);
        }

        String parameterAsString = LucyNetUtils.getParameterAsString(params, 10, 100);
        System.out.println(parameterAsString);
        Assert.assertTrue((eachParamSize * objectLength + (objectLength + 1) * 2) > parameterAsString.length());
    }

    @Test
    public void listTest4() throws Exception {
        int objectLength = 15;

        Object[] params = new Object[objectLength];

        int eachParamSize = 20;
        for (int i = 0; i < params.length; i++) {
            params[i] = RandomString.getRandomString(eachParamSize);
        }

        String parameterAsString = LucyNetUtils.getParameterAsString(params, 10, 100);
        System.out.println(parameterAsString);
        Assert.assertTrue((eachParamSize * objectLength + (objectLength + 1) * 2) > parameterAsString.length());
        Assert.assertTrue(parameterAsString.endsWith("... ]"));
    }

    @Test
    public void mapTest1() throws Exception {
        int mapSize = 5;
        int valueSize = 10;

        Map map = new HashMap();

        for (int i = 0; i < mapSize; i++) {
            map.put("key" + (i + 1), RandomString.getRandomString(valueSize));
        }

        Object[] params = new Object[1];
        params[0] = map;

        String parameterAsString = LucyNetUtils.getParameterAsString(params, 10, 100);
        System.out.println(parameterAsString);
        Assert.assertTrue(!parameterAsString.endsWith("... }"));
    }

    @Test
    public void mapTest2() throws Exception {
        int mapSize = 10;
        int valueSize = 10;

        Map map = new HashMap();

        for (int i = 0; i < mapSize; i++) {
            map.put("key" + (i + 1), RandomString.getRandomString(valueSize));
        }

        Object[] params = new Object[1];
        params[0] = map;

        String parameterAsString = LucyNetUtils.getParameterAsString(params, 10, 100);
        System.out.println(parameterAsString);
        Assert.assertTrue(parameterAsString.endsWith("... }"));
    }

    @Test
    public void mapTest3() throws Exception {
        int mapSize = 10;
        int valueSize = 10;

        Map map = new HashMap();

        for (int i = 0; i < mapSize; i++) {
            map.put("key" + (i + 1), RandomString.getRandomString(valueSize));
        }

        Object[] params = new Object[1];
        params[0] = map;

        String parameterAsString = LucyNetUtils.getParameterAsString(params, 2, 100);
        System.out.println(parameterAsString);
        Assert.assertTrue(parameterAsString.endsWith("... }"));
    }

    private static class RandomString {

        private static final char[] symbols;
        private static final Random random = new Random();

        static {
            StringBuilder tmp = new StringBuilder();
            for (char ch = '0'; ch <= '9'; ++ch) {
                tmp.append(ch);
            }
            for (char ch = 'a'; ch <= 'z'; ++ch) {
                tmp.append(ch);
            }
            for (char ch = 'A'; ch <= 'Z'; ++ch) {
                tmp.append(ch);
            }
            symbols = tmp.toString().toCharArray();
        }

        public static String getRandomString(int length) {
            if (length < 0) {
                throw new IllegalArgumentException();
            }

            char[] chars = new char[length];

            for (int i = 0; i < chars.length; i++) {
                chars[i] = symbols[random.nextInt(symbols.length)];
            }

            return new String(chars);
        }
    }

}
