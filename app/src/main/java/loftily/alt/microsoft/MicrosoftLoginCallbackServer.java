package loftily.alt.microsoft;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.Getter;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class MicrosoftLoginCallbackServer implements Closeable {
    private final HttpServer server;
    @Getter
    private final CompletableFuture<String> codeFuture = new CompletableFuture<>();
    
    public MicrosoftLoginCallbackServer(int port, String callbackPath) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext(callbackPath, new CallbackHandler());
    }
    
    public MicrosoftLoginCallbackServer start() {
        server.start();
        return this;
    }
    
    @Override
    public void close() throws IOException {
        if (this.server != null) {
            this.server.stop(0);
        }
    }
    
    private class CallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            URI requestURI = exchange.getRequestURI();
            String query = requestURI.getQuery();
            //提取code
            if (query != null && query.contains("code=")) {
                String code = "";
                for (String param : query.split("&")) {
                    String[] kv = param.split("=", 2);
                    if (kv.length == 2 && kv[0].equals("code")) {
                        code = kv[1];
                    }
                }
                codeFuture.complete(code);
                response(exchange, "Authorization code received. You can close this window.");
                return;
            }
            response(exchange, "Missing authorization code.");
        }
        
        private void response(HttpExchange exchange, String body) throws IOException {
            exchange.sendResponseHeaders(200, body.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body.getBytes());
                os.flush();
            }
        }
    }
}
