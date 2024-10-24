package org.drop.net.core.kcp;

import core.IKCPContext;
import core.KCPContext;
import core.KCPSegment;
import core.KCPUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public abstract class KcpSession implements IKCPContext {

    private final int sessionId;
    private int conversationId;
    private InetSocketAddress remoteAddress;
    private Channel channel;
    private KcpSessionStatus status;

    private KCPContext kcpContext;
    private ByteBuffer buffer;

    public KcpSession(int sessionId, int conversationId, InetSocketAddress remoteAddress, Channel channel) {
        this.sessionId = sessionId;
        this.conversationId = conversationId;
        this.remoteAddress = remoteAddress;
        this.channel = channel;
        this.status = KcpSessionStatus.NEW;
    }

    //TODO:move config to xml
    public void init(int receiveBuffSize){
        this.buffer = ByteBuffer.allocate(receiveBuffSize);

        this.kcpContext = new KCPContext(conversationId, sessionId, this);
        kcpContext.setRemoteWindow(KCPUtils.KCP_WND_RCV);
        kcpContext.setMTU(KCPUtils.KCP_MTU_DEF);
        kcpContext.setMSS(kcpContext.getMTU() - KCPSegment.KCP_OVERHEAD);
        kcpContext.setIsStream(false);

        kcpContext.setBuffer(ByteBuffer.allocate((kcpContext.getMTU() + KCPSegment.KCP_OVERHEAD) * 3));
        kcpContext.setCurrentRTO(KCPUtils.KCP_RTO_DEF);
        kcpContext.setMinRto(KCPUtils.KCP_RTO_MIN);
        kcpContext.setInterval(KCPUtils.KCP_INTERVAL);
        kcpContext.setNextFlushTimeStamp(KCPUtils.KCP_INTERVAL);
        kcpContext.setSlowStartThresh(KCPUtils.KCP_THRESH_INIT);
        kcpContext.setFastLimit(KCPUtils.KCP_FAST_ACK_LIMIT);
        kcpContext.setDeadLink(KCPUtils.KCP_DEAD_LINK);
        kcpContext.setWindowSize(128, 128);
        kcpContext.setNoDelay(0, 10, 0, false);
        status = KcpSessionStatus.INITIALIZED;
    }

    public void connected(){
        status = KcpSessionStatus.ACTIVE;
    }

    public void reconnect(int conversationId,InetSocketAddress remoteAddress, Channel channel) {
        this.conversationId = conversationId;
        this.kcpContext.setConversationId(conversationId);
        this.remoteAddress = remoteAddress;
        this.channel = channel;
        this.status = KcpSessionStatus.ACTIVE;
        //TODO 重连kcpContext怎么操作
    }

    public void disconnect() {
        this.status = KcpSessionStatus.INACTIVE;
        //TODO 主动离线操作
    }

    public void send(ByteBuf buf) {
        if (status == KcpSessionStatus.ACTIVE) {
            kcpContext.send(buf.nioBuffer(), buf.readableBytes());
        }
    }

    public void input(ByteBuf buf) {
        if (status == KcpSessionStatus.ACTIVE) {
            kcpContext.input(buf.nioBuffer(), buf.readableBytes());
        }
    }

    public void update(long current) {
        if (status == KcpSessionStatus.ACTIVE) {
            long nextUpdateTime = kcpContext.check(current);
            if (nextUpdateTime <= current) {
                kcpContext.update(current);
            }

            int kcpState = kcpContext.getState();
            if (kcpState == -1){
                onDisconnect();
                status = KcpSessionStatus.INACTIVE;
            }

            int length = kcpContext.receive(buffer, 4096);
            if (length > 0) {
                buffer.flip();
                ByteBuf buf = Unpooled.copiedBuffer(buffer);
                onReceiveMessage(buf);
            }
            this.buffer.clear();
        }
    }

    @Override
    public int output(byte[] bytes, int i, KCPContext kcpContext, Object user) {
        if (status == KcpSessionStatus.ACTIVE) {
            ByteBuf buf = Unpooled.copiedBuffer(bytes);
            channel.writeAndFlush(new DatagramPacket(buf, remoteAddress));
            return bytes.length;
        }
        return 0;
    }

    public KcpSessionStatus getStatus() {
        return status;
    }

    public int getSessionId() {
        return sessionId;
    }

    public abstract void onDisconnect();

    public abstract void onReceiveMessage(ByteBuf buffer);
}
