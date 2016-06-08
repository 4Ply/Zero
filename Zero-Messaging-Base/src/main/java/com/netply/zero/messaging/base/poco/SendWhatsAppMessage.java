package com.netply.zero.messaging.base.poco;

public class SendWhatsAppMessage extends BaseMessage {
    private String toJID;
    private String message;


    public SendWhatsAppMessage(String toJID, String message) {
        this.toJID = toJID;
        this.message = message;
    }

    @Override
    public String getUUID() {
        return toJID;
    }

    public void setToJID(String toJID) {
        this.toJID = toJID;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public MessageType getType() {
        return MessageType.WHATSAPP_SEND;
    }

    @Override
    public String toString() {
        return "SendWhatsAppMessage{" +
                "toJID='" + toJID + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
