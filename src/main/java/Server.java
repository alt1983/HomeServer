import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final List<String> validPaths;

    public Server(List<String> validPaths) {
        this.validPaths = validPaths;
    }

    public void listen(int port) {
        System.out.println(port);
        final ExecutorService threadPool = Executors.newFixedThreadPool(64);
        try (ServerSocket serverSocket = new ServerSocket(9999)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(new Connector(clientSocket, validPaths));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
