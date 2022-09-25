import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final List<String> validPaths;
    private Map<String, Map<String, Handler>> handlers;

    public Server(List<String> validPaths) {
        this.validPaths = validPaths;
        handlers = new ConcurrentHashMap<>();
    }



    public void addHandler(String method, String path, Handler handler) {
        Map<String, Handler> element = new HashMap<>();
        element.put(path, handler);
        if (handlers.get(method) == null) {
            handlers.put(method, element);
        }
    }

    public void listen(int port) {
        System.out.println(port);
        final ExecutorService threadPool = Executors.newFixedThreadPool(64);
        try (ServerSocket serverSocket = new ServerSocket(9999)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(new Connector(clientSocket, validPaths, handlers));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
