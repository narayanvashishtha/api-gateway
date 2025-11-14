package com.example.api_gateway.logging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class GatewayRequestLog {
    String requestId;
    String method;
    String path;
    String query;
    String clientIp;
    String user;
    int status;
    long timestamp;           // start time ms
    long processingTimeMs;  // duration ms

    @Override
    public String toString() {
        return  "reqId=" + requestId +
                " | method=" + method +
                " | path=" + path +
                " | query=" + (query != null ? query : "") +
                " | ip=" + clientIp +
                " | user=" + user +
                " | status=" + status +
                " | time=" + processingTimeMs + "ms";
    }
}
