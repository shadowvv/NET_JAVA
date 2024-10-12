package core.kcp;

import core.kcp.message.KcpBaseMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

class KcpServerClientSession<T extends KcpBaseMessage> extends KcpSession{

    public KcpServerClientSession(int sessionId, InetSocketAddress remoteAddress, Channel channel) {
        super(sessionId, remoteAddress, channel);
    }

    @Override
    public void onReceiveMessage(ByteBuf buffer) {

    }
}
