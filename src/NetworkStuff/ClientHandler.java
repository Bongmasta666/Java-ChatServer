package NetworkStuff;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Random;

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
                if (in.length() >= 4 && in.length() <= 20) {
                    if (!in.contains(" ")) {
                        String lower = in.toLowerCase();
                        if (!Server.handlerMap.containsKey(lower)) {
                            username = in;
                            Server.handlerMap.put(lower, this);
                            writer.println("SERVER: Welcome to Chat!");
                            broadcast("SERVER: "+username+" Has Entered The Chat");
                        } else {writer.println("SERVER: Username Has Been Taken");}
                    } else {writer.println("SERVER: Username Cannot Contain Spaces");}
                } else {writer.println("SERVER: Name Must Be Between 4-20 Chars");}
            }
        } catch (IOException e){logError(e);}
    }

    @Override public void run() {
        promptUsername();
        while (cSocket.isConnected()){
            try{
                String userIn = reader.readLine().strip();
                if (!userIn.isBlank()){
                    switch (userIn.charAt(0)){
                        case '@':
                            int b = userIn.indexOf(" ");
                            if (b > -1) {
                                String name = userIn.substring(1, b);
                                String message = userIn.substring(b+1);
                                whisper(name,"From "+username+": "+ message);
                                break;
                            }
                        case '!':
                            String com = userIn.substring(1);
                            if (com.equalsIgnoreCase("rnd")){
                                getRandomRoll(); break;
                            }else if (com.equalsIgnoreCase("show")){
                                String users = "Users Online:";
                                for (ClientHandler c : Server.handlerMap.values()) {users += "\n"+c.username;}
                                writer.println(users);
                                break;
                            }
                        default:
                            writer.println("You: "+userIn);
                            broadcast(username+": "+userIn);
                    }
                }
            } catch (IOException e){
                logError(e);
                disconnect();
                break;
            }
        }
    }

    private void broadcast(String message){
        for (ClientHandler c : Server.handlerMap.values()) {
            if (!c.username.equals(username)) {
                c.writer.println(message);
            }
        }
    }

    private void whisper(String at, String message){
        ClientHandler ch = Server.handlerMap.get(at.toLowerCase());
        if (ch != null) {ch.writer.println(message);}
        else {writer.println("SERVER: User Not Found");}
    }

    public void disconnect(){
        broadcast("SERVER: "+username + " Disconnected");
        Server.handlerMap.remove(username);
        shutdown();
    }

    public void shutdown(){
        try {
            reader.close();
            writer.close();
            cSocket.close();
        } catch (IOException e){logError(e);}
    }

    private void logError(IOException e){
        Server.logMessage(e.getMessage());
    }

    private void getRandomRoll(){
        Random rand = new Random();
        int r = rand.nextInt(101);
        writer.println("SERVER: Your Rolled "+r);
        broadcast("Rolled "+r);
    }
}
