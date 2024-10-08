package core.kcp;

import core.IKCPContext;
import core.KCPContext;
import core.KCPSegment;
import core.KCPUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;

public abstract class KcpSession implements IKCPContext {

    private int sessionId;
    private final KCPContext kcpContext;
    private final ByteBuffer buffer;

    public KcpSession(int sessionId) {
        this.sessionId = sessionId;
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

    public void receive(int sessionId, ByteBuf buf){
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
            ByteBuf buf = Unpooled.copiedBuffer(buffer);
            onReceiveMessage(buf);
        }
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public abstract void start();

    public abstract void onReceiveMessage(ByteBuf buffer);
}