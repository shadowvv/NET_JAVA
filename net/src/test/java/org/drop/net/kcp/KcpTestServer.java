package org.drop.net.kcp;

import org.drop.net.core.kcp.IKcpCoder;
import org.drop.net.core.kcp.KcpServerSession;
import io.netty.buffer.ByteBuf;

public class KcpTestServer {

    public KcpTestServer() {

    }

    public void init(int port) {
        KcpServerSession<KcpTestLogicMessage> serverSession = new KcpServerSession<>(new IKcpCoder<KcpTestLogicMessage>() {
            @Override
            public ByteBuf encode(KcpTestLogicMessage message) {
                return null;
            }

            @Override
            public KcpTestLogicMessage decode(ByteBuf buffer) {
                return null;
            }
        },2) {

            @Override
            public void dispatchSessionMessage(int sessionId, KcpTestLogicMessage message) {

            }
        };
        serverSession.listen(port);
    }

    public static void main(String[] args) {
        KcpTestServer server = new KcpTestServer();
        server.init(8800);
    }

}
