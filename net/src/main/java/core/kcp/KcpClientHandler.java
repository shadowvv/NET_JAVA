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
        clientSession.receive(buffer);
    }
}
