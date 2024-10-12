package core.kcp;

import core.kcp.message.KcpBaseMessage;
import io.netty.buffer.ByteBuf;

import java.util.concurrent.ConcurrentHashMap;

public class KcpNettyServerClientRunner implements Runnable {

    private final ConcurrentHashMap<Integer,KcpServerClientSession<? extends KcpBaseMessage>> clientSessions;

    public KcpNettyServerClientRunner() {
        clientSessions = new ConcurrentHashMap<>();
    }

    public void registerSession(int sessionId, KcpServerClientSession<? extends KcpBaseMessage> session) {
        clientSessions.put(sessionId, session);
    }

    public void unregisterSession(int sessionId) {
        clientSessions.remove(sessionId);
    }

    @Override
    public void run() {
        long current = System.currentTimeMillis();
        for (KcpServerClientSession<?> session : clientSessions.values()) {
            session.update(current);
        }
    }

    public void receive(int sessionId, ByteBuf buf) {
        KcpServerClientSession<?> clientSession = clientSessions.get(sessionId);
        if (clientSession != null) {
            clientSession.receive(buf);
        }
    }
}
