package com.example.pslab1fx;

import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import static com.example.pslab1fx.ServerController.*;

public class Server implements Runnable{
    private ServerSocket serverSocket;
    private Socket socket;
    private DataOutputStream output;
    private DataInputStream input;
    private final ServerController c;
    private Thread t1;
    private boolean isThreadRunning;
    private boolean isClientConnected = false;

    private static final int BUSY = 2;
    private static final int ALLOWED = 1;
    private static final int DENIED = 0;

    public Server(ServerController c) {
        this.c = c;
    }

    @Override
    public void run() {
        isThreadRunning = true;
        t1.start();
    }

    private void serverThread() {
        while (isThreadRunning) {
            waitForClient();
            // zamykanie serwera - metoda przypisana do przycisku
        }
    }

    public void waitForClient() {
        try {

            Platform.runLater(() -> c.sendAlert(INFO, "Waiting for client.."));
            socket = serverSocket.accept();
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            output.writeInt(ALLOWED);

            Platform.runLater(() -> c.sendAlert(INFO, "Client connected!"));
            isClientConnected = true;


            // pobierz wiadomosc, wyslij wiadomosc
            while (isThreadRunning) {
                String messageFromClient = input.readUTF();
                Platform.runLater(() -> c.sendAlert(ECHO, "Message [" + messageFromClient.getBytes().length + " bytes]: " + messageFromClient));

                output.writeUTF(messageFromClient);
                Platform.runLater(() -> c.sendAlert(ECHO, "Sending message back to client"));
            }

            socket.close();
            Platform.runLater(() -> c.sendAlert(INFO, "Client disconnected by server"));
        } catch (IOException e) {
            if (isClientConnected)
                Platform.runLater(() -> c.sendAlert(ERROR, "Client disconnected!"));
        }
    }

    public void start(Integer port) {
        try {
            this.serverSocket = new ServerSocket();
            SocketAddress address = new InetSocketAddress("0.0.0.0", port);
            this.serverSocket.bind(address);
        } catch (IOException e) {
            c.sendAlert(ERROR, "Socket input error!");
            return;
        }
        c.sendAlert(INFO, "Server started");
        t1 = new Thread(this::serverThread);
        run();
    }

    public void stop() {
        try {
            isThreadRunning = false;
            this.serverSocket.close();

            if (isClientConnected)
                socket.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        c.sendAlert(INFO, "Server closed");

    }


}
