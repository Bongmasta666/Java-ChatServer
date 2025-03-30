package NetworkStuff;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.io.*;
import java.util.ArrayList;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;

public class Server extends JFrame {
    private static final JTextArea logWindow = new JTextArea();
    private static final JTextField cmdLine = new JTextField();
    private static final ArrayList<Socket> clients = new ArrayList<>();

    private ServerSocket socket;
    private String log = "";

    public Server(){
        logWindow.setPreferredSize(new Dimension(400, 320));
        logWindow.setBackground(Color.BLACK);
        logWindow.setDisabledTextColor(Color.GREEN);
        logWindow.setEnabled(false);
        add(logWindow, "Center");

        cmdLine.setPreferredSize(new Dimension(400, 26));
        cmdLine.setBackground(Color.LIGHT_GRAY);
        cmdLine.setForeground(Color.GREEN);
        add(cmdLine, "South");

        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setTitle("Bongs Server");
        setVisible(true);
        openSocket(59125);
    }

    private void openSocket(int port) {
        try{
            socket = new ServerSocket(port);
            InetAddress address = InetAddress.getLocalHost();
            logMessage("Server Started.\nIP:"+ address.getHostAddress()+" Port: "+port);

            while (!socket.isClosed()){
                Socket incoming = socket.accept();
                String addy = incoming.getInetAddress().getHostAddress();
                logMessage("New Connection Received: "+addy);
                OutputStream stream = incoming.getOutputStream();
                PrintWriter writer = new PrintWriter(stream, true);
                writer.println("Enter A Username: ");
                clients.add(incoming);
            }
            
        } catch (IOException e){logMessage(e.getMessage());}
    }

    public void logMessage(String message){
        log += message+"\n";
        logWindow.setText(log);
    }

    public static void main(String[] args){
        new Server();
    }
}
