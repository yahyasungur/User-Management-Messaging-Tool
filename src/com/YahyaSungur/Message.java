package com.YahyaSungur;

public class Message {
    private String msgFrom;
    private String msgTo;
    private String body;

    public Message(String from,String to,String body){
        this.msgFrom = from;
        this.msgTo = to;
        this.body = body;
    }

    public String getMsgFrom() {
        return msgFrom;
    }

    public void setMsgFrom(String msgFrom) {
        this.msgFrom = msgFrom;
    }

    public String getMsgTo() {
        return msgTo;
    }

    public void setMsgTo(String msgTo) {
        this.msgTo = msgTo;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
