package org.noear.socketd.protocol;

import java.io.IOException;

/**
 * 编解码器
 *
 * @author noear
 * @since 2.0
 */
public interface Codec<T> {
    /**
     * 编码
     */
    Frame decode(T buffer) throws IOException;

    /**
     * 解码
     */
    T encode(Frame frame) throws IOException;
}
