package org.drop.net.core.kcp;

import core.KCPContext;
import org.drop.net.core.kcp.message.KcpCommonMessage;
import org.drop.net.core.kcp.message.KcpConnectedMessage;
import org.drop.net.core.kcp.message.KcpHeartBeatMessage;
import org.drop.net.core.kcp.message.KcpShakeMessage;
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
import java.util.concurrent.*;

public abstract class KcpClientSession<T extends KcpCommonMessage> {

    private KcpSession kcpSession;
    private InetSocketAddress serverAddress;
    private Channel channel;
    private KcpSessionStatus status;
    private final IKcpCoder<T> coder;
    private final int buffSize;

    private long lastShakeTime;
    private int resentShakeCount;
    private long lastHeartBeatTime;

    public KcpClientSession(IKcpCoder<T> coder,int buffSize) {
        this.coder = coder;
        this.buffSize = buffSize;
        this.status = KcpSessionStatus.NEW;
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

            this.sendShakeMessage();

            ScheduledExecutorService service1 = Executors.newSingleThreadScheduledExecutor();
            Future<Boolean> future = service1.submit(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    while (status == KcpSessionStatus.ACTIVE) {
                        return true;
                    }
                    return false;
                }
            });
            future.get();

            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            try {
                service.scheduleAtFixedRate(new Runnable() {
                    public void run() {
                        update();
                    }
                }, 0, 10, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            channel.closeFuture().await();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            group.shutdownGracefully();
        }
    }

    private void sendShakeMessage() {
        ByteBuf buf = Unpooled.buffer();
        KcpShakeMessage shake = new KcpShakeMessage();
        channel.writeAndFlush(new DatagramPacket(shake.encode(buf), serverAddress));
        this.status = KcpSessionStatus.SHAKE;
        this.lastShakeTime = System.currentTimeMillis();
        this.resentShakeCount++;
    }

    public void update(){
        long currentTime = System.currentTimeMillis();
        if (status == KcpSessionStatus.SHAKE && currentTime - lastShakeTime > KcpUtils.KCP_SEND_SHAKE_RETRY_INTERVAL) {
            if (this.resentShakeCount > KcpUtils.KCP_SEND_SHAKE_RETRY_COUNT) {
                onDisconnect();
            }else {
                sendShakeMessage();
            }
        }

        if (kcpSession != null && kcpSession.getStatus() == KcpSessionStatus.ACTIVE) {
            kcpSession.update(currentTime);

            if (currentTime - lastHeartBeatTime > KcpUtils.KCP_SEND_HEARTBEAT_INTERVAL) {
                sendHeartBeatMessage();
            }
        }
    }

    private void sendHeartBeatMessage() {
        //TODO:
        sendMessage((T) new KcpHeartBeatMessage(getSessionId()));
    }

    public void onShakeConfirm(int conversationId,int sessionId) {
        kcpSession = new ClientKcpSession<>(conversationId, sessionId,serverAddress, channel, this);
        kcpSession.init(buffSize);
        try {
            ByteBuf buf = Unpooled.buffer();
            KcpConnectedMessage connectedMessage = new KcpConnectedMessage(conversationId);
            channel.writeAndFlush(new DatagramPacket(connectedMessage.encode(buf), serverAddress));

            kcpSession.connected();
            this.status = KcpSessionStatus.ACTIVE;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean isKcpActive() {
        return this.status == KcpSessionStatus.ACTIVE;
    }

    public int getSessionId() {
        return kcpSession.getSessionId();
    }

    public void sendMessage(T message) {
        if (isKcpActive()) {
            ByteBuf buffer = coder.encode(message);
            kcpSession.send(buffer);
        }
    }

    public void receive(ByteBuf buffer) {
        if (isKcpActive()) {
            kcpSession.input(buffer);
            lastHeartBeatTime = System.currentTimeMillis();
        }
    }

    private void onReceiveMessage(ByteBuf buffer) {
        if (isKcpActive()) {
            int command = buffer.readInt();
            int sessionId = buffer.readInt();
            if (command == KcpUtils.KCP_CMD_COMMON) {
                T message = coder.decode(buffer);
                onReceiveMessage(message);
            }
        }
    }

    /**
     * 接收到消息，可以直接在net线程处理，推荐将消息投递到其他逻辑线程处理
     * @param message 消息
     */
    public abstract void onReceiveMessage(T message);

    /**
     *
     */
    public abstract void onDisconnect();

    public abstract void writeLog(String s, KCPContext kcpContext, Object o);

    private static class ClientKcpSession<T extends KcpCommonMessage> extends KcpSession {

        private final KcpClientSession<T> clientSession;

        public ClientKcpSession(int sessionId, int conversationId, InetSocketAddress remoteAddress, Channel channel, KcpClientSession<T> clientSession) {
            super(sessionId, conversationId, remoteAddress, channel);
            this.clientSession = clientSession;
        }

        @Override
        public void onDisconnect() {
            clientSession.onDisconnect();
        }

        @Override
        public void onReceiveMessage(ByteBuf buffer) {
            clientSession.onReceiveMessage(buffer);
        }

        @Override
        public void writeLog(String s, KCPContext kcpContext, Object o) {
            clientSession.writeLog(s,kcpContext,o);
        }
    }

}
