package org.noear.socketd.protocol.impl;

import org.noear.socketd.protocol.*;

import java.io.IOException;

/**
 * 处理器
 *
 * @author noear
 * @since 2.0
 */
public class ProcessorDefault implements Processor {
    private Listener listener;

    public ProcessorDefault(Listener listener) {
        this.listener = listener;
    }

    public void onReceive(Channel channel, Frame frame) throws IOException {
        if (frame.getFlag() == Flag.Connect) {
            //if server
            Payload payload = frame.getPayload();
            channel.setHandshaker(new Handshaker(payload));
            channel.sendConnack(); //->Connack

            onOpen(channel.getSession());
        } else if (frame.getFlag() == Flag.Connack) {
            //if client
            onOpen(channel.getSession());
        } else {
            if (channel.getHandshaker() == null) {
                channel.close();
                return;
            }

            try {
                switch (frame.getFlag()) {
                    case Ping: {
                        channel.sendPong();
                        break;
                    }
                    case Pong: {
                        break;
                    }
                    case Close: {
                        channel.close();
                        onClose(channel.getSession());
                        break;
                    }
                    case Message: {
                        onMessage(channel.getSession(), frame.getPayload());
                        break;
                    }
                    case Request: {
                        onMessage(channel.getSession(), frame.getPayload());
                        break;
                    }
                    case Subscribe: {
                        onMessage(channel.getSession(), frame.getPayload());
                        break;
                    }
                    default: {
                        channel.close();
                        onClose(channel.getSession());
                    }
                }
            } catch (Throwable e) {
                onError(channel.getSession(), e);
            }
        }
    }


    @Override
    public void onOpen(Session session) {
        listener.onOpen(session);
    }

    @Override
    public void onMessage(Session session, Payload message) throws IOException {
        listener.onMessage(session, message);
    }

    @Override
    public void onClose(Session session) {
        listener.onClose(session);
    }

    @Override
    public void onError(Session session, Throwable error) {
        listener.onError(session, error);
    }
}