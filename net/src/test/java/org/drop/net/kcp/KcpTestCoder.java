package org.drop.net.kcp;

import io.netty.buffer.ByteBuf;
import org.drop.net.core.kcp.IKcpCoder;
import org.drop.net.core.kcp.message.KcpCommonMessage;

public class KcpTestCoder implements IKcpCoder<KcpCommonMessage> {
    @Override
    public ByteBuf encode(KcpCommonMessage message) {
        return null;
    }

    @Override
    public KcpCommonMessage decode(ByteBuf buffer) {
        return null;
    }
}
