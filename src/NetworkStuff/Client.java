package NetworkStuff;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client extends JFrame {
    private static final JTextArea logWindow = new JTextArea();
    private static final JTextField userInput = new JTextField();
    private static final String hostAddress = "127.0.0.1";
    private static final int hostPort = 8888;

    private Socket hostSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String log = "";

    public Client() {
        setJMenuBar(clientMenuBar());
        logWindow.setPreferredSize(new Dimension(400, 320));
        logWindow.setDisabledTextColor(Color.GREEN);
        logWindow.setBackground(Color.BLACK);
        logWindow.setEnabled(false);
        add(logWindow, "Center");

        userInput.setPreferredSize(new Dimension(400, 25));
        userInput.addActionListener(_ -> sendMessage());
        userInput.setBackground(Color.DARK_GRAY);
        userInput.setForeground(Color.GREEN);
        add(userInput, "South");

        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setTitle("Client Shell");
        setVisible(true);
    }

    public static void main(String[] args) {
        new Client().connect();
    }

    private void connect() {
        try {
            hostSocket = new Socket(hostAddress, hostPort);
            OutputStream output = hostSocket.getOutputStream();
            reader = new BufferedReader(new InputStreamReader(hostSocket.getInputStream()));
            writer = new PrintWriter(output, true);
            logMessage("Connected");
            listen();
        } catch (IOException e) {logMessage(e.getMessage());}
    }

    private void listen() {
        new Thread(() -> {
            try {
                while (hostSocket.isConnected()) {
                    String message = reader.readLine();
                    logMessage(message);
                }
            } catch (IOException e) {logMessage(e.getMessage());}
        }).start();
    }

    private void sendMessage() {
        String message = userInput.getText();
        userInput.setText("");
        if (hostSocket.isConnected()) {
            writer.println(message);
        }
    }

    public void logMessage(String message) {
        log += message + "\n";
        logWindow.setText(log);
    }

    private void disconnect() {
        try {
            if (reader != null) {reader.close();}
            if (writer != null) {writer.close();}
            if (hostSocket != null) {hostSocket.close();}
            logMessage("Disconnected");
        } catch (IOException e) {logMessage(e.getMessage());}
    }

    private JMenuBar clientMenuBar() {
        JMenuBar menuB = new JMenuBar();
        JMenu fMenu = new JMenu("File");
        JMenuItem quitMi = new JMenuItem("Shutdown");
        quitMi.addActionListener(_ -> {disconnect();System.exit(69);});
        fMenu.add(quitMi);
        menuB.add(fMenu);
        return menuB;
    }
}