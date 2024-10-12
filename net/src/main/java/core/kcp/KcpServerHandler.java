package core.kcp;

import core.kcp.message.KcpBaseMessage;
import core.kcp.message.KcpCommonMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public class KcpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final KcpNettyServerSession<?> serverSession;

    public <T extends KcpCommonMessage> KcpServerHandler(KcpNettyServerSession<T> nettyKcpClientSession) {
        this.serverSession = nettyKcpClientSession;
    }

    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf buffer = msg.content().asReadOnly();
        int conversationId = buffer.readInt();
        if (conversationId == 0){
            int command = buffer.readInt();
            if (command == KcpUtils.KCP_CMD_SHAKE){
                serverSession.onClientShake(msg.sender());

                System.out.println("server KCP_CMD_SHAKE");
            }else if (command == KcpUtils.KCP_CMD_CONNECTED){
                int newConversationId = buffer.readInt();
                serverSession.onClientConnected(msg.sender(),newConversationId);

                System.out.println("server KCP_CMD_CONNECTED");
            }
        }else {
            buffer.resetReaderIndex();
            System.out.println("server receive:"+conversationId);
            serverSession.receive(conversationId,buffer);
        }
    }
}
