package core.kcp;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.ConcurrentHashMap;

public class KcpServerClientRunner implements Runnable {

    private final ConcurrentHashMap<Integer,KcpServerClientSession> clientSessions;

    public KcpServerClientRunner() {
        clientSessions = new ConcurrentHashMap<>();
    }

    public void registerSession(int sessionId, KcpServerClientSession session) {
        clientSessions.put(sessionId, session);
    }

    public void unregisterSession(int sessionId) {
        clientSessions.remove(sessionId);
    }

    @Override
    public void run() {
        long current = System.currentTimeMillis();
        for (KcpServerClientSession session : clientSessions.values()) {
            session.update(current);
        }
    }

    public void input(int sessionId, ByteBuf buf) {
        KcpServerClientSession clientSession = clientSessions.get(sessionId);
        if (clientSession != null) {
            clientSession.input(buf);
        }
    }
}
