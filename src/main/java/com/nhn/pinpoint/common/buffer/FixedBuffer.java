package com.nhn.pinpoint.common.buffer;

import com.nhn.pinpoint.common.util.BytesUtils;

import java.io.UnsupportedEncodingException;

/**
 *
 */
public class FixedBuffer implements Buffer {

    protected byte[] buffer;
    protected int offset;

    private static final int BYTE_UNSIGNED_MAX = 256;
    private static final int SHORT_UNSIGNED_MAX = 65536;

    public FixedBuffer() {
        this(32);
    }

    public FixedBuffer(final int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size:" + size);
        }
        this.buffer = new byte[size];
        this.offset = 0;
    }

    public FixedBuffer(final byte[] buffer) {
        if (buffer == null) {
            throw new NullPointerException("buffer must not be null");
        }
        this.buffer = buffer;
        this.offset = 0;
    }

    public FixedBuffer(final byte[] buffer, final int offset) {
        if (buffer == null) {
            throw new NullPointerException("buffer must not be null");
        }
        this.buffer = buffer;
        this.offset = offset;
    }

    @Override
    public void put1PrefixedBytes(final byte[] bytes) {
        if (bytes == null) {
            put((byte)0);
        } else {
            final int length = bytes.length;
            if (length >= BYTE_UNSIGNED_MAX) {
                throw new IllegalArgumentException("too large bytes:" + bytes.length);
            }
            put((byte) length);
            put(bytes);
        }
    }

    @Override
    public void put2PrefixedBytes(final byte[] bytes) {
        if (bytes == null) {
            put((short)0);
        } else {
            final int length = bytes.length;
            if (length >= SHORT_UNSIGNED_MAX) {
                throw new IllegalArgumentException("too large bytes:" + bytes.length);
            }
            put((short) length);
            put(bytes);
        }
    }

    @Override
    public void putPrefixedBytes(final byte[] bytes) {
        if (bytes == null) {
            put(0);
        } else {
            put(bytes.length);
            put(bytes);
        }
    }

    @Override
    public void putNullTerminatedBytes(final byte[] bytes) {
        if (bytes == null) {
            put((byte) 0);
        } else {
            put(bytes);
            put((byte) 0);
        }
    }

    @Override
    public void putPrefixedString(final String string) {
        byte[] bytes = BytesUtils.getBytes(string);
        putPrefixedBytes(bytes);
    }

    @Override
    public void put(final byte v) {
        this.buffer[offset++] = v;
    }

    @Override
    public void put(final boolean v) {
        if (v) {
            this.buffer[offset++] = BOOLEAN_TRUE;
        } else {
            this.buffer[offset++] = BOOLEAN_FALSE;
        }
    }

    @Override
    public void put(final int v) {
        this.offset = BytesUtils.writeInt(v, buffer, offset);
    }

    public void putVar(int v) {
        if (v >= 0) {
            putVar32(v);
        } else {
            putVar64((long) v);
        }
    }

    public void putSVar(int v) {
        this.offset = BytesUtils.writeSVar32(v, buffer, offset);
    }

    private void putVar32(int v) {
        this.offset = BytesUtils.writeVar32(v, buffer, offset);
    }

    @Override
    public void put(final short v) {
        this.offset = BytesUtils.writeShort(v, buffer, offset);
    }

    @Override
    public void put(final long v) {
        this.offset = BytesUtils.writeLong(v, buffer, offset);
    }

    @Override
    public void putVar(long v) {
        putVar64(v);
    }

    @Override
    public void putSVar(long v) {
        putVar64(BytesUtils.encodeZigZagLong(v));
    }

    private void putVar64(long v) {
        this.offset = BytesUtils.writeVar64(v, buffer, offset);
    }



    @Override
    public void put(final byte[] v) {
        if (v == null) {
            throw new NullPointerException("v must not be null");
        }
        System.arraycopy(v, 0, buffer, offset, v.length);
        this.offset = offset + v.length;
    }

    @Override
    public byte readByte() {
        return this.buffer[offset++];
    }

    @Override
    public int readUnsignedByte() {
        return readByte() & 0xff;
    }

    @Override
    public boolean readBoolean() {
        final byte b = readByte();
        return b == BOOLEAN_TRUE;
    }

    @Override
    public int readInt() {
        final int i = BytesUtils.bytesToInt(buffer, offset);
        this.offset = this.offset + 4;
        return i;
    }

    @Override
    public int readVarInt() {
        // protocol buffer의 var encoding 차용.
        byte v = readByte();
        if (v >= 0) {
            return v;
        }
        int result = v & 0x7f;
        if ((v = readByte()) >= 0) {
            result |= v << 7;
        } else {
            result |= (v & 0x7f) << 7;
            if ((v = readByte()) >= 0) {
                result |= v << 14;
            } else {
                result |= (v & 0x7f) << 14;
                if ((v = readByte()) >= 0) {
                    result |= v << 21;
                } else {
                    result |= (v & 0x7f) << 21;
                    result |= (v = readByte()) << 28;
                    if (v < 0) {
                        for (int i = 0; i < 5; i++) {
                            if (readByte() >= 0) {
                                return result;
                            }
                        }
                        throw new IllegalArgumentException("invalid varInt");
                    }
                }
            }
        }
        return result;
    }

    public int readSVarInt() {
        return BytesUtils.decodeZigZagInt(readVarInt());
    }

    @Override
    public short readShort() {
        final short i = BytesUtils.bytesToShort(buffer, offset);
        this.offset = this.offset + 2;
        return i;
    }

    public int readUnsignedShort() {
        return readShort() & 0xFFFF;
    }

    @Override
    public long readLong() {
        final long l = BytesUtils.bytesToLong(buffer, offset);
        this.offset = this.offset + 8;
        return l;
    }

    @Override
    public long readVarLong() {
        int shift = 0;
        long result = 0;
        while (shift < 64) {
            final byte v = readByte();
            result |= (long)(v & 0x7F) << shift;
            if ((v & 0x80) == 0) {
                return result;
            }
            shift += 7;
        }
        throw new IllegalArgumentException("invalid varLong");
    }

    @Override
    public long readSVarLong() {
        return BytesUtils.decodeZigZagLong(readVarLong());
    }

    @Override
    public byte[] readPrefixedBytes() {
        final int size = readInt();
        if (size == 0) {
            return EMPTY;
        }
        return readBytes(size);
    }

    @Override
    public byte[] read1PrefixedBytes() {
        final int b = readUnsignedByte();
        if (b == 0) {
            return EMPTY;
        }
        return readBytes(b);
    }

    @Override
    public byte[] read2PrefixedBytes() {
        final int b = readUnsignedShort();
        if (b == 0) {
            return EMPTY;
        }
        return readBytes(b);
    }

    private byte[] readBytes(int size) {
        final byte[] b = new byte[size];
        System.arraycopy(buffer, offset, b, 0, size);
        this.offset = offset + size;
        return b;
    }

    @Override
    public String readPrefixedString() {
        final int size = readInt();
        if (size == 0) {
            return "";
        }
        return readString(size);
    }

    @Override
    public String read1PrefixedString() {
        final int size = readUnsignedByte();
        if (size == 0) {
            return "";
        }
        return readString(size);
    }

    @Override
    public String read1UnsignedPrefixedString() {
        final int size = readUnsignedByte();
        if (size == 0) {
            return "";
        }
        return readString(size);
    }

    @Override
    public String read2PrefixedString() {
        final int size = readShort();
        if (size == 0) {
            return "";
        }
        return readString(size);
    }

    @Override
    public String readNullTerminatedString() {
        final int size = findNull();
        if (size == 0) {
            return "";
        } else if (size == -1) {
            throw new IllegalArgumentException("null not found");
        }
        return readString(size);
    }

    private int findNull() {
        for (int i = offset; i < buffer.length; i++) {
            final byte b = this.buffer[i];
            if (b == 0) {
                return i - offset;
            }
        }
        return -1;
    }


    private String readString(final int size) {
        final String s = newString(size);
        this.offset = offset + size;
        return s;
    }

    private String newString(final int size) {
        try {
            return new String(buffer, offset, size, UTF8);
        } catch (UnsupportedEncodingException ue) {
            throw new RuntimeException(ue.getMessage(), ue);
        }
    }

    /**
     * 암묵적으로 성능을 내부 buffe length와 offset의 사이즈가 같으면 메모리 copy를 하지 않고 그냥 internal buffer를 리턴하므로 주의해야 한다.
     * @return
     */
    @Override
    public byte[] getBuffer() {
        if (offset == buffer.length) {
            return this.buffer;
        } else {
            final byte[] copy = new byte[offset];
            System.arraycopy(buffer, 0, copy, 0, offset);
            return copy;
        }
    }

    /**
     * 내부 buffer를 리턴한다.
     * @return
     */
    @Override
    public byte[] getInternalBuffer() {
        return this.buffer;
    }

    @Override
    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public int limit() {
        return buffer.length - offset;
    }
}
