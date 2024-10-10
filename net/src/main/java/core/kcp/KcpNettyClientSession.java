package core.kcp;

import core.KCPContext;
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

public abstract class KcpNettyClientSession<T> extends KcpSession {

    private InetSocketAddress serverAddress;
    private Channel channel;

    private final IKcpCoder<T> coder;

    public KcpNettyClientSession(IKcpCoder<T> coder) {
        super(0);
        this.coder = coder;
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

            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            try {
                service.scheduleAtFixedRate(new Runnable() {
                    public void run() {
                        update(System.currentTimeMillis());
                    }
                }, 0, 10, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            ByteBuf buf = Unpooled.buffer();
            KcpShakeMessage shake = new KcpShakeMessage();
            this.send(shake.encode(buf));

            channel.closeFuture().await();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            group.shutdownGracefully();
        }
    }

    @Override
    public void onReceiveMessage(ByteBuf buffer) {
        int command = buffer.readInt();
        int sessionId = buffer.readInt();
        switch (command) {
            case KcpUtils.KCP_CMD_SHAKE_CONFIRM:
                setSessionId(sessionId);
                start();
                break;
            case KcpUtils.KCP_CMD_COMMON:
                T message = coder.decode(buffer);
                onReceiveMessage(message);
                break;
            default:
                break;
        }
    }

    @Override
    public void start() {
        try {
            ByteBuf buf = Unpooled.buffer();
            KcpConnectedMessage connectedMessage = new KcpConnectedMessage(getSessionId());
            this.send(connectedMessage.encode(buf));

//            buf = Unpooled.buffer();
//            KcpCommonMessage message = new KcpCommonMessage(getSessionId(),"test".getBytes());
//            this.send(message.encode(buf));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public abstract void onReceiveMessage(T message);

    @Override
    public int output(byte[] bytes, int i, KCPContext kcpContext, Object o) {
        try {
            ByteBuf buf = Unpooled.copiedBuffer(bytes);
            channel.writeAndFlush(new DatagramPacket(buf, serverAddress)).sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
