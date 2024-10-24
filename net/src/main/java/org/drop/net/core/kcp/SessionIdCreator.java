package org.drop.net.core.kcp;

import java.util.concurrent.atomic.AtomicInteger;

public class SessionIdCreator {

    private static final AtomicInteger sessionId = new AtomicInteger(0);

    public static int getNextSessionId() {
        return sessionId.incrementAndGet();
    }

}
