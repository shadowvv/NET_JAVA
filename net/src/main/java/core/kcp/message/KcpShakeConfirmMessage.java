package core.kcp.message;

import core.kcp.KcpUtils;

public class KcpShakeConfirmMessage extends KcpBaseMessage {

    public KcpShakeConfirmMessage(int sessionId) {
        super(sessionId, KcpUtils.KCP_CMD_SHAKE_CONFIRM, new byte[0]);
    }
}
