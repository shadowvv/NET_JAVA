package core.kcp;

import io.netty.buffer.ByteBuf;

public interface IKcpCoder<T> {

    ByteBuf encode(ByteBuf buffer);

    T decode(ByteBuf buffer);

}
