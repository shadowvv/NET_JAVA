package org.drop.net.kcp;

import org.drop.net.core.kcp.message.KcpCommonMessage;
import io.netty.buffer.ByteBuf;

public class KcpTestLogicMessage extends KcpCommonMessage {

    public KcpTestLogicMessage(int sessionId){
        super(sessionId);
    }

    @Override
    protected void encode0(ByteBuf buffer) {

    }

    @Override
    protected void decode0(ByteBuf buffer) {

    }
}
