import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientThread extends Thread {

    private Server server;
    private Socket socket;
    private ObjectInputStream socketIn;
    private ObjectOutputStream socketOut;
    private int id;
    private String username, message;
    private ChatMessage msg;
    private boolean isRunning;

    public ClientThread(Server server, Socket socket, int id){
        this.server = server;
        this.socket = socket;
        this.id = id;
        try{
            socketOut = new ObjectOutputStream(socket.getOutputStream());
            socketIn = new ObjectInputStream(socket.getInputStream());
            username = (String) socketIn.readObject();
        } catch (Exception e){
            e.printStackTrace();
        }
        isRunning = true;
    }

    @Override
    public void run(){
        while(isRunning){
            try {
                msg = (ChatMessage) socketIn.readObject();
            } catch (Exception e) {
                server.remove(id);
                server.broadcast("[Server] " + username + " has disconnected.");
                isRunning = false;
                close();
                return;
            }
            message = msg.getMessage();
            switch (msg.getType()){
                case ChatMessage.MESSAGE:
                    server.broadcast(username + "> " + message);
                    break;
                case ChatMessage.USERLIST:
                    String[] usernames = server.getUsernames();
                    for(String s : usernames){
                        writeMessage(s);
                    }
                    break;
                case ChatMessage.COMMAND:
                    String[] args = message.split(" ");
                    switch (args[0]) {
                        case "logout":
                            server.broadcast("[Server] " + username + " has disconnected");
                            isRunning = false;
                            break;
                    }
                    break;
            }
        }
        server.remove(id);
        close();
    }

    public void close(){
        try {
            socketIn.close();
            socketOut.close();
            socket.close();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public void writeMessage(String msg){
        if(!socket.isConnected()){
            server.broadcast("[Server] " + username + " has disconnected.");
            isRunning = false;
            server.remove(id);
            close();
            return;
        } else {
            try {
                socketOut.writeObject(msg);
            } catch (Exception e) {
                server.broadcast("[Server] " + username + " has disconnected.");
                isRunning = false;
                server.remove(id);
                close();
                return;
            }
        }
    }

    public String getUsername(){
        return username;
    }

    public int getID(){
        return id;
    }


}
