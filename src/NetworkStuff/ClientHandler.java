package NetworkStuff;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{
    public static ArrayList<ClientHandler> handlers = new ArrayList<>();

    private String username = "Unknown User";
    private Socket cSocket;
    private BufferedReader reader;
    private PrintWriter writer;

    public ClientHandler(Socket s, String username){
        try{
            cSocket = s;
            reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            writer = new PrintWriter(cSocket.getOutputStream(), true);
            handlers.add(this);
            this.username = username;
            broadcast(username + " Has Entered The Chat");
        } catch (IOException e){
            logError(e);
        }
    }

    @Override public void run() {
        String userIn;
        while (cSocket.isConnected()){
            try{
                userIn = reader.readLine();
                broadcast(userIn);
            } catch (IOException e){
                disconnect();
                logError(e);
                shutdown();
                break;
            }
        }
    }

    public void broadcast(String message){
        for (ClientHandler c : handlers){
            if (!c.username.equals(username)){
                c.writer.println(message);
            }
        }
    }

    public void disconnect(){
        handlers.remove(this);
        broadcast(username + " Disconnected");
    }

    public void shutdown(){
        try {
            reader.close();
            writer.close();
            cSocket.close();
        } catch (IOException e){
            logError(e);
        }
    }

    private void logError(IOException e){
        System.out.println(e.getMessage());
    }
}
