package kcp;

import core.kcp.IKcpCoder;
import core.kcp.KcpNettyClientSession;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KcpNettyTestClient {

    KcpNettyClientSession<KcpTestLogicMessage> session;

    public KcpNettyTestClient() {

    }

    public void connect(String host, int port) {
         session = new KcpNettyClientSession<KcpTestLogicMessage>(new IKcpCoder<KcpTestLogicMessage>() {
            @Override
            public ByteBuf encode(KcpTestLogicMessage message) {
                ByteBuf buffer = Unpooled.buffer();
                message.encode(buffer);
                return buffer;
            }

            @Override
            public KcpTestLogicMessage decode(ByteBuf buffer) {
                return null;
            }
        }) {
            @Override
            public void onReceiveMessage(KcpTestLogicMessage message) {

            }
        };
        session.connect(host,port);
    }

    public void sendTestPackage() {
        session.sendMessage(new KcpTestLogicMessage(session.getSessionId()));
        System.out.println("sendTestPackage:"+session.getSessionId());
    }

    public boolean isConnected() {
        if (session == null) return false;
        return session.isKcpActive();
    }


    public static void main(String[] args) {
        KcpNettyTestClient client = new KcpNettyTestClient();

        final boolean[] send = {false};
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        try {
            service.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    if (client.isConnected() && !send[0]){
                        client.sendTestPackage();
                        send[0] = true;
                    }
                }
            }, 0, 10, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        client.connect("127.0.0.1",8800);
    }

}
