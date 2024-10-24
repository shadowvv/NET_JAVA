package org.drop.net.core.kcp.message;

import org.drop.net.core.kcp.KcpUtils;
import io.netty.buffer.ByteBuf;

public class KcpShakeMessage extends KcpBaseMessage {

    public KcpShakeMessage() {
        super(0, KcpUtils.KCP_CMD_SHAKE);

        System.out.println("send KCP_CMD_SHAKE_START");
    }

    @Override
    protected void encode0(ByteBuf buffer) {

    }

    @Override
    protected void decode0(ByteBuf buffer) {

    }
}