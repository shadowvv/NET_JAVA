package core.kcp.message;

import io.netty.buffer.ByteBuf;

public class KcpBaseMessage {

    private final int command;
    private final int sessionId;
    private final byte[] data;

    KcpBaseMessage(int sessionId, int command, byte[] data) {
        this.sessionId = sessionId;
        this.command = command;
        this.data = data;
    }

    public int getSessionId() {
        return sessionId;
    }

    public int getCommand() {
        return command;
    }

    public byte[] getData() {
        return data;
    }

    public ByteBuf encode(ByteBuf buffer) {
        buffer.writeInt(command);
        buffer.writeInt(sessionId);
        buffer.writeBytes(data);
        return buffer;
    }
}
