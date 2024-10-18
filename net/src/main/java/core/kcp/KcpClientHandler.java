package core.kcp;

import core.kcp.message.KcpCommonMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public class KcpClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final KcpClientSession<?> clientSession;

    public <T extends KcpCommonMessage> KcpClientHandler(KcpClientSession<T> nettyKcpClientSession) {
        this.clientSession = nettyKcpClientSession;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf buffer = msg.content().asReadOnly();
        int conversationId = buffer.readInt();
        if (conversationId == 0){
            int command = buffer.readInt();
            if (command == KcpUtils.KCP_CMD_SHAKE_CONFIRM){
                int newConversationId = buffer.readInt();
                int sessionId = buffer.readInt();
                clientSession.onShakeConfirm(newConversationId,sessionId);
            }
        }else {
            buffer.resetReaderIndex();
            clientSession.receive(buffer);
        }
    }
}
