package NetworkStuff;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientHandler implements Runnable{
    private String username = "";
    private Socket cSocket;
    private BufferedReader reader;
    private PrintWriter writer;

    public ClientHandler(Socket s){
        try{
            cSocket = s;
            reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            writer = new PrintWriter(cSocket.getOutputStream(), true);
        } catch (IOException e){logError(e);}
    }

    public void promptUsername(){
        try{
            writer.println("Enter a Username To Begin..");
            while (username.isEmpty()){
                String in = reader.readLine().strip();
                if (in.length() > 4) {
                    if (!Server.handlerMap.containsKey(in)){
                        username = in;
                        Server.handlerMap.put(username, this);
                        writer.println("Welcome to Chat!");
                        broadcast(username + " Has Entered The Chat");
                    } else {writer.println("Username Has Been Taken");}
                } else {writer.println("Name Must Be At least 4 Chars");}
            }
        } catch (IOException e){logError(e);}
    }

    @Override public void run() {
        promptUsername();
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
        for (ClientHandler c : Server.handlerMap.values()){
            if (!c.username.equals(username)){
                c.writer.println(message);
            }
        }
    }

    public void disconnect(){
        Server.handlerMap.remove(username);
        broadcast(username + " Disconnected");
    }

    public void shutdown(){
        try {
            reader.close();
            writer.close();
            cSocket.close();
        } catch (IOException e){logError(e);}
    }

    private void logError(IOException e){
        System.out.println(e.getMessage());
    }
}
