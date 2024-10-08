package core.kcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public class KcpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final KcpNettyServerSession<?> serverSession;

    public <T> KcpServerHandler(KcpNettyServerSession<T> nettyKcpClientSession) {
        this.serverSession = nettyKcpClientSession;
    }

    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf buffer = msg.content().asReadOnly();
        int command = buffer.readInt();
        int sessionId = buffer.readInt();
        switch (command) {
            case KcpUtils.KCP_CMD_SHAKE_START:
                serverSession.acceptChannel(ctx.channel(),msg.sender());
                break;
            case KcpUtils.KCP_CMD_CONNECTED:
                serverSession.start();
                break;
            case KcpUtils.KCP_CMD_COMMON:
                serverSession.receive(sessionId,buffer);
                break;
            default:
                break;
        }
    }
}
