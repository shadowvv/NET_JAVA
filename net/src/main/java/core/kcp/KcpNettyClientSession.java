package core.kcp;

import core.kcp.message.KcpBaseMessage;
import core.kcp.message.KcpCommonMessage;
import core.kcp.message.KcpConnectedMessage;
import core.kcp.message.KcpShakeMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class KcpNettyClientSession<T extends KcpCommonMessage> {

    private KcpSession kcpSession;
    private InetSocketAddress serverAddress;
    private Channel channel;
    private boolean isKcpActive;
    private final IKcpCoder<T> coder;

    public KcpNettyClientSession(IKcpCoder<T> coder) {
        this.coder = coder;
        this.isKcpActive = false;
    }

    public void connect(String host, int port) {
        this.serverAddress = new InetSocketAddress(host,port);
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new KcpClientHandler(this));

            channel = b.bind(0).sync().channel();

            ByteBuf buf = Unpooled.buffer();
            KcpShakeMessage shake = new KcpShakeMessage();
            channel.writeAndFlush(new DatagramPacket(shake.encode(buf), serverAddress)).sync();

            channel.closeFuture().await();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            group.shutdownGracefully();
        }
    }

    public void onShakeConfirm(int newConversationId) {
        kcpSession = new ClientKcpSession<>(newConversationId, serverAddress, channel, this);
        try {
            ByteBuf buf = Unpooled.buffer();
            KcpConnectedMessage connectedMessage = new KcpConnectedMessage(newConversationId);
            channel.writeAndFlush(new DatagramPacket(connectedMessage.encode(buf), serverAddress)).sync();
            this.isKcpActive = true;

            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            try {
                service.scheduleAtFixedRate(new Runnable() {
                    public void run() {
                        kcpSession.update(System.currentTimeMillis());
                    }
                }, 0, 10, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean isKcpActive() {
        return isKcpActive;
    }

    public int getSessionId() {
        return kcpSession.getSessionId();
    }

    public void sendMessage(T message) {
        if (isKcpActive) {
            ByteBuf buffer = coder.encode(message);
            kcpSession.send(buffer);
        }
    }

    public void receive(ByteBuf buffer) {
        kcpSession.receive(buffer);
    }

    private void onReceiveMessage(ByteBuf buffer) {
        int command = buffer.readInt();
        int sessionId = buffer.readInt();
        if (command == KcpUtils.KCP_CMD_COMMON) {
            T message = coder.decode(buffer);
            onReceiveMessage(message);

            System.out.println("client KCP_CMD_COMMON");
        }
    }

    public abstract void onReceiveMessage(T message);

    private static class ClientKcpSession<T extends KcpCommonMessage> extends KcpSession {

        private final KcpNettyClientSession<T> clientSession;

        public ClientKcpSession(int sessionId, InetSocketAddress remoteAddress, Channel channel,KcpNettyClientSession<T> clientSession) {
            super(sessionId, remoteAddress, channel);
            this.clientSession = clientSession;
        }

        @Override
        public void onReceiveMessage(ByteBuf buffer) {
            clientSession.onReceiveMessage(buffer);
        }
    }

}
