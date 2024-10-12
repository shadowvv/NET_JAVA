package core.kcp.message;

import core.kcp.KcpUtils;
import io.netty.buffer.ByteBuf;

public class KcpShakeConfirmMessage extends KcpBaseMessage {

    private int kcpConversionId;

    public KcpShakeConfirmMessage(int kcpConversionId) {
        super(0, KcpUtils.KCP_CMD_SHAKE_CONFIRM);
        this.kcpConversionId = kcpConversionId;

        System.out.println("send KCP_CMD_SHAKE_CONFIRM");
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
