package org.noear.socketd.protocol;

import java.io.IOException;

/**
 * @author noear 2023/10/14 created
 */
public interface OutputTarget<T>{

    void write(T source, Frame frame) throws IOException;
}