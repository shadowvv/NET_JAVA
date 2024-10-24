package org.drop.net.core.kcp;

import core.KCPContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

public class KcpServerClientSession extends KcpSession{

    public KcpServerClientSession(int sessionId, int conversationId, InetSocketAddress remoteAddress, Channel channel) {
        super(sessionId, conversationId, remoteAddress, channel);
    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onReceiveMessage(ByteBuf buffer) {

    }

    @Override
    public void writeLog(String s, KCPContext kcpContext, Object o) {

    }
}
