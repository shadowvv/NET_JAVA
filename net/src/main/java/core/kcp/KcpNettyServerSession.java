package core.kcp;

import core.kcp.message.KcpBaseMessage;
import core.kcp.message.KcpCommonMessage;
import core.kcp.message.KcpConnectedMessage;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class KcpNettyServerSession<T extends KcpCommonMessage> {

    private Channel serverChannel;
    private final int workThreadNum;
    private final ConcurrentHashMap<String,KcpServerClientSession<T>> shakeClients;
    private final ConcurrentHashMap<Integer,KcpNettyServerClientRunner> clientRunners;

    private final IKcpCoder<T> coder;

    public KcpNettyServerSession(IKcpCoder<T> coder,int workThreadNum) {
        this.coder = coder;
        this.workThreadNum = workThreadNum;
        this.shakeClients = new ConcurrentHashMap<>();
        this.clientRunners = new ConcurrentHashMap<>();
        for (int i = 0; i < workThreadNum; i++) {
            clientRunners.put(i, new KcpNettyServerClientRunner());
        }
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

            for (int i = 0; i < workThreadNum; i++) {
                KcpNettyServerClientRunner runner = clientRunners.get(i);
                ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
                try {
                    service.scheduleAtFixedRate(runner, 0, 10, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                } finally {
//                    service.shutdown();
                }
            }

            serverChannel.closeFuture().await();
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            group.shutdownGracefully();
        }
    }

    public void onClientShake(InetSocketAddress senderAddr) {
        if (shakeClients.containsKey(senderAddr.getAddress().getHostAddress())){
            return;
        }
        int sessionId = SessionIdCreator.getNextSessionId();
        KcpServerClientSession<T> clientSession = new KcpServerClientSession<>(sessionId,senderAddr,serverChannel);
        shakeClients.put(senderAddr.getAddress().getHostAddress(),clientSession);

        ByteBuf buf = Unpooled.buffer();
        KcpShakeConfirmMessage shakeConfirmMessage = new KcpShakeConfirmMessage(sessionId);
        try {
            serverChannel.writeAndFlush(new DatagramPacket(shakeConfirmMessage.encode(buf), senderAddr)).sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void onClientConnected(InetSocketAddress senderAddr, int newConversationId) {
        if (!shakeClients.containsKey(senderAddr.getAddress().getHostAddress())){
            return;
        }

        int index = newConversationId % workThreadNum;
        KcpServerClientSession<T> clientSession = shakeClients.get(senderAddr.getAddress().getHostAddress());
        if (newConversationId != clientSession.getSessionId()){
            clientRunners.get(index).unregisterSession(clientSession.getSessionId());
            return;
        }

        if (!clientRunners.containsKey(index)) {
            clientRunners.put(index, new KcpNettyServerClientRunner());
        }
        clientRunners.get(index).registerSession(newConversationId,clientSession);
        shakeClients.remove(senderAddr.getAddress().getHostAddress());
    }

    public void receive(int sessionId, ByteBuf buffer) {
        int index = sessionId % workThreadNum;
        if (clientRunners.containsKey(index)) {
           clientRunners.get(index).receive(sessionId, buffer);
        }else {
            System.out.println(clientRunners.get(index).toString());
        }
    }

    public abstract void dispatchSessionMessage(int sessionId, T message);
}
