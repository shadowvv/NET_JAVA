package core.kcp;

import core.KCPContext;
import core.kcp.message.KcpShakeConfirmMessage;
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
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class KcpNettyServerSession<T> extends KcpSession {

    private Channel serverChannel;
    private IKcpCoder<T> coder;
    private final int workThreadNum;
    private final HashMap<Integer,KcpNettyServerClientRunner> clientRunners;
    private final Class<? extends KcpNettyServerClientSession> clientSessionClass;

    public KcpNettyServerSession(IKcpCoder<T> coder,Class<? extends KcpNettyServerClientSession> serverClientSession,int workThreadNum) {
        super(0);
        this.coder = coder;
        this.workThreadNum = workThreadNum;
        clientRunners = new HashMap<>();
        for (int i = 0; i < workThreadNum; i++) {
            clientRunners.put(i, new KcpNettyServerClientRunner());
        }
        this.clientSessionClass = serverClientSession;
    }

    public void listen(int port){
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new KcpServerHandler(this));

            serverChannel = bootstrap.bind(port).sync().channel();
            serverChannel.closeFuture().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public void acceptChannel(Channel channel, InetSocketAddress socketAddress) throws InterruptedException {
        int sessionId = SessionIdCreator.getNextSessionId();
        try {
            int index = sessionId % workThreadNum;
            if (!clientRunners.containsKey(index)) {
                clientRunners.put(index, new KcpNettyServerClientRunner());
            }
            KcpNettyServerClientSession serverClientSessionClass = clientSessionClass.getDeclaredConstructor(int.class).newInstance(sessionId);
            clientRunners.get(index).registerSession(sessionId,serverClientSessionClass);
        }catch (Exception e){
            e.printStackTrace();
        }
        ByteBuf buf = Unpooled.buffer();
        KcpShakeConfirmMessage message = new KcpShakeConfirmMessage(sessionId);
        channel.writeAndFlush(new DatagramPacket(message.encode(buf), new InetSocketAddress(socketAddress.getHostName(),socketAddress.getPort()))).sync();
    }

    @Override
    public void receive(int sessionId, ByteBuf buf){
        int index = sessionId % workThreadNum;
        KcpNettyServerClientRunner runner = clientRunners.get(index);
        if (runner != null) {
            runner.receive(sessionId, buf);
        }
    }

    public abstract void dispatchSessionMessage(int sessionId, T message);

    @Override
    public void start() {
        for (int i = 0; i < workThreadNum; i++) {
            KcpNettyServerClientRunner runner = clientRunners.get(i);
            try (ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor()) {
                service.scheduleAtFixedRate(runner, 0, 10, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int output(byte[] bytes, int i, KCPContext kcpContext, Object o) {
//        try {
//            ByteBuf buf = Unpooled.copiedBuffer(bytes);
//            serverChannel.writeAndFlush(new DatagramPacket(buf, addr)).sync();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        return 0;
    }

    @Override
    public void writeLog(String s, KCPContext kcpContext, Object o) {

    }
}
