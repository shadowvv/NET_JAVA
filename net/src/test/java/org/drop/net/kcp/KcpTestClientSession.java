package org.drop.net.kcp;

import core.KCPContext;
import org.drop.net.core.kcp.IKcpCoder;
import org.drop.net.core.kcp.KcpClientSession;
import org.drop.net.core.kcp.message.KcpCommonMessage;

public class KcpTestClientSession extends KcpClientSession<KcpCommonMessage> {

    public KcpTestClientSession(IKcpCoder<KcpCommonMessage> coder, int buffSize) {
        super(coder, buffSize);
    }

    @Override
    public void onReceiveMessage(KcpCommonMessage message) {

    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void writeLog(String s, KCPContext kcpContext, Object o) {
        System.out.println(s);
    }
}
