package tracker;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import entity.config;

/**
 * 启动 DataCaptureProxy：
 * 客户端将 announce 请求发到本机 capturePort，
 * 由 DataCaptureProxy 拦截并转发到 trackerHost:trackerPort。
 */
public class DataCaptureServer {
    
    public static void start() throws IOException {
        String trackerHost = config.trackerHost;
        int    trackerPort = config.trackerPort;
        int    capturePort = config.capturePort;

        DataCaptureProxy proxy = new DataCaptureProxy(trackerHost, trackerPort);
        ContainerServer server = new ContainerServer(proxy);
        Connection      conn   = new SocketConnection(server);

        conn.connect(new InetSocketAddress(capturePort));
        System.out.println("DataCapture proxy listening on port "
            + capturePort + ", forwarding to "
            + trackerHost + ":" + trackerPort);
    }
}
