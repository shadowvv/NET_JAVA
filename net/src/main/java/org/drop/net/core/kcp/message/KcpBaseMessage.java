package org.drop.net.core.kcp.message;

import io.netty.buffer.ByteBuf;

public abstract class KcpBaseMessage {

    private int conversationId;
    private int command;

    public KcpBaseMessage(){

    }

    public KcpBaseMessage(int conversationId, int command) {
        this.conversationId = conversationId;
        this.command = command;
    }

    public ByteBuf encode(ByteBuf buffer) {
        buffer.writeInt(conversationId);
        buffer.writeInt(command);
        encode0(buffer);
        return buffer;
    }

    protected abstract void encode0(ByteBuf buffer);

    public void decode(ByteBuf buffer){
        conversationId = buffer.readInt();
        command = buffer.readInt();
        decode0(buffer);
    }

    protected abstract void decode0(ByteBuf buffer);

    public int getConversationId() {
        return conversationId;
    }

    public int getCommand() {
        return command;
    }
}
