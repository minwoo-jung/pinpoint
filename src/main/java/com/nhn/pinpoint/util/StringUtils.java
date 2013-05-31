package com.nhn.pinpoint.util;

public class StringUtils {

    public static String defaultString(final String str, final String defaultStr) {
        return str == null ? defaultStr : str;
    }

    public static String toString(final Object object) {
        if (object == null) {
            return "null";
        }
        return object.toString();
    }

    public static String drop(final String str) {
        return drop(str, 64);
    }

    public static String drop(final String str, final int length) {
        if (str == null) {
            return "null";
        }
        if (str.length() > length) {
            StringBuilder buffer = new StringBuilder(length + 10);
            buffer.append(str.substring(0, length));
            appendDropMessage(buffer, str.length());
            return buffer.toString();
        } else {
            return str;
        }
    }

    private static void appendDropMessage(StringBuilder buffer, int length) {
        buffer.append("...(");
        buffer.append(length);
        buffer.append(")");
    }
}
