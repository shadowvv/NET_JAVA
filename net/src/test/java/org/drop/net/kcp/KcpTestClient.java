package org.drop.net.kcp;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KcpTestClient {

    KcpTestCoder coder;
    KcpTestClientSession session;

    public KcpTestClient() {

    }

    public void connect(String host, int port) {
        coder = new KcpTestCoder();
        session = new KcpTestClientSession(coder,4096);
        session.connect(host,port);
    }

    public void sendTestPackage() {
        session.sendMessage(new KcpTestLogicMessage(session.getSessionId()));
    }

    public boolean isConnected() {
        if (session == null) return false;
        return session.isKcpActive();
    }


    public static void main(String[] args) {
        KcpTestClient client = new KcpTestClient();

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
