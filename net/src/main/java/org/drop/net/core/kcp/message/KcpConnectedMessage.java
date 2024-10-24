package org.drop.net.core.kcp.message;

import org.drop.net.core.kcp.KcpUtils;
import io.netty.buffer.ByteBuf;

public class KcpConnectedMessage extends KcpBaseMessage {

    private int kcpConversionId;

    public KcpConnectedMessage(int kcpConversionId) {
        super(0, KcpUtils.KCP_CMD_CONNECTED);
        this.kcpConversionId = kcpConversionId;

        System.out.println("send KCP_CMD_CONNECTED");
    }

    public int getKcpConversionId() {
        return kcpConversionId;
    }

    @Override
    protected void encode0(ByteBuf buffer) {
        buffer.writeInt(kcpConversionId);
    }

    @Override
    protected void decode0(ByteBuf buffer) {
        kcpConversionId = buffer.readInt();
    }
}
