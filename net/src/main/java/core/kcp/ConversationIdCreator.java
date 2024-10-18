package core.kcp;

import java.util.concurrent.atomic.AtomicInteger;

public class ConversationIdCreator {

    private static final AtomicInteger conversationId = new AtomicInteger(0);

    public static int getConversationId() {
        return conversationId.incrementAndGet();
    }

}
