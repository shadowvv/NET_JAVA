package core.kcp;

import core.kcp.message.KcpCommonMessage;
import io.netty.buffer.ByteBuf;

public interface IKcpCoder<T extends KcpCommonMessage> {

    ByteBuf encode(T message);

    T decode(ByteBuf buffer);

}
