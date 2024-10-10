package core.kcp;

import core.KCPContext;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class KcpNettyServerSession<T> extends KcpSession {

    private Channel serverChannel;
    private final int workThreadNum;
    private final HashMap<Integer,KcpNettyServerClientRunner> clientRunners;
    private final Class<? extends KcpNettyServerClientSession<T>> clientSessionClass;

    private final IKcpCoder<T> coder;

    public KcpNettyServerSession(IKcpCoder<T> coder,Class<? extends KcpNettyServerClientSession<T>> serverClientSession,int workThreadNum) {
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

            serverChannel.closeFuture().await();
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            group.shutdownGracefully();
        }
    }

    public void receive(int sessionId, ByteBuf buffer, InetSocketAddress sender) {
        if (sessionId == 0){
            acceptChannel(sender);
        }else {
            int index = sessionId % workThreadNum;
            if (!clientRunners.containsKey(index)) {
               clientRunners.get(index).receive(sessionId, buffer);
            }else {
                System.out.println(clientRunners.get(index).toString());
            }
        }
    }

    @Override
    public void onReceiveMessage(ByteBuf buffer){
        int command = buffer.readInt();
        switch (command) {
            case KcpUtils.KCP_CMD_CONNECTED:
                start();
                break;
            case KcpUtils.KCP_CMD_COMMON:
                break;
            default:
                break;
        }
    }

    private void acceptChannel(InetSocketAddress clientAddress) {
        int sessionId = SessionIdCreator.getNextSessionId();
        try {
            int index = sessionId % workThreadNum;
            if (!clientRunners.containsKey(index)) {
                clientRunners.put(index, new KcpNettyServerClientRunner());
            }
            KcpNettyServerClientSession<T> serverClientSessionClass = clientSessionClass.getDeclaredConstructor(int.class,Channel.class,InetSocketAddress.class,IKcpCoder.class).newInstance(sessionId,serverChannel,clientAddress,coder);
            clientRunners.get(index).registerSession(sessionId,serverClientSessionClass);
            serverClientSessionClass.start();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void start() {
        for (int i = 0; i < workThreadNum; i++) {
            KcpNettyServerClientRunner runner = clientRunners.get(i);
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            try {
                service.scheduleAtFixedRate(runner, 0, 10, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                service.shutdown();
            }
        }
    }

    @Override
    public int output(byte[] bytes, int i, KCPContext kcpContext, Object o) {
        return 0;
    }

    @Override
    public void writeLog(String s, KCPContext kcpContext, Object o) {

    }

    public abstract void dispatchSessionMessage(int sessionId, T message);

}
