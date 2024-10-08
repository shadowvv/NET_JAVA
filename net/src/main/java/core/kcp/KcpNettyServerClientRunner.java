package core.kcp;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.ConcurrentHashMap;

public class KcpNettyServerClientRunner implements Runnable {

    private final ConcurrentHashMap<Integer,KcpNettyServerClientSession> clientSessions;

    public KcpNettyServerClientRunner() {
        clientSessions = new ConcurrentHashMap<>();
    }

    public void registerSession(int sessionId, KcpNettyServerClientSession session) {
        clientSessions.put(sessionId, session);
    }

    public void unregisterSession(int sessionId) {
        clientSessions.remove(sessionId);
    }

    @Override
    public void run() {
        long current = System.currentTimeMillis();
        for (KcpNettyServerClientSession session : clientSessions.values()) {
            session.update(current);
        }
    }

    public void receive(int sessionId, ByteBuf buf) {
        KcpNettyServerClientSession clientSession = clientSessions.get(sessionId);
        if (clientSession != null) {
            clientSession.receive(sessionId, buf);
        }
    }
}
