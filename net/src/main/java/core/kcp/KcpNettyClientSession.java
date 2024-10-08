package core.kcp;

import core.KCPContext;
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

public abstract class KcpNettyClientSession<T> extends KcpSession {

    private InetSocketAddress addr;
    private Channel channel;
    private IKcpCoder<T> coder;

    public KcpNettyClientSession(IKcpCoder<T> coder) {
        super(0);
        this.coder = coder;
    }

    public void connect(String host, int port) {
        this.addr = new InetSocketAddress(host,port);
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
            channel.writeAndFlush(new DatagramPacket(shake.encode(buf), addr)).sync();

            channel.closeFuture().await();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    @Override
    public void start() {
        try {
            ByteBuf buf = Unpooled.buffer();
            KcpConnectedMessage shake = new KcpConnectedMessage(getSessionId());
            channel.writeAndFlush(new DatagramPacket(shake.encode(buf), addr)).sync();

            buf = Unpooled.buffer();
            KcpCommonMessage message = new KcpCommonMessage(getSessionId(),"test".getBytes());
            this.send(message.encode(buf));
//            channel.writeAndFlush(new DatagramPacket(message.encode(buf), addr)).sync();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor()) {
            service.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    System.out.println("update");
                    update(System.currentTimeMillis());
                }
            }, 0, 10, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceiveMessage(ByteBuf buffer) {
        T message = coder.decode(buffer);
        onReceiveMessage(message);
    }

    public abstract void onReceiveMessage(T message);

    @Override
    public int output(byte[] bytes, int i, KCPContext kcpContext, Object o) {
        try {
            ByteBuf buf = Unpooled.copiedBuffer(bytes);
            channel.writeAndFlush(new DatagramPacket(buf, addr)).sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
