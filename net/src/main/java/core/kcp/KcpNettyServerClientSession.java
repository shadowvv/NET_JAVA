package core.kcp;

import core.KCPContext;
import io.netty.channel.Channel;

public abstract class KcpNettyServerClientSession extends KcpSession {

    private Channel clientChannel;

    public KcpNettyServerClientSession(int sessionId) {
        super(sessionId);
    }

    @Override
    public void start() {

    }

    @Override
    public int output(byte[] bytes, int i, KCPContext kcpContext, Object o) {
//        try {
//            ByteBuf buf = Unpooled.copiedBuffer(bytes);
//            clientChannel.writeAndFlush(new DatagramPacket(buf, addr)).sync();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        return 0;
    }
}
