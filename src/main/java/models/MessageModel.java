package models;

import org.jivesoftware.smack.packet.Message.Type;

public class MessageModel {
    private String from;
    private String to;
    private String message;
    private Type type;

    public MessageModel(String from, String to, String message, Type type) {
        this.from = from;
        this.to = to;
        this.message = message;
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getMessage() {
        return message;
    }

    public Type getType() {
        return type;
    }

    public String toString() {
        return "ChatModel{" + "from=" + from + ", to=" + to + ", message=" + message + ", type=" + type + '}';
    }
}
