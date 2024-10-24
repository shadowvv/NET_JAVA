package org.drop.net.core.kcp;

public class KcpUtils {

    /**
     * 推送数据命令
     */
    public static final int KCP_CMD_SHAKE = 81;
    /**
     * 推送ack信息命令
     */
    public static final int KCP_CMD_SHAKE_CONFIRM = 82;
    /**
     * 请求窗口大小命令
     */
    public static final int KCP_CMD_CONNECTED = 83;
    /**
     * 通知窗口大小命令
     */
    public static final int KCP_CMD_COMMON = 84;

    public static final int KCP_SEND_SHAKE_RETRY_COUNT = 5;

    public static final int KCP_SEND_SHAKE_RETRY_INTERVAL = 500;

    public static final int KCP_SEND_HEARTBEAT_INTERVAL = 50;
}
