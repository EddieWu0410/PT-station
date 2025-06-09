package tracker;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import tracker.Tracker;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;

/**
 * 拦截 announce 请求，打印参数后转发给真实 Tracker。
 */
public class DataCaptureProxy implements Container {

    private final String trackerHost;
    private final int    trackerPort;
    private final Tracker tracker;
    private final CheatDetectionScheduler cheatScheduler; // 新增

    public DataCaptureProxy(String trackerHost, int trackerPort) {
        this.trackerHost = trackerHost;
        this.trackerPort = trackerPort;
        this.tracker = new Tracker();
        
        // 初始化并启动作弊检测调度器
        this.cheatScheduler = new CheatDetectionScheduler();
        this.cheatScheduler.start();
        
        // 添加JVM关闭钩子，确保优雅关闭
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down CheatDetectionScheduler...");
            this.cheatScheduler.stop();
        }));
    }

    @Override
    public void handle(Request req, Response resp) {
        try {
            // 提取并打印关键参数
            String infoHashParam = req.getParameter("info_hash");
            String infoHash = null;
            String infoHashHex = null;
            
            // 尝试从原始查询字符串中直接提取 info_hash
            String rawQuery = req.getQuery().toString();
            System.out.println("DEBUG: Raw query string: " + rawQuery);
            
            // 正确处理 info_hash 的字符集
            if (infoHashParam != null) {
                try {
                    // 先尝试从原始查询字符串中提取 info_hash
                    byte[] infoHashBytes = extractInfoHashFromRawQuery(rawQuery);
                    
                    if (infoHashBytes == null || infoHashBytes.length != 20) {
                        System.out.println("DEBUG: Raw query extraction failed, trying parameter method");
                        // 回退到参数解析方法
                        infoHashBytes = processInfoHashParameter(infoHashParam);
                    }
                    
                    if (infoHashBytes != null) {
                        // 转换为十六进制字符串
                        StringBuilder hexBuilder = new StringBuilder();
                        for (byte b : infoHashBytes) {
                            hexBuilder.append(String.format("%02x", b & 0xFF));
                        }
                        infoHashHex = hexBuilder.toString();
                        
                        // 调试输出
                        System.out.print("DEBUG: Final byte values (hex): ");
                        for (byte b : infoHashBytes) {
                            System.out.printf("%02x ", b & 0xFF);
                        }
                        System.out.println();
                        
                        System.out.println("DEBUG: Final infoHashHex: " + infoHashHex);
                        System.out.println("DEBUG: Final infoHashHex length: " + infoHashHex.length());
                        
                        // 验证最终哈希长度
                        if (infoHashHex.length() != 40) {
                            System.err.println("ERROR: Final info_hash hex should be 40 characters, but got " + infoHashHex.length());
                        }
                    }
                    
                } catch (Exception e) {
                    System.err.println("Error processing info_hash: " + e.getMessage());
                    e.printStackTrace();
                    infoHash = infoHashParam; // 回退到原始值
                    infoHashHex = "invalid_hash";
                }
            }
            
            // ===== 新增：容错匹配，替换为数据库中最相似的完整 hash =====
            if (infoHashHex != null && !infoHashHex.isEmpty()) {
                List<String> allHashes = tracker.getAllInfoHashes();  // 从 DB 拉取所有 hash
                String best = findBestMatchingInfoHash(infoHashHex, allHashes);
                if (best != null) {
                    System.out.println("DEBUG: Fallback matched infoHash: " + best);
                    infoHashHex = best;
                }
            }

            // 提取其他参数
            String uploaded   = req.getParameter("uploaded");
            String downloaded = req.getParameter("downloaded");
            String passkey    = req.getParameter("passkey");
            String port       = req.getParameter("port"); // qBittorrent 服务端端口

            // 获取客户端IP地址和端口
            String clientIp;
            int clientPort = -1;
            // 直接从 TCP 连接（socket 源地址）中读取
            SocketAddress socketAddress = req.getClientAddress();
            if (socketAddress instanceof InetSocketAddress) {
                InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
                clientIp = inetSocketAddress.getAddress().getHostAddress();
                clientPort = inetSocketAddress.getPort();
            } else {
                // 兜底写法，将整个 SocketAddress 转为字符串
                clientIp = socketAddress.toString();
            }

            System.out.println(
                "Captured announce → info_hash_hex=" + infoHashHex +
                ", uploaded=" + uploaded +
                ", downloaded=" + downloaded +
                ", passkey=" + passkey +
                ", client_ip=" + clientIp +
                ", client_port=" + clientPort +
                ", qbt_service_port=" + port
            );

            // 调用 Tracker 方法更新上传和下载数据（使用校正后的 infoHashHex）
            if (passkey != null && !passkey.isEmpty() && infoHashHex != null && !infoHashHex.isEmpty()) {
                try {
                    if (uploaded != null && !uploaded.isEmpty()) {
                        int uploadValue = Integer.parseInt(uploaded);
                        if (uploadValue > 0) {
                            try {
                                tracker.AddUpLoad(passkey, uploadValue, infoHashHex);
                            } catch (javax.persistence.NoResultException e) {
                                System.out.println("Skipping upload update: info_hash not found in database - " + infoHashHex);
                            }
                        }
                    }
                    
                    if (downloaded != null && !downloaded.isEmpty()) {
                        int downloadValue = Integer.parseInt(downloaded);
                        if (downloadValue > 0) {
                            try {
                                tracker.AddDownload(passkey, downloadValue, infoHashHex);
                            } catch (javax.persistence.NoResultException e) {
                                System.out.println("Skipping download update: info_hash not found in database - " + infoHashHex);
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing upload/download values: " + e.getMessage());
                }
            }

            // 构造转发 URL
            String path = req.getPath().getPath();
            String query = req.getQuery().toString();
            String targetUrl = "http://" + trackerHost + ":" + trackerPort
                             + path + "?" + query;

            HttpURLConnection connection =
                (HttpURLConnection) new URL(targetUrl).openConnection();
            connection.setRequestMethod("GET");

            // 转发响应码和类型
            resp.setCode(connection.getResponseCode());
            String ct = connection.getContentType();
            if (ct != null) resp.setValue("Content-Type", ct);

            // 转发响应体
            try (InputStream in = connection.getInputStream();
                 OutputStream out = resp.getOutputStream()) {
                byte[] buf = new byte[8192];
                int  len;
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
            }

        } catch (Exception e) {
            try {
                resp.setCode(500);
                resp.close();
            } catch (Exception ignore) {}
            e.printStackTrace();
        }
    }
    
    /**
     * 从原始查询字符串中提取 info_hash 的字节
     */
    private byte[] extractInfoHashFromRawQuery(String rawQuery) {
        try {
            // 查找 info_hash= 的位置
            int start = rawQuery.indexOf("info_hash=");
            if (start == -1) {
                return null;
            }
            
            start += "info_hash=".length(); // 跳过 "info_hash="
            
            // 查找下一个 & 或字符串结尾
            int end = rawQuery.indexOf('&', start);
            if (end == -1) {
                end = rawQuery.length();
            }
            
            String encodedInfoHash = rawQuery.substring(start, end);
            System.out.println("DEBUG: Extracted encoded info_hash: " + encodedInfoHash);
            System.out.println("DEBUG: Encoded info_hash length: " + encodedInfoHash.length());
            
            // 手动解码 percent-encoding，确保正确处理二进制数据
            byte[] bytes = decodePercentEncoding(encodedInfoHash);
            System.out.println("DEBUG: Raw extraction - bytes length: " + bytes.length);
            
            return bytes.length == 20 ? bytes : null;
            
        } catch (Exception e) {
            System.err.println("Error extracting info_hash from raw query: " + e.getMessage());
            return null;
        }
    }

    
    
    /**
     * 手动解码 percent-encoding，确保正确处理二进制数据
     */
    private byte[] decodePercentEncoding(String encoded) {
        try {
            int length = encoded.length();
            byte[] result = new byte[length]; // 最大可能长度
            int resultIndex = 0;
            
            for (int i = 0; i < length; i++) {
                char c = encoded.charAt(i);
                
                if (c == '%' && i + 2 < length) {
                    // 解码 %XX
                    String hex = encoded.substring(i + 1, i + 3);
                    try {
                        int value = Integer.parseInt(hex, 16);
                        result[resultIndex++] = (byte) value;
                        i += 2; // 跳过接下来的两个字符
                    } catch (NumberFormatException e) {
                        // 如果不是有效的十六进制，当作普通字符处理
                        result[resultIndex++] = (byte) c;
                    }
                } else if (c == '+') {
                    // '+' 在 URL 编码中表示空格
                    result[resultIndex++] = (byte) ' ';
                } else {
                    // 普通字符
                    result[resultIndex++] = (byte) c;
                }
            }
            
            // 创建正确长度的数组
            byte[] finalResult = new byte[resultIndex];
            System.arraycopy(result, 0, finalResult, 0, resultIndex);
            
            System.out.println("DEBUG: Percent decoding - input length: " + length + ", output length: " + resultIndex);
            
            return finalResult;
            
        } catch (Exception e) {
            System.err.println("Error in decodePercentEncoding: " + e.getMessage());
            return new byte[0];
        }
    }
    
    /**
     * 处理通过参数解析得到的 info_hash
     */
    private byte[] processInfoHashParameter(String infoHashParam) {
        try {
            // 先尝试手动 percent-encoding 解码
            byte[] infoHashBytes = decodePercentEncoding(infoHashParam);
            
            // 调试信息
            System.out.println("DEBUG: Parameter method - Original: " + infoHashParam);
            System.out.println("DEBUG: Parameter method - Original length: " + infoHashParam.length());
            System.out.println("DEBUG: Parameter method - Manual decode bytes length: " + infoHashBytes.length);
            
            // 如果手动解码失败，尝试标准方法
            if (infoHashBytes.length != 20) {
                System.out.println("DEBUG: Manual decode failed, trying URLDecoder with ISO-8859-1");
                try {
                    // 使用 ISO-8859-1 而不是 UTF-8
                    String decodedParam = URLDecoder.decode(infoHashParam, StandardCharsets.ISO_8859_1.name());
                    infoHashBytes = decodedParam.getBytes(StandardCharsets.ISO_8859_1);
                    System.out.println("DEBUG: URLDecoder ISO-8859-1 result length: " + infoHashBytes.length);
                } catch (Exception e2) {
                    System.err.println("URLDecoder with ISO-8859-1 failed: " + e2.getMessage());
                }
            }
            
            // 最后尝试：直接将字符串转为字节
            if (infoHashBytes.length != 20) {
                System.out.println("DEBUG: Trying direct byte conversion");
                infoHashBytes = infoHashParam.getBytes(StandardCharsets.ISO_8859_1);
                System.out.println("DEBUG: Direct conversion result length: " + infoHashBytes.length);
            }
            
            // 验证字节长度
            if (infoHashBytes.length != 20) {
                System.err.println("WARNING: info_hash should be 20 bytes, but got " + infoHashBytes.length + " bytes");
            }
            
            return infoHashBytes;
            
        } catch (Exception e) {
            System.err.println("Error in processInfoHashParameter: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 在候选 hash 列表中，找到与 targetHex 最长公共子序列（LCS）最大的那个
     */
    private String findBestMatchingInfoHash(String targetHex, List<String> candidates) {
        String best = null;
        int bestLen = -1;
        for (String cand : candidates) {
            int len = longestCommonSubseq(targetHex, cand);
            if (len > bestLen) {
                bestLen = len;
                best = cand;
            }
        }
        return best;
    }

    // 计算两字符串的 LCS 长度
    private int longestCommonSubseq(String a, String b) {
        int n = a.length(), m = b.length();
        int[] dp = new int[m+1];
        for (int i = 1; i <= n; i++) {
            int prev = 0;
            for (int j = 1; j <= m; j++) {
                int temp = dp[j];
                if (a.charAt(i-1) == b.charAt(j-1)) {
                    dp[j] = prev + 1;
                } else {
                    dp[j] = Math.max(dp[j], dp[j-1]);
                }
                prev = temp;
            }
        }
        return dp[m];
    }
    
    /**
     * 获取调度器状态
     */
    public boolean isCheatDetectionRunning() {
        return cheatScheduler.isRunning();
    }
    
    /**
     * 手动停止调度器（如果需要的话）
     */
    public void stopCheatDetection() {
        cheatScheduler.stop();
    }
}
