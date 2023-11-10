package org.noear.socketd.transport.java_tcp;

import org.noear.socketd.exception.SocketdConnectionException;
import org.noear.socketd.transport.client.ClientConnectorBase;
import org.noear.socketd.transport.client.ClientHandshakeResult;
import org.noear.socketd.transport.core.Channel;
import org.noear.socketd.transport.core.ChannelInternal;
import org.noear.socketd.transport.core.Flag;
import org.noear.socketd.transport.core.Frame;
import org.noear.socketd.transport.core.internal.ChannelDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.*;

/**
 * Tcp-Bio 客户端连接器实现（支持 ssl）
 *
 * @author noear
 * @since 2.0
 */
public class TcpBioClientConnector extends ClientConnectorBase<TcpBioClient> {
    private static final Logger log = LoggerFactory.getLogger(TcpBioClientConnector.class);

    private Socket real;
    private ExecutorService serverExecutor;

    public TcpBioClientConnector(TcpBioClient client) {
        super(client);
    }

    @Override
    public ChannelInternal connect() throws Exception {
        log.debug("Start connecting to: {}", client.config().getUrl());

        //不要复用旧的对象
        serverExecutor = client.config().getExecutor();
        if (serverExecutor == null) {
            serverExecutor = Executors.newFixedThreadPool(client.config().getCoreThreads());
        }

        SocketAddress socketAddress = new InetSocketAddress(client.config().getHost(), client.config().getPort());

        //支持 ssl
        if (client.config().getSslContext() == null) {
            real = new Socket();
        } else {
            real = client.config().getSslContext().getSocketFactory().createSocket();
        }

        //闲置超时
        if (client.config().getIdleTimeout() > 0L) {
            //单位：毫秒
            real.setSoTimeout((int) client.config().getIdleTimeout());
        }

        if (client.config().getConnectTimeout() > 0) {
            real.connect(socketAddress, (int) client.config().getConnectTimeout());
        } else {
            real.connect(socketAddress);
        }

        CompletableFuture<ClientHandshakeResult> handshakeFuture = new CompletableFuture<>();

        try {
            ChannelInternal channel = new ChannelDefault<>(real, client.config(), client.assistant());

            serverExecutor.submit(() -> {
                receive(channel, real, handshakeFuture);
            });

            channel.sendConnect(client.config().getUrl());
        } catch (Throwable e) {
            log.debug("{}", e);
            close();
        }

        try {
            ClientHandshakeResult handshakeResult = handshakeFuture.get(client.config().getConnectTimeout(), TimeUnit.MILLISECONDS);

            if (handshakeResult.getException() != null) {
                throw handshakeResult.getException();
            } else {
                return handshakeResult.getChannel();
            }
        } catch (TimeoutException e) {
            close();
            throw new SocketdConnectionException("Connection timeout: " + client.config().getUrl());
        } catch (Exception e) {
            close();
            throw e;
        }
    }

    private void receive(ChannelInternal channel, Socket socket, CompletableFuture<ClientHandshakeResult> handshakeFuture) {
        while (true) {
            try {
                if (socket.isClosed()) {
                    client.processor().onClose(channel);
                    break;
                }

                Frame frame = client.assistant().read(socket);
                if (frame != null) {
                    client.processor().onReceive(channel, frame);

                    if (frame.getFlag() == Flag.Connack) {
                        handshakeFuture.complete(new ClientHandshakeResult(channel, null));
                    }
                }
            } catch (Exception e) {
                if (e instanceof SocketdConnectionException) {
                    //说明握手失败了
                    handshakeFuture.complete(new ClientHandshakeResult(channel, e));
                    break;
                }

                client.processor().onError(channel, e);

                if (e instanceof SocketException) {
                    break;
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (real == null) {
            return;
        }

        try {
            real.close();
            serverExecutor.shutdown();
        } catch (Throwable e) {
            log.debug("{}", e);
        }
    }
}