package com.lmgy.redirect.net;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author lmgy
 * @date 2019/8/14
 */
public class ByteBufferPool {
    private static final int BUFFER_SIZE = 16384;
    private static ConcurrentLinkedQueue<ByteBuffer> pool = new ConcurrentLinkedQueue<>();

    public static ByteBuffer acquire() {
        ByteBuffer buffer = pool.poll();
        if (buffer == null) {
            // Using DirectBuffer for zero-copy
            buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        }
        return buffer;
    }

    public static void release(ByteBuffer buffer) {
        buffer.clear();
        pool.offer(buffer);
    }

    public static void clear() {
        pool.clear();
    }
}
