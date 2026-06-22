package io.percy.espresso.testutil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * A tiny single-threaded HTTP/1.1 stub used by the unit tests. It is built on
 * {@link ServerSocket} (a java.net.* type that is present in android.jar) rather
 * than com.sun.net.httpserver, which is NOT on the Android Gradle Plugin
 * unit-test compile classpath.
 *
 * <p>The server serves a fixed status / headers / body for every request, after
 * fully draining the request (so POST bodies are consumed). It loops accepting
 * connections until {@link #stop()} is called.
 */
public class StubHttpServer implements AutoCloseable {

    private final ServerSocket serverSocket;
    private final int status;
    private final String headerName;
    private final String headerValue;
    private final String body;
    private final Thread acceptThread;
    private volatile boolean running = true;

    public StubHttpServer(int status, String headerName, String headerValue, String body) throws IOException {
        this.serverSocket = new ServerSocket(0, 0, java.net.InetAddress.getByName("127.0.0.1"));
        this.status = status;
        this.headerName = headerName;
        this.headerValue = headerValue;
        this.body = body == null ? "" : body;
        this.acceptThread = new Thread(this::acceptLoop, "stub-http-server");
        this.acceptThread.setDaemon(true);
        this.acceptThread.start();
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public String getBaseUrl() {
        return "http://127.0.0.1:" + getPort();
    }

    private void acceptLoop() {
        while (running) {
            try (Socket socket = serverSocket.accept()) {
                handle(socket);
            } catch (IOException e) {
                // Socket closed during stop() -> exit quietly.
                if (running) {
                    // Best-effort: ignore transient client errors and keep serving.
                    continue;
                }
                return;
            }
        }
    }

    private void handle(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1));
        // Read request line + headers, track content-length to drain the body.
        String line = in.readLine();
        int contentLength = 0;
        while (line != null && !line.isEmpty()) {
            int colon = line.indexOf(':');
            if (colon > 0 && line.substring(0, colon).trim().equalsIgnoreCase("Content-Length")) {
                try {
                    contentLength = Integer.parseInt(line.substring(colon + 1).trim());
                } catch (NumberFormatException ignored) {
                    contentLength = 0;
                }
            }
            line = in.readLine();
        }
        // Drain the request body so the client's stream.flush() completes cleanly.
        for (int i = 0; i < contentLength; i++) {
            if (in.read() == -1) {
                break;
            }
        }

        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        StringBuilder head = new StringBuilder();
        head.append("HTTP/1.1 ").append(status).append(" ").append(reason(status)).append("\r\n");
        if (headerName != null) {
            head.append(headerName).append(": ").append(headerValue).append("\r\n");
        }
        head.append("Content-Length: ").append(bodyBytes.length).append("\r\n");
        head.append("Connection: close\r\n");
        head.append("\r\n");

        OutputStream out = socket.getOutputStream();
        out.write(head.toString().getBytes(StandardCharsets.ISO_8859_1));
        out.write(bodyBytes);
        out.flush();
    }

    private static String reason(int status) {
        switch (status) {
            case 200: return "OK";
            case 500: return "Internal Server Error";
            default: return "Status";
        }
    }

    public void stop() {
        running = false;
        try {
            serverSocket.close();
        } catch (IOException ignored) {
            // ignore
        }
    }

    @Override
    public void close() {
        stop();
    }
}
