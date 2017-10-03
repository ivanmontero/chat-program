import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerGUI {

    private Main main;
    private volatile ArrayList<String> chat;
    private StringBuilder currentString;
    private HashMap<String, Color> colors;
    private Font font;
    private int maxDescent, maxAscent, currentDisplacement, followChatLocation, stringHeight, lastStringLocation,
            chatBoxSize, textSpace, charWidth;
    private boolean ctrlHeld, upHeld, downHeld, followChat;
    private String serverName;
    private Server server;

    private final int leftEdgeBorder = 5, rightEdgeBorder = 8, stringGap = 2;

    public ServerGUI(Main main){
        this.main = main;

        colors = new HashMap<String, Color>();
        colors.put("background", new Color(30, 30, 30));
        colors.put("chat_box", new Color(50, 50, 50));
        colors.put("chat_text", Color.GREEN);
        font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        maxAscent = main.getGraphics().getFontMetrics(font).getMaxAscent();
        maxDescent = main.getGraphics().getFontMetrics(font).getMaxDescent();
        stringHeight = maxAscent + maxDescent;
        textSpace = stringHeight + stringGap;
        currentDisplacement = 0;
        chatBoxSize = 21;
        charWidth = getAverageCharWidth();
        serverName = "Server";
        chat = new ArrayList<String>();
        chat.add("[System] Chat initialized");
        chat.add("[System] Start server with: /start [port]");
        chat.add("[System] View additional commands with: /help");
        currentString = new StringBuilder();
        currentString.append("[" + serverName + "] ");
        followChat = true;
    }

    public void update(){
        if(upHeld)
            currentDisplacement += 2;
        if(downHeld)
            currentDisplacement -= 2;
        if(chat.size() > 0) {
            //305
            followChatLocation = (main.getHeight() - chatBoxSize - maxDescent - stringGap)
                    - (chat.size() * textSpace);
        } else {
            followChatLocation = (main.getHeight() - chatBoxSize - maxDescent - stringGap)
                    - textSpace;
        }
        //System.out.println(currentDisplacement);
        if(currentDisplacement < followChatLocation){
            currentDisplacement = followChatLocation;
        } else if (currentDisplacement >= 0){
            if(chat.size() > (main.getHeight()- chatBoxSize) / textSpace) {
                currentDisplacement = 0;
            } else {
                currentDisplacement = followChatLocation;
            }
        }
        lastStringLocation = chat.size() * textSpace + currentDisplacement;
        if(chat.size() != 0) {
            followChat = lastStringLocation < main.getHeight() - chatBoxSize
                    && lastStringLocation > main.getHeight() - (chatBoxSize + textSpace);
        }
        //System.out.println(lastStringLocation);
    }

    public void render(Graphics2D g2d){
        g2d.setColor(colors.get("background"));
        g2d.fillRect(0, 0, main.getWidth(), main.getHeight());
        //g2d.setColor(chatBox);
        //g2d.drawString("ESC for settings", main.getWidth() - getStringWidth("ESC for settings"), 2 + maxAscent);
        g2d.setColor(colors.get("chat_text"));
        g2d.setFont(font);
        for(int i = 0; i < chat.size(); i++) {
            String string = chat.get(i);
            int currentStringY = (i + 1) * textSpace + currentDisplacement;
            if(currentStringY > -10 && currentStringY < main.getHeight() + 10) {
                g2d.drawString(string, leftEdgeBorder, currentStringY);
            }
        }
        if(!followChat){
            g2d.setColor(colors.get("chat_text"));
            g2d.drawLine(main.getWidth() - 5, main.getHeight() - chatBoxSize - 12, main.getWidth() - 5,
                    main.getHeight() - chatBoxSize - 2);
            g2d.drawLine(main.getWidth() - 5, main.getHeight() - chatBoxSize - 2, main.getWidth() - 2,
                    main.getHeight() - chatBoxSize - 5);
            g2d.drawLine(main.getWidth() - 5, main.getHeight() - chatBoxSize - 2, main.getWidth() - 8,
                    main.getHeight() - chatBoxSize - 5);
        }
        if(chat.size() > (main.getHeight()- chatBoxSize) / textSpace && currentDisplacement != 0){
            g2d.setColor(colors.get("chat_text"));
            g2d.drawLine(main.getWidth() - 5, 2, main.getWidth() - 5, 12);
            g2d.drawLine(main.getWidth() - 5, 2, main.getWidth() - 2, 5);
            g2d.drawLine(main.getWidth() - 5, 2, main.getWidth() - 8, 5);
        }
        //System.out.println(currentDisplacement);
        g2d.setColor(colors.get("chat_box"));
        g2d.fillRect(0, main.getHeight() - chatBoxSize, main.getWidth(), chatBoxSize);
        g2d.setColor(colors.get("chat_text"));
        g2d.drawString(currentString.toString(), leftEdgeBorder, main.getHeight() - (chatBoxSize/2 - maxDescent));
    }

    public synchronized void display(String msg){
        if(!(getStringWidth(msg) < main.getWidth() - (leftEdgeBorder + rightEdgeBorder))) {
            msg = trimString(msg, main.getWidth() - (leftEdgeBorder + rightEdgeBorder));
        }
        chat.add(msg);
        if (followChat) {
            currentDisplacement -= textSpace;
        }
    }

    public synchronized void clearChat(){
        chat.clear();
    }

    public void keyTyped(KeyEvent e){
        if(e.getKeyChar() != '\b' && getStringWidth(currentString.toString()) < main.getWidth()
                - (leftEdgeBorder + rightEdgeBorder + charWidth) && !ctrlHeld && e.getKeyChar() != '\n') {
            currentString.append(e.getKeyChar());
        }
    }

    public void keyPressed(KeyEvent e){
        switch (e.getKeyCode()){
            case KeyEvent.VK_ENTER:
                String s = currentString.toString();
                server.processInput(s.substring(s.indexOf("]") + 2));
                currentString = new StringBuilder();
                currentString.append("[" + serverName + "] ");
                break;
            case KeyEvent.VK_BACK_SPACE:
                if(currentString.charAt(currentString.length() - 2) != ']') {
                    currentString.deleteCharAt(currentString.length() - 1);
                }
                break;
            case KeyEvent.VK_UP:
                upHeld = true;
                break;
            case KeyEvent.VK_DOWN:
                downHeld = true;
                break;
            case KeyEvent.VK_CONTROL:
                ctrlHeld = true;
                break;
            case KeyEvent.VK_TAB:
                currentDisplacement = followChatLocation;
                break;
            case KeyEvent.VK_V:
                if(ctrlHeld){
                    try {
                        String clipboardData = (String) Toolkit.getDefaultToolkit()
                                .getSystemClipboard().getData(DataFlavor.stringFlavor);
                        currentString.append(clipboardData);
                        String trimmedString = trimString(currentString.toString(),
                                main.getWidth() - (leftEdgeBorder + rightEdgeBorder));
                        currentString = new StringBuilder();
                        currentString.append(trimmedString);
                    } catch (Exception ex){
                        System.out.println("Error getting clipboard contents");
                    }
                }
                break;
        }
    }

    public void keyReleased(KeyEvent e){
        switch (e.getKeyCode()){
            case KeyEvent.VK_UP:
                upHeld = false;
                break;
            case KeyEvent.VK_DOWN:
                downHeld = false;
                break;
            case KeyEvent.VK_CONTROL:
                ctrlHeld = false;
                break;
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e){
        if(ctrlHeld){
            int frameHeight = main.getHeight();
            frameHeight += e.getWheelRotation() * textSpace;
            main.setHeight(frameHeight);
        } else {
            currentDisplacement -= e.getWheelRotation() * textSpace;
        }
    }

    private int getStringWidth(String string){
        return main.getGraphics().getFontMetrics(font).stringWidth(string);
    }

    private int getAverageCharWidth(){
        return main.getGraphics().getFontMetrics(font)
                .stringWidth("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz")/62;
    }

    private String trimString(String string, int maxWidth){
        StringBuilder modifiedString = new StringBuilder();
        modifiedString.append(string);
        while(getStringWidth(modifiedString.toString()) > maxWidth){
            modifiedString.deleteCharAt(modifiedString.length() - 1);
        }
        return modifiedString.toString();
    }

    public ArrayList<String> splitString(String string, int maxWidth){
        StringBuilder modifiedString = new StringBuilder();
        modifiedString.append(string);

        //TODO
        return null;
    }

    private void setColor(String key, String color){
        if(colors.get(key) != null){
            switch(color.toLowerCase()){
                case "black":
                    colors.put(key, Color.BLACK);
                    display("[System] " + key + " changed to " + color);
                    break;
                case "blue":
                    colors.put(key, Color.BLUE);
                    display("[System] " + key + " changed to " + color);
                    break;
                case "cyan":
                    colors.put(key, Color.CYAN);
                    display("[System] " + key + " changed to " + color);
                    break;
                case "dark_gray":
                    colors.put(key, Color.DARK_GRAY);
                    display("[System] " + key + " changed to " + color);
                    break;
                case "gray":
                    colors.put(key, Color.GRAY);
                    display("[System] " + key + " changed to " + color);
                    break;
                case "green":
                    colors.put(key, Color.GREEN);
                    display("[System] " + key + " changed to " + color);
                    break;
                case "light_gray":
                    colors.put(key, Color.LIGHT_GRAY);
                    display("[System] " + key + " changed to " + color);
                    break;
                case "magenta":
                    colors.put(key, Color.MAGENTA);
                    display("[System] " + key + " changed to " + color);
                    break;
                case "orange":
                    colors.put(key, Color.ORANGE);
                    display("[System] " + key + " changed to " + color);
                    break;
                case "pink":
                    colors.put(key, Color.PINK);
                    display("[System] " + key + " changed to " + color);
                    break;
                case "red":
                    colors.put(key, Color.RED);
                    display("[System] " + key + " changed to " + color);
                    break;
                case "white":
                    colors.put(key, Color.WHITE);
                    display("[System] " + key + " changed to " + color);
                    break;
                case "yellow":
                    colors.put(key, Color.YELLOW);
                    display("[System] " + key + " changed to " + color);
                    break;
                default:
                    display("[Error] " + color + " is not a valid color");
                    break;
            }
        } else {
            display("[Error] \"" + key + "\" is not a valid component");
            return;
        }
    }

    public void setColor(String key, Color color){
        if(colors.get(key) != null){
            colors.put(key, color);
            display("[System] " + key + " changed to " + color.getRed() + " " + color.getGreen() + " " + color.getBlue()
                    + " " + color.getAlpha());
        } else {
            display("[Error] \"" + key + "\" is not a valid component");
            return;
        }
    }

    public void setServer(Server server){
        this.server = server;
        this.serverName = server.getName();
    }

}
