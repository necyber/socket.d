package org.noear.socketd.transport.core;

import org.noear.socketd.transport.core.impl.ConfigBase;

/**
 * 基础配置实现
 *
 * @author noear
 * @since 2.0
 */
public class ConfigDefault extends ConfigBase<ConfigDefault> {

    public ConfigDefault(boolean clientMode) {
        super(clientMode);
    }
}
