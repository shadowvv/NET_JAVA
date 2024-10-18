package core.kcp.message;

import io.netty.buffer.ByteBuf;

public class KcpHeartBeatMessage extends KcpCommonMessage{

    public KcpHeartBeatMessage(int sessionId) {
        super(sessionId);
    }

    @Override
    protected void encode0(ByteBuf buffer) {

    }

    @Override
    protected void decode0(ByteBuf buffer) {

    }
}
