package core.kcp.message;

import core.kcp.KcpUtils;

public class KcpShakeMessage extends KcpBaseMessage {

    public KcpShakeMessage() {
        super(0, KcpUtils.KCP_CMD_SHAKE_START, new byte[0]);
    }
}
