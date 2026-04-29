package server;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(9999)) {
            System.out.println("Máy chủ Pharmacy đang chạy ở port 9999...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Có một Client mới kết nối: " + clientSocket.getInetAddress());

                new ClientHandler(clientSocket).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}