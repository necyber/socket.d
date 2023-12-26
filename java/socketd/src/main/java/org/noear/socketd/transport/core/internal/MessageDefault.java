package org.noear.socketd.transport.core.internal;

import org.noear.socketd.transport.core.*;

import java.nio.ByteBuffer;
import java.util.Map;


/**
 * 消息默认实现（帧[消息[实体]]）
 *
 * @author noear
 * @since 2.0
 */
public class MessageDefault implements MessageInternal {
    private String sid = Constants.DEF_SID;
    private String event = Constants.DEF_EVENT;
    private Entity entity = null;

    private int flag = Flags.Unknown;

    /**
     * 设置标记
     */
    public MessageDefault flagSet(int flag) {
        this.flag = flag;
        return this;
    }

    /**
     * 设置流id
     */
    public MessageDefault sidSet(String sid) {
        this.sid = sid;
        return this;
    }

    /**
     * 设置事件
     */
    public MessageDefault eventSet(String event) {
        this.event = event;
        return this;
    }

    /**
     * 设置实体
     */
    public MessageDefault entitySet(Entity entity) {
        this.entity = entity;

        return this;
    }

    /**
     * 获取标记
     */
    @Override
    public int flag() {
        return flag;
    }

    /**
     * 是否答复结束
     * */
    @Override
    public boolean isEnd() {
        return flag == Flags.ReplyEnd;
    }

    /**
     * 是否为请求
     */
    @Override
    public boolean isRequest() {
        return flag == Flags.Request;
    }

    /**
     * 是否为订阅
     */
    @Override
    public boolean isSubscribe() {
        return flag == Flags.Subscribe;
    }

    /**
     * 获取消息流Id（用于消息交互、分片）
     */
    @Override
    public String sid() {
        return sid;
    }

    /**
     * 获取消息事件
     */
    @Override
    public String event() {
        return event;
    }

    /**
     * 获取消息实体
     */
    @Override
    public Entity entity() {
        return entity;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sid='" + sid + '\'' +
                ", event='" + event + '\'' +
                ", entity=" + entity +
                '}';
    }

    @Override
    public String metaString() {
        return entity.metaString();
    }

    @Override
    public Map<String, String> metaMap() {
        return entity.metaMap();
    }

    @Override
    public String meta(String name) {
        return entity.meta(name);
    }

    @Override
    public String metaOrDefault(String name, String def) {
        return entity.metaOrDefault(name, def);
    }

    @Override
    public void putMeta(String name, String val) {
        entity.putMeta(name, val);
    }

    @Override
    public ByteBuffer data() {
        return entity.data();
    }

    @Override
    public String dataAsString() {
        return entity.dataAsString();
    }

    @Override
    public byte[] dataAsBytes() {
        return entity.dataAsBytes();
    }

    @Override
    public int dataSize() {
        return entity.dataSize();
    }

    @Override
    public void release() {
        entity.release();
    }
}