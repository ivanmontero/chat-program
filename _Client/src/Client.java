import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {

    private Main main;
    private ClientGUI clientGUI;
    private ObjectInputStream socketIn;
    private ObjectOutputStream socketOut;
    private Socket socket;
    private String serverIP = "localhost", username = System.getProperty("user.name");
    private Thread serverListener;
    private int port = 1339;
    private boolean isRunning;

    public Client(Main main, ClientGUI clientGUI){
        this.main = main;
        this.clientGUI = clientGUI;
    }

    public void start(){
        if(connect()) {
            this.isRunning = true;
            serverListener.start();
        }
    }

    public boolean connect(){
        try{
            socket = new Socket(serverIP, port);
            socketOut = new ObjectOutputStream(socket.getOutputStream());
            socketIn = new ObjectInputStream(socket.getInputStream());
            socketOut.writeObject(username);
        } catch (Exception e){
            clientGUI.display("[Error] Failed to connect to server: " + serverIP + ":" + port);
            return false;
        }
        clientGUI.display("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
        serverListener = new ServerListener(this, clientGUI, socketIn);
        return true;
    }

    public void processInput(String msg){
        if(msg.startsWith("/")){
            String[] args = msg.split(" ");
            switch (args[0].substring(1, args[0].length())){
                case "connect":
                    if(!isRunning) {
                        switch (args.length){
                            case 1:
                                start();
                                break;
                            case 2:
                                if(args[1].split("\\.").length == 4) {
                                    this.serverIP = args[1];
                                    System.out.println(args[1]);
                                    start();
                                } else {
                                    clientGUI.display("[Error] " + args[1] + " is not a valid IP address");
                                }
                                break;
                            case 3:
                                if(args[1].split("\\.").length == 4) {
                                    this.serverIP = args[1];
                                    try {
                                        this.port = Integer.parseInt(args[2]);
                                    } catch (Exception e){
                                        clientGUI.display("[Error] " + args[2] + " is not a valid port");
                                        return;
                                    }
                                    start();
                                } else {
                                    clientGUI.display("[Error] " + args[1] + " is not a valid IP address");
                                }
                                break;
                            default:
                                clientGUI.display("[Error] Usage: /connect [IP address] (port)");
                                break;
                        }
                    } else {
                        clientGUI.display("[Error] Already connected to server");
                    }
                    break;
                case "help":
                    for(String s : commandList){
                        clientGUI.display("   " + s);
                    }
                    break;
                case "clear":
                    clientGUI.clearChat();
                    break;
                case "users":
                    send(new ChatMessage(ChatMessage.USERLIST, ""));
                    break;
                case "color":
                    int r = 0;
                    int g = 0;
                    int b = 0;
                    int a = 0;
                    switch (args.length){
                        case 3:
                            clientGUI.setColor(args[1], args[2]);
                            break;
                        case 5:
                            try{
                                r = Integer.parseInt(args[2]);
                                g = Integer.parseInt(args[3]);
                                b = Integer.parseInt(args[4]);
                            } catch (Exception e){
                                clientGUI.display("[Error] Provided color values are not valid");
                            }
                            if(r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255){
                                clientGUI.display("[Error] Provided color values are out of range (0 - 255)");
                            } else {
                                clientGUI.setColor(args[1], new Color(r, g, b));
                            }
                            break;
                        case 6:
                            try{
                                r = Integer.parseInt(args[2]);
                                g = Integer.parseInt(args[3]);
                                b = Integer.parseInt(args[4]);
                                a = Integer.parseInt(args[5]);
                            } catch (Exception e){
                                clientGUI.display("[Error] Provided color values are not valid");
                            }
                            if(r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255 || a < 0 || a > 255){
                                clientGUI.display("[Error] Provided color values are out of range (0 - 255)");
                            } else {
                                clientGUI.setColor(args[1], new Color(r, g, b, a));
                            }
                        default:
                            clientGUI.display("[Error] Usage: /color [component] [color name]");
                            clientGUI.display("               /color [component] [red] [green] [blue] (alpha)");
                            break;
                    }
                    break;
                case "quit":
                    if(args.length == 1) {
                        exit();
                    } else {
                        clientGUI.display("[Error] Usage: /quit");
                    }
                    break;
                case "exit":
                    if(args.length == 1) {
                        exit();
                    } else {
                        clientGUI.display("[Error] Usage: /exit");
                    }
                    break;
                case "stop":
                    if(args.length == 1) {
                        exit();
                    } else {
                        clientGUI.display("[Error] Usage: /stop");
                    }
                    break;
                default:
                    clientGUI.display("[Error] Command \""
                            + args[0].substring(1, args[0].length()) + "\" not recognized");
                    break;
            }
        } else {
            send(new ChatMessage(ChatMessage.MESSAGE, msg));
        }
    }

    public void send(ChatMessage msg){
        if(isRunning) {
            try {
                socketOut.writeObject(msg);
            } catch (Exception e) {
                clientGUI.display("[Error] Failed to send message");
            }
        } else {
            clientGUI.display("[Error] Not connected to server: /connect [IP address] (port)");
        }
    }

    public synchronized void exit(){
        if (isRunning) {
            clientGUI.display("[System] Quitting");
            isRunning = false;
            try {
                if (!socket.isClosed()) {
                    socketIn.close();
                    socketOut.close();
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        main.exit();
    }

    public String getUsername(){
        return username;
    }

    private final String[] commandList = {
            "/help",
            "/connect [IP address] (port)",
            "/exit /quit /exit",
            "/clear",
            "/color [component] [red] [green] [blue] (alpha)"
    };
}
