import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

@SuppressWarnings("serial")
public class Main extends JPanel implements ActionListener {

    public static final int TIME_INTERVAL = 16;
    private JFrame frame;
    private Timer timer;
    private int initialX, initialY, minHeight;
    private ClientGUI clientGUI;
    private Client client;
    private boolean initialized;

    public Main() {
        setUpGUI();
        setUpClient();
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
        clientGUI = new ClientGUI(this);
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
        this.addMouseListener(new MouseInput(clientGUI));
        this.addMouseMotionListener(new MouseInput(clientGUI));
        this.addMouseWheelListener(new MouseInput(clientGUI));
        this.addKeyListener(new KeyboardInput(clientGUI));
        this.setFocusTraversalKeysEnabled(false);
        this.setFocusable(true);
        this.grabFocus();
        timer.start();
        minHeight = 160;
        initialized = true;
    }

    public void setUpClient(){
        this.client = new Client(this, clientGUI);
        clientGUI.setClient(client);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        clientGUI.update();
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if(initialized)
            clientGUI.render(g2d);
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

        private ClientGUI clientGUI;

        public MouseInput(ClientGUI clientGUI){
            this.clientGUI = clientGUI;
        }

        @Override
        public void mousePressed(MouseEvent e) {}

        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mouseMoved(MouseEvent e) {}

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            clientGUI.mouseWheelMoved(e);
        }
    }

    private class KeyboardInput extends KeyAdapter{

        private ClientGUI clientGUI;

        public KeyboardInput(ClientGUI clientGUI){
            this.clientGUI = clientGUI;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            clientGUI.keyPressed(e);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            clientGUI.keyReleased(e);
        }

        @Override
        public void keyTyped(KeyEvent e) {
            clientGUI.keyTyped(e);
        }
    }

}



































