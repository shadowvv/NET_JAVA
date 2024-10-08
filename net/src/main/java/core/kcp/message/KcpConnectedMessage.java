package core.kcp.message;

import core.kcp.KcpUtils;

public class KcpConnectedMessage extends KcpBaseMessage {
    public KcpConnectedMessage(int sessionId) {
        super(sessionId, KcpUtils.KCP_CMD_CONNECTED, new byte[0]);
    }
}
