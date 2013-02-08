package com.profiler.common.util;


import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BytesUtils {
    public static final int SHORT_BYTE_LENGTH = 2;
    public static final int INT_BYTE_LENGTH = 4;
    public static final int LONG_BYTE_LENGTH = 8;

    private static final byte[] EMPTY_BYTES = new byte[0];
    private static final String UTF8 = "UTF-8";

    public static byte[] longLongToBytes(final long value1, final long value2) {
        final byte[] buffer = new byte[16];
        writeFirstLong0(value1, buffer);
        writeSecondLong0(value2, buffer);
        return buffer;
    }

    public static long[] bytesToLongLong(final byte[] buf) {
        if (buf == null) {
            throw new NullPointerException("buf must not be null");
        }
        if (buf.length < 16) {
            throw new IllegalArgumentException("Illegal buf size.");
        }
        final long[] result = new long[2];

        result[0] = bytesToFirstLong0(buf);
        result[1] = bytesToSecondLong0(buf);

        return result;
    }

    public static long bytesToLong(final byte[] buf, final int offset) {
        if (buf == null) {
            throw new NullPointerException("buf must not be null");
        }
        if (buf.length < offset + 8) {
            throw new IllegalArgumentException("buf.length is too small. buf.length:" + buf.length + " offset:" + (offset + 8));
        }

        final long rv = (((long) buf[offset] & 0xff) << 56)
                | (((long) buf[offset + 1] & 0xff) << 48)
                | (((long) buf[offset + 2] & 0xff) << 40)
                | (((long) buf[offset + 3] & 0xff) << 32)
                | (((long) buf[offset + 4] & 0xff) << 24)
                | (((long) buf[offset + 5] & 0xff) << 16)
                | (((long) buf[offset + 6] & 0xff) << 8)
                | (((long) buf[offset + 7] & 0xff));
        return rv;
    }

    public static int bytesToInt(final byte[] buf, final int offset) {
        if (buf == null) {
            throw new NullPointerException("buf must not be null");
        }
        if (buf.length < offset + 4) {
            throw new IllegalArgumentException("buf.length is too small. buf.length:" + buf.length + " offset:" + (offset + 4));
        }

        final int v = ((buf[offset] & 0xff) << 24)
                | ((buf[offset + 1] & 0xff) << 16)
                | ((buf[offset + 2] & 0xff) << 8)
                | ((buf[offset + 3] & 0xff));

        return v;
    }

    public static short bytesToShort(final byte[] buf, final int offset) {
        if (buf == null) {
            throw new NullPointerException("buf must not be null");
        }
        if (buf.length < offset + 2) {
            throw new IllegalArgumentException("buf.length is too small. buf.length:" + buf.length + " offset:" + (offset + 2));
        }

        final short v = (short) (((buf[offset] & 0xff) << 8) | ((buf[offset + 1] & 0xff)));

        return v;
    }

    public static short bytesToShort(final byte byte1, final byte byte2) {
        return (short) (((byte1 & 0xff) << 8) | ((byte2 & 0xff)));
    }

    public static long bytesToFirstLong(final byte[] buf) {
        if (buf == null) {
            throw new NullPointerException("buf must not be null");
        }
        if (buf.length < 8) {
            throw new IllegalArgumentException("buf.length is too small(8). buf.length:" + buf.length);
        }

        return bytesToFirstLong0(buf);
    }

    private static long bytesToFirstLong0(byte[] buf) {
        final long rv = (((long) buf[0] & 0xff) << 56)
                | (((long) buf[1] & 0xff) << 48)
                | (((long) buf[2] & 0xff) << 40)
                | (((long) buf[3] & 0xff) << 32)
                | (((long) buf[4] & 0xff) << 24)
                | (((long) buf[5] & 0xff) << 16)
                | (((long) buf[6] & 0xff) << 8)
                | (((long) buf[7] & 0xff));
        return rv;
    }

    public static long bytesToSecondLong(final byte[] buf) {
        if (buf == null) {
            throw new NullPointerException("buf must not be null");
        }
        if (buf.length < 16) {
            throw new IllegalArgumentException("buf.length is too small(16). buf.length:" + buf.length);
        }

        return bytesToSecondLong0(buf);
    }

    private static long bytesToSecondLong0(byte[] buf) {
        final long rv = (((long) buf[8] & 0xff) << 56)
                | (((long) buf[9] & 0xff) << 48)
                | (((long) buf[10] & 0xff) << 40)
                | (((long) buf[11] & 0xff) << 32)
                | (((long) buf[12] & 0xff) << 24)
                | (((long) buf[13] & 0xff) << 16)
                | (((long) buf[14] & 0xff) << 8)
                | (((long) buf[15] & 0xff));
        return rv;
    }

    public static void writeLong(final long value, final byte[] buf, int offset) {
        if (buf == null) {
            throw new NullPointerException("buf must not be null");
        }
        if (buf.length < offset + 8) {
            throw new IllegalArgumentException("buf.length is too small. buf.length:" + buf.length + " offset:" + (offset + 8));
        }
        buf[offset++] = (byte) (value >> 56);
        buf[offset++] = (byte) (value >> 48);
        buf[offset++] = (byte) (value >> 40);
        buf[offset++] = (byte) (value >> 32);
        buf[offset++] = (byte) (value >> 24);
        buf[offset++] = (byte) (value >> 16);
        buf[offset++] = (byte) (value >> 8);
        buf[offset] = (byte) (value);
    }

    public static byte writeShort1(final short value) {
        return (byte) (value >> 8);
    }

    public static byte writeShort2(final short value) {
        return (byte) (value);
    }

    public static void writeShort(final short value, final byte[] buf, int offset) {
        if (buf == null) {
            throw new NullPointerException("buf must not be null");
        }
        if (buf.length < offset + 2) {
            throw new IllegalArgumentException("buf.length is too small. buf.length:" + buf.length + " offset:" + (offset + 2));
        }
        buf[offset++] = (byte) (value >> 8);
        buf[offset] = (byte) (value);
    }

    public static void writeInt(final int value, final byte[] buf, int offset) {
        if (buf == null) {
            throw new NullPointerException("buf must not be null");
        }
        if (buf.length < offset + 4) {
            throw new IllegalArgumentException("buf.length is too small. buf.length:" + buf.length + " offset:" + (offset + 4));
        }
        buf[offset++] = (byte) (value >> 24);
        buf[offset++] = (byte) (value >> 16);
        buf[offset++] = (byte) (value >> 8);
        buf[offset] = (byte) (value);
    }

    public static void writeFirstLong(final long value, byte[] buf) {
        if (buf == null) {
            throw new NullPointerException("buf must not be null");
        }
        if (buf.length < 8) {
            throw new IllegalArgumentException("buf.length is too small(8). buf.length:" + buf.length);
        }
        writeFirstLong0(value, buf);
    }

    private static void writeFirstLong0(final long value, final byte[] buf) {
        buf[0] = (byte) (value >> 56);
        buf[1] = (byte) (value >> 48);
        buf[2] = (byte) (value >> 40);
        buf[3] = (byte) (value >> 32);
        buf[4] = (byte) (value >> 24);
        buf[5] = (byte) (value >> 16);
        buf[6] = (byte) (value >> 8);
        buf[7] = (byte) (value);
    }

    public static void writeSecondLong(final long value, final byte[] buf) {
        if (buf == null) {
            throw new NullPointerException("buf must not be null");
        }
        if (buf.length < 16) {
            throw new IllegalArgumentException("buf.length is too small(16). buf.length:" + buf.length);
        }
        writeSecondLong0(value, buf);
    }


    private static Logger getLogger() {
        return Logger.getLogger(BytesUtils.class.getName());
    }

    private static void writeSecondLong0(final long value, final byte[] buf) {
        buf[8] = (byte) (value >> 56);
        buf[9] = (byte) (value >> 48);
        buf[10] = (byte) (value >> 40);
        buf[11] = (byte) (value >> 32);
        buf[12] = (byte) (value >> 24);
        buf[13] = (byte) (value >> 16);
        buf[14] = (byte) (value >> 8);
        buf[15] = (byte) (value);
    }

    public static byte[] add(final String prefix, final long postfix) {
        byte[] agentByte = getBytes(prefix);
        return add(agentByte, postfix);
    }

    public static byte[] add(final byte[] preFix, final long postfix) {
        byte[] buf = new byte[preFix.length + 8];
        System.arraycopy(preFix, 0, buf, 0, preFix.length);
        writeLong(postfix, buf, preFix.length);
        return buf;
    }

    public static byte[] add(final byte[] preFix, final short postfix) {
        byte[] buf = new byte[preFix.length + 2];
        System.arraycopy(preFix, 0, buf, 0, preFix.length);
        writeShort(postfix, buf, preFix.length);
        return buf;
    }

    public static byte[] add(final int preFix, final short postFix) {
        byte[] buf = new byte[4 + 2];
        writeInt(preFix, buf, 0);
        writeShort(postFix, buf, 4);
        return buf;
    }


    public static byte[] add(final long preFix, final short postFix) {
        byte[] buf = new byte[8 + 2];
        writeLong(preFix, buf, 0);
        writeShort(postFix, buf, 8);
        return buf;
    }

    public static byte[] getBytes(final String value) {
        if (value == null) {
            return EMPTY_BYTES;
        }
        try {
            return value.getBytes(UTF8);
        } catch (UnsupportedEncodingException e) {
            final Logger logger = getLogger();
            logger.log(Level.SEVERE, "String encoding fail. value:" + value + " Caused:" + e.getMessage(), e);
            return EMPTY_BYTES;
        }
    }

    public static byte[] merge(final byte[] b1, final byte[] b2) {
        if (b1 == null || b2 == null) {
            throw new IllegalArgumentException("b1, b2 is must not be null");
        }

        byte[] result = new byte[b1.length + b2.length];

        System.arraycopy(b1, 0, result, 0, b1.length);
        System.arraycopy(b2, 0, result, b1.length, b2.length);

        return result;
    }

    public static byte[] toFixedLengthBytes(final String str, final int length) {
        byte[] b1 = getBytes(str);

        if (b1.length > length) {
            throw new IllegalArgumentException("String is longer then target length of bytes.");
        }
        byte[] b = new byte[length];
        System.arraycopy(b1, 0, b, 0, b1.length);

        return b;
    }


    public static int encodeZigZagInt(final int n) {
        return (n << 1) ^ (n >> 31);
    }

    public static int decodeZigZagInt(final int n) {
        return (n >>> 1) ^ -(n & 1);
    }

    public static int decodeZigZagInt2(final int n) {
        return (n >>> 1) ^ -(n & 1);
    }


    public static long encodeZigZagLong(final long n) {
        return (n << 1) ^ (n >> 63);
    }

    public static long decodeZigZagLong(final long n) {
        return (n >>> 1) ^ -(n & 1);
    }


}
