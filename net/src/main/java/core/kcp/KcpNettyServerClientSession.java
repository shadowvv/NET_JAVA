package core.kcp;

import core.KCPContext;
import core.kcp.message.KcpShakeConfirmMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

public abstract class KcpNettyServerClientSession<T> extends KcpSession {

    private final Channel serverChannel;
    private final InetSocketAddress clientAddress;
    private final IKcpCoder<T> coder;

    public KcpNettyServerClientSession(int sessionId,Channel serverChannel, InetSocketAddress clientAddress,IKcpCoder<T> coder) {
        super(sessionId);
        this.serverChannel = serverChannel;
        this.clientAddress = clientAddress;
        this.coder = coder;
    }

    @Override
    public void start() {
        ByteBuf buf = Unpooled.buffer();
        KcpShakeConfirmMessage message = new KcpShakeConfirmMessage(getSessionId());
        this.send(message.encode(buf));
    }

    @Override
    public void onReceiveMessage(ByteBuf buffer){
        int command = buffer.readInt();
        int sessionId = buffer.readInt();
        if (command == KcpUtils.KCP_CMD_COMMON) {
            T message = coder.decode(buffer);
            onReceiveMessage(message);
        }
    }

    public abstract void onReceiveMessage(T message);

    @Override
    public int output(byte[] bytes, int i, KCPContext kcpContext, Object o) {
        try {
            ByteBuf buf = Unpooled.copiedBuffer(bytes);
            serverChannel.writeAndFlush(new DatagramPacket(buf, clientAddress)).sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
