package kcp;

import core.kcp.IKcpCoder;
import core.kcp.KcpNettyServerSession;
import io.netty.buffer.ByteBuf;

public class KcpNettyTestServer {

    public KcpNettyTestServer() {

    }

    public void init(int port) {
        KcpNettyServerSession<KcpTestLogicMessage> serverSession = new KcpNettyServerSession<>(new IKcpCoder<KcpTestLogicMessage>() {
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
        KcpNettyTestServer server = new KcpNettyTestServer();
        server.init(8800);
    }

}
