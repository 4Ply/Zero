package com.netply.zero.messaging.base.poco;

public abstract class ReceivedWhatsAppMessage extends BaseReplyableMessage {
    private final String fromJID;
    private final String authorJID;
    private final String message;


    public ReceivedWhatsAppMessage(String fromJID, String authorJID, String message) {
        this.fromJID = fromJID;
        this.authorJID = authorJID;
        this.message = message;
    }

    @Override
    public String getUUID() {
        return fromJID;
    }

    public String getAuthorJID() {
        return authorJID;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public boolean isGroupMessage() {
        return !getUUID().equals(getAuthorJID());
    }

    @Override
    public MessageType getType() {
        return MessageType.WHATSAPP_RECEIVED;
    }

    @Override
    public String toString() {
        return "ReceivedWhatsAppMessage{" +
                "fromJID='" + fromJID + '\'' +
                ", authorJID='" + authorJID + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
