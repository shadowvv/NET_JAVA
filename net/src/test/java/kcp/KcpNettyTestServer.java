package kcp;

import core.KCPContext;
import core.kcp.IKcpCoder;
import core.kcp.KcpNettyServerClientSession;
import core.kcp.KcpNettyServerSession;
import io.netty.buffer.ByteBuf;

public class KcpNettyTestServer {

    public KcpNettyTestServer() {

    }

    public void init(int port) {
        KcpNettyServerSession<Object> serverSession = new KcpNettyServerSession<>(new IKcpCoder<Object>() {
            @Override
            public ByteBuf encode(ByteBuf buffer) {
                return buffer;
            }

            @Override
            public Object decode(ByteBuf buffer) {
                return null;
            }
        },ServerClientSession.class,2) {
            @Override
            public void onReceiveMessage(ByteBuf buffer) {

            }

            @Override
            public void dispatchSessionMessage(int sessionId, Object message) {

            }
        };
        serverSession.listen(port);
    }

    public static class ServerClientSession extends KcpNettyServerClientSession{

        public ServerClientSession(int sessionId) {
            super(sessionId);
        }

        @Override
        public void onReceiveMessage(ByteBuf buffer) {

        }

        @Override
        public void writeLog(String s, KCPContext kcpContext, Object o) {

        }
    }

    public static void main(String[] args) {
        KcpNettyTestServer server = new KcpNettyTestServer();
        server.init(8800);
    }

}
