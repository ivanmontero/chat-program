import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

@SuppressWarnings("serial")
public class Main extends JPanel implements ActionListener {

    public static final int TIME_INTERVAL = 16;
    private JFrame frame;
    private Timer timer;
    private Server server;
    private Thread serverThread;
    private int initialX, initialY, minHeight;
    private ServerGUI serverGUI;
    private boolean initialized;

    public Main() {
        setUpGUI();
        setUpServer();
    }

    public void setUpGUI(){
        frame = new JFrame();
        frame.setUndecorated(true);
        frame.add(this);
        frame.setSize(500, 350);
        frame.setLocation((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2 - frame.getWidth()/2,
                (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2 - frame.getHeight()/2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(true);

        serverGUI = new ServerGUI(this);

        timer = new Timer(TIME_INTERVAL, this);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                initialX = me.getX();
                initialY = me.getY();
            }
            @Override
            public void mouseDragged(MouseEvent me) {
                frame.setLocation(frame.getLocation().x + me.getX() - initialX,
                        frame.getLocation().y + me.getY() - initialY);
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                frame.setLocation(frame.getLocation().x + e.getX() - initialX,
                        frame.getLocation().y + e.getY() - initialY);
            }
        });
        this.addMouseListener(new MouseInput(serverGUI));
        this.addMouseMotionListener(new MouseInput(serverGUI));
        this.addMouseWheelListener(new MouseInput(serverGUI));
        this.addKeyListener(new KeyboardInput(serverGUI));
        this.setFocusTraversalKeysEnabled(false);
        this.setFocusable(true);
        this.grabFocus();
        timer.start();
        minHeight = 160;
        initialized = true;
    }

    public void setUpServer(){
        server = new Server(this, serverGUI);
        serverGUI.setServer(server);
    }

    public void startServer(){
        serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                server.connect();
                //server.start();
            }
        }, "serverThread");
        serverThread.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        serverGUI.update();
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if(initialized)
            serverGUI.render(g2d);
    }

    public void exit() {
        timer.stop();
        frame.dispose();
    }

    public void setHeight(int height){
        if(height >= minHeight) {
            frame.setSize(getWidth(), height);
        }
    }

    public static void main(String[] args) {
        //new Main();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Main();
            }
        });
    }

    private class MouseInput extends MouseAdapter{

        private ServerGUI serverGUI;

        public MouseInput(ServerGUI serverGUI){
            this.serverGUI = serverGUI;
        }

        @Override
        public void mousePressed(MouseEvent e) {}

        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mouseMoved(MouseEvent e) {}

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            serverGUI.mouseWheelMoved(e);
        }
    }

    private class KeyboardInput extends KeyAdapter{

        private ServerGUI serverGUI;

        public KeyboardInput(ServerGUI serverGUI){
            this.serverGUI = serverGUI;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            serverGUI.keyPressed(e);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            serverGUI.keyReleased(e);
        }

        @Override
        public void keyTyped(KeyEvent e) {
            serverGUI.keyTyped(e);
        }
    }

}



































