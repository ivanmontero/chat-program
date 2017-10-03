import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class Server{

    private int uniqueId, port = 1339;
    private ServerSocket serverSocket;
    private volatile boolean isRunning;
    private volatile ArrayList<ClientThread> clients;
    private Main main;
    private ServerGUI serverGUI;
    private String serverName;

    public Server(Main main, ServerGUI serverGUI){
        this.main = main;
        this.serverGUI = serverGUI;
        this.clients = new ArrayList<ClientThread>();
        this.serverName = "Server";
    }

    public void connect(){
        try{
            /*
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress("0.0.0.0", port));
            */
            serverSocket = new ServerSocket(port,8, InetAddress.getByName("0.0.0.0"));
            serverGUI.display("[System] Connection successfully created: "
                    + InetAddress.getLocalHost().getHostAddress() + ":" + serverSocket.getLocalPort());
        } catch (IOException e) {
            serverGUI.display("[Error] Unable to bind to port " + port);
            e.printStackTrace();
            return;
        }
        start();
    }

    public void start(){
        this.isRunning = true;
        loop();
        exit();
    }

    public void loop(){
        serverGUI.display("[System] Listening on port " + serverSocket.getLocalPort());
        while(isRunning){
            try {
                Socket socket = serverSocket.accept();
                ClientThread ct = new ClientThread(this, socket, ++uniqueId);
                broadcast("[Server] " + ct.getUsername() + " has connected");
                ct.start();
                clients.add(ct);
            } catch (SocketException se){
                serverGUI.display("[System] Server socket closed");
                return;
            } catch (Exception e){
                serverGUI.display("[Error] Could not listen to port");
            }
        }
    }

    //Server use
    public void processInput(String msg){
        if(msg.startsWith("/")){
            String[] args = msg.split(" ");
            switch (args[0].substring(1, args[0].length())){
                case "start":
                    if(!isRunning) {
                        switch (args.length){
                            case 1:
                                main.startServer();
                                break;
                            case 2:
                                try{
                                    this.port = Integer.parseInt(args[1]);
                                } catch (Exception e){
                                    serverGUI.display("[Error] " + args[1] + " is not a valid port");
                                    return;
                                }
                                main.startServer();
                                break;
                            default:
                                serverGUI.display("[Error] Usage: /start (port)");
                                break;
                        }
                    } else {
                        serverGUI.display("[Error] Server is already running");
                    }
                    break;
                case "help":
                    for(String s : commandList){
                        serverGUI.display("   " + s);
                    }
                    break;
                case "clear":
                    serverGUI.clearChat();
                    break;
                case "color":
                    int r = 0;
                    int g = 0;
                    int b = 0;
                    int a = 0;
                    switch (args.length){
                        case 3:
                            //serverGUI.setColor(args[1], args[2]); TODO: Find out what this does
                            break;
                        case 5:
                            try{
                                r = Integer.parseInt(args[2]);
                                g = Integer.parseInt(args[3]);
                                b = Integer.parseInt(args[4]);
                            } catch (Exception e){
                                serverGUI.display("[Error] Provided color values are not valid");
                            }
                            if(r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255){
                                serverGUI.display("[Error] Provided color values are out of range (0 - 255)");
                            } else {
                                serverGUI.setColor(args[1], new Color(r, g, b));
                            }
                            break;
                        case 6:
                            try{
                                r = Integer.parseInt(args[2]);
                                g = Integer.parseInt(args[3]);
                                b = Integer.parseInt(args[4]);
                                a = Integer.parseInt(args[5]);
                            } catch (Exception e){
                                serverGUI.display("[Error] Provided color values are not valid");
                            }
                            if(r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255 || a < 0 || a > 255){
                                serverGUI.display("[Error] Provided color values are out of range (0 - 255)");
                            } else {
                                serverGUI.setColor(args[1], new Color(r, g, b, a));
                            }
                        default:
                            serverGUI.display("[Error] Usage: /color [component] [color name]");
                            serverGUI.display("               /color [component] [red] [green] [blue] (alpha)");
                            break;
                    }
                    break;
                case "quit":
                    if(args.length == 1) {
                        exit();
                    } else {
                        serverGUI.display("[Error] Usage: /quit");
                    }
                    break;
                case "exit":
                    if(args.length == 1) {
                        exit();
                    } else {
                        serverGUI.display("[Error] Usage: /exit");
                    }
                    break;
                case "stop":
                    if(args.length == 1) {
                        exit();
                    } else {
                        serverGUI.display("[Error] Usage: /stop");
                    }
                    break;
                default:
                    serverGUI.display("[Error] Command \""
                            + args[0].substring(1, args[0].length()) + "\" not recognized");
                    break;
            }
        } else {
            if(isRunning) {
                send("[" + serverName + "] " + msg);
            } else {
                serverGUI.display("[Error] Server not initialized: /start (port)");
            }
        }
    }

    public void send(String msg){
        broadcast(msg);
    }

    public synchronized void exit(){
        //serverGUI.display("[System] Stopping server");
        if (isRunning) {
            broadcast("[Server] Stopping");
            this.isRunning = false;

            try {
                serverSocket.close();
                for (int i = 0; i < clients.size(); i++) {
                    clients.get(i).close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        main.exit();
    }

    //for everyone
    public synchronized void broadcast(String msg){
        serverGUI.display(msg);
        for(int i = clients.size(); i > 0; i--){
            clients.get(i - 1).writeMessage(msg);
        }
    }

    public synchronized void remove(int id){
        for(int i = 0; i < clients.size(); i++){
            if(clients.get(i).getID() == id){
                clients.remove(i);
                return;
            }
        }
        System.out.println("[ERROR] Could not find client");
    }

    public synchronized String[] getUsernames(){
        String[] usernames = new String[clients.size()];
        for(int i = 0; i < usernames.length; i++){
            usernames[i] = clients.get(i).getUsername();
        }
        return usernames;
    }

    public boolean isRunning(){
        return isRunning;
    }

    public String getName(){
        return serverName;
    }

    private final String[] commandList = {
            "/help (command)",
            "/start (port)",
            "/exit /quit /exit",
            "/clear",
            "/color [component] [red] [green] [blue] (alpha)"
    };

}

/*
System.out.println(System.getProperty("user.name"));
        try {
            System.out.println(java.net.InetAddress.getLocalHost().getHostName());
            System.out.println(java.net.InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        new Scanner(System.in).nextLine();
 */
