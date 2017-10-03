import java.io.ObjectInputStream;

public class ServerListener extends Thread{

    private boolean isRunning;
    private Client client;
    private ClientGUI clientGUI;
    private ObjectInputStream socketIn;
    private String msg;

    public ServerListener(Client client, ClientGUI clientGUI, ObjectInputStream socketIn){
        super();
        this.client = client;
        this.clientGUI = clientGUI;
        this.socketIn = socketIn;
        this.isRunning = true;
    }

    @Override
    public void run(){
        while(isRunning){
            try{
                msg = (String) socketIn.readObject();
            } catch (Exception e){
                System.out.println("Connection has been lost");
                return;
            }
            clientGUI.display(msg);
        }
    }

}
