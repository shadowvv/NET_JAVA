package core.kcp.message;

import core.kcp.KcpUtils;

public abstract class KcpCommonMessage extends KcpBaseMessage {
    public KcpCommonMessage(int sessionId) {
        super(sessionId, KcpUtils.KCP_CMD_COMMON);

        System.out.println("send KCP_CMD_COMMON:"+sessionId);
    }
}
