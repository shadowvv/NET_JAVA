package core.kcp.message;

import core.kcp.KcpUtils;

public class KcpCommonMessage extends KcpBaseMessage {
    public KcpCommonMessage(int sessionId, byte[] data) {
        super(sessionId, KcpUtils.KCP_CMD_COMMON, data);
    }
}
