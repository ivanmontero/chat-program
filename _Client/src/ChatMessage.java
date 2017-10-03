import java.io.Serializable;

public class ChatMessage implements Serializable {

    public static final int MESSAGE = 0, USERLIST = 1, COMMAND = 2, LOGOUT = 3;
    private static final long serialVersionUID = 5487853389078563279L;

    private int type;
    private String message;

    public ChatMessage(int type, String message){
        this.type = type;
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
