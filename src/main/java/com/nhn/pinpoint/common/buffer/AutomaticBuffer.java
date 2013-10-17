package com.nhn.pinpoint.common.buffer;

import com.nhn.pinpoint.common.util.BytesUtils;

/**
 * 버퍼사이즈가 자동으로 확장되는 buffer
 */
public class AutomaticBuffer extends FixedBuffer {

    private static final int VLONG_MAX_SIZE = 10;
    private static final int VINT_MAX_SIZE = 5;

    public AutomaticBuffer() {
        super(32);
    }

    public AutomaticBuffer(final int size) {
        super(size);
    }

    public AutomaticBuffer(final byte[] buffer) {
        super(buffer);
    }

    public AutomaticBuffer(final byte[] buffer, final int offset) {
        super(buffer, offset);
    }

    private void checkExpend(final int size) {
        int length = buffer.length;
        final int remain = length - offset;
        if (remain >= size) {
            return;
        }

        if (length == 0) {
            length = 1;
        }

        // 사이즈 계산을 먼저한 후에 buffer를 한번만 할당하도록 변경.
        final int expendBufferSize = computeExpendBufferSize(size, length, remain);
        // allocate buffer
        final byte[] expendBuffer = new byte[expendBufferSize];
        System.arraycopy(buffer, 0, expendBuffer, 0, buffer.length);
        buffer = expendBuffer;
    }

    private int computeExpendBufferSize(final int size, int length, int remain) {
        int expendBufferSize = 0;
        while (remain < size) {
            length <<= 2;
            expendBufferSize = length;
            remain = expendBufferSize - offset;
        }
        return expendBufferSize;
    }


    @Override
    public void putPrefixedBytes(final byte[] bytes) {
        if (bytes == null) {
            checkExpend(VINT_MAX_SIZE);
        } else {
            checkExpend(bytes.length + VINT_MAX_SIZE);
        }
        super.putPrefixedBytes(bytes);
    }


    @Override
    public void putPrefixedString(final String string) {
        byte[] bytes = BytesUtils.toBytes(string);
        this.putPrefixedBytes(bytes);
    }

    @Override
    public void put(final byte v) {
        checkExpend(1);
        super.put(v);
    }

    @Override
    public void put(final boolean v) {
        checkExpend(1);
        super.put(v);
    }

    @Override
    public void put(final short v) {
        checkExpend(2);
        super.put(v);
    }

    @Override
    public void put(final int v) {
        checkExpend(4);
        super.put(v);
    }

    public void putVar(final int v) {
        checkExpend(VLONG_MAX_SIZE);
        super.putVar(v);
    }

    public void putSVar(final int v) {
        checkExpend(VINT_MAX_SIZE);
        super.putSVar(v);
    }

    public void putVar(final long v) {
        checkExpend(VLONG_MAX_SIZE);
        super.putVar(v);
    }

    public void putSVar(final long v) {
        checkExpend(VLONG_MAX_SIZE);
        super.putSVar(v);
    }

    @Override
    public void put(final long v) {
        checkExpend(8);
        super.put(v);
    }

    @Override
    public void put(final byte[] v) {
        if (v == null) {
            throw new NullPointerException("v must not be null");
        }
        super.put(v);
    }


}
