package client;

import network.Request;
import network.Response;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientSocketManager {
    private static ClientSocketManager instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private ClientSocketManager() {}

    public static ClientSocketManager getInstance() {
        if (instance == null) instance = new ClientSocketManager();
        return instance;
    }

    public void connect(String host, int port) throws Exception {
        if (socket == null || socket.isClosed()) {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        }
    }

    public synchronized Response sendRequest(Request req) throws Exception {
        out.writeObject(req);
        out.flush();
        return (Response) in.readObject();
    }
}