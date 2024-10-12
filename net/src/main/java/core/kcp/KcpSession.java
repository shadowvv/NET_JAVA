package core.kcp;

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

    private int sessionId;
    private InetSocketAddress remoteAddress;
    private Channel channel;

    private final KCPContext kcpContext;
    private final ByteBuffer buffer;

    public KcpSession(int sessionId, InetSocketAddress remoteAddress, Channel channel) {
        this.sessionId = sessionId;
        this.remoteAddress = remoteAddress;
        this.channel = channel;

        this.buffer = ByteBuffer.allocate(4096);
        this.kcpContext = new KCPContext(sessionId,sessionId,this);
        kcpContext.setWindowSize(KCPUtils.KCP_WND_SND,KCPUtils.KCP_WND_RCV);
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
        kcpContext.setWindowSize(128,128);
        kcpContext.setNoDelay(0,10,0,false);
    }

    public void send(ByteBuf buf){
        kcpContext.send(buf.nioBuffer(),buf.readableBytes());
    }

    public void receive(ByteBuf buf){
        kcpContext.input(buf.nioBuffer(),buf.readableBytes());
    }

    public void update(long current) {
        long nextUpdateTime = kcpContext.check(current);
        if (nextUpdateTime <= current) {
            kcpContext.update(current);
        }
        this.buffer.clear();
        int length = kcpContext.receive(buffer,4096);
        if (length > 0) {
            buffer.flip();
            ByteBuf buf = Unpooled.copiedBuffer(buffer);
            onReceiveMessage(buf);
        }
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
        this.kcpContext.setConversationId(sessionId);
    }

    public int getSessionId() {
        return sessionId;
    }

    @Override
    public int output(byte[] bytes, int i, KCPContext kcpContext, Object o) {
        try {
            ByteBuf buf = Unpooled.copiedBuffer(bytes);
            channel.writeAndFlush(new DatagramPacket(buf, remoteAddress)).sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    @Override
    public void writeLog(String s, KCPContext kcpContext, Object o) {

    }

    public abstract void onReceiveMessage(ByteBuf buffer);
}
