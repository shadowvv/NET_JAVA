package core.kcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public class KcpClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final KcpNettyClientSession<?> clientSession;

    public <T> KcpClientHandler(KcpNettyClientSession<T> nettyKcpClientSession) {
        this.clientSession = nettyKcpClientSession;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf buffer = msg.content().asReadOnly();
        int command = buffer.readInt();
        int sessionId = buffer.readInt();
        switch (command) {
            case KcpUtils.KCP_CMD_SHAKE_CONFIRM:
                clientSession.setSessionId(sessionId);
                clientSession.start();
                break;
            case KcpUtils.KCP_CMD_COMMON:
                clientSession.receive(sessionId, buffer);
                break;
            default:
                break;
        }
    }
}
