import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Connector implements Runnable {

    private List<String> validPaths;
    private final Socket socket;
    private Map<String, Map<String, Handler>> handlers;

    public Connector(Socket socket, List<String> validPaths, Map<String, Map<String, Handler>> handlers) {
        this.validPaths = validPaths;
        this.socket = socket;
        this.handlers = handlers;
    }

    @Override
    public void run() {
        try {
            try (
                    final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    final var out = new BufferedOutputStream(socket.getOutputStream());
            ) {
                // read only request line for simplicity
                // must be in form GET /path HTTP/1.1
                final var requestLine = in.readLine();
                String[] parts = null;
                if (requestLine != null) {
                    parts = requestLine.split(" ");
                } else {
                    return;
                }

                if (parts.length != 3) {
                    // just close socket
                    return;
                }
                final var method = parts[0];
                final var query = parts[1];
                final var fullpath = parts[1].split("\\?");
                final var path = fullpath[0];
                final var body = requestLine;
                Request request = new Request(method, path, body, query);

                if (handlers.get(method) != null) {
                    if ((handlers.get(method)).get(path) != null) {
                        (handlers.get(method)).get(path).handle(request, new BufferedOutputStream(socket.getOutputStream()));
                        return;
                    }
                }


                if (!validPaths.contains(path)) {
                    out.write((
                            "HTTP/1.1 404 Not Found\r\n" +
                                    "Content-Length: 0\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.flush();
                    return;
                }

                final var filePath = Path.of(".", "public", path);
                final var mimeType = Files.probeContentType(filePath);

                // special case for classic
                if (path.equals("/classic.html")) {
                    final var template = Files.readString(filePath);
                    final var content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    ).getBytes();
                    out.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + content.length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.write(content);
                    out.flush();
                    return;
                }

                final var length = Files.size(filePath);
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                Files.copy(filePath, out);
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
