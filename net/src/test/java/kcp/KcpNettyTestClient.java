package kcp;

import core.KCPContext;
import core.kcp.IKcpCoder;
import core.kcp.KcpNettyClientSession;
import io.netty.buffer.ByteBuf;

public class KcpNettyTestClient {

    public KcpNettyTestClient() {

    }

    public void connect(String host, int port) {
        KcpNettyClientSession<Object> session = new KcpNettyClientSession<Object>(new IKcpCoder<Object>() {
            @Override
            public ByteBuf encode(ByteBuf buffer) {
                return null;
            }

            @Override
            public Object decode(ByteBuf buffer) {
                return null;
            }
        }) {
            @Override
            public void onReceiveMessage(Object message) {

            }

            @Override
            public void writeLog(String s, KCPContext kcpContext, Object o) {

            }
        };
        session.connect(host,port);
    }

    public void sendTestPackage() {

    }


    public static void main(String[] args) {
        KcpNettyTestClient client = new KcpNettyTestClient();
        client.connect("127.0.0.1",8800);
        client.sendTestPackage();
    }

}
