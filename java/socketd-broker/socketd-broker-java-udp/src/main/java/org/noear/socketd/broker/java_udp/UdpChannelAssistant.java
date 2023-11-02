package org.noear.socketd.broker.java_udp;

import org.noear.socketd.broker.java_udp.impl.DatagramFrame;
import org.noear.socketd.broker.java_udp.impl.DatagramTagert;
import org.noear.socketd.core.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Udp 交换器实现（它没法固定接口，但可以固定输出目录）
 *
 * @author Urara
 * @since 2.0
 */
public class UdpChannelAssistant implements ChannelAssistant<DatagramTagert> {
    private final Config config;

    public UdpChannelAssistant(Config config) {
        this.config = config;
    }

    /**
     * 读取
     */
    public DatagramFrame read(DatagramSocket source) throws IOException {
        //获取长度
        DatagramPacket datagramPacket = new DatagramPacket(new byte[Integer.BYTES], Integer.BYTES);
        source.receive(datagramPacket);
        byte[] sizeBytes = datagramPacket.getData();
        if (sizeBytes == null || sizeBytes.length == 0) {
            return null;
        }

        //获取数据（接着在原地址上拿）
        int size = ByteBuffer.wrap(sizeBytes).getInt();
        datagramPacket = new DatagramPacket(new byte[size], size, datagramPacket.getSocketAddress());
        source.receive(datagramPacket);
        byte[] dataBytes = datagramPacket.getData();
        if (dataBytes == null || dataBytes.length == 0) {
            return null;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(dataBytes);
        Frame frame = config.getCodec().decode(byteBuffer);

        return new DatagramFrame(datagramPacket, frame);
    }

    /**
     * 写入
     */
    @Override
    public void write(DatagramTagert target, Frame frame) throws IOException {
        byte[] dataBytes = config.getCodec().encode(frame).array();

        byte[] sizeBytes = ByteBuffer.allocate(Integer.BYTES).putInt(dataBytes.length).array();

        //先发长度包
        target.send(sizeBytes);
        //再发数据包
        target.send(dataBytes);
    }

    @Override
    public boolean isValid(DatagramTagert target) {
        return true;
    }

    @Override
    public void close(DatagramTagert target) throws IOException {
        target.close();
    }

    @Override
    public InetSocketAddress getRemoteAddress(DatagramTagert target) throws IOException {
        return target.getRemoteAddress();
    }

    @Override
    public InetSocketAddress getLocalAddress(DatagramTagert target) throws IOException {
        return target.getLocalAddress();
    }
}
