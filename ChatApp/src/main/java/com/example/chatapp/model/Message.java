package com.example.chatapp.model;

public class Message {
//can be also made record class like User since all final vars with getters and setters and constructor
    private final String content;
    private final User sender;
    private final User receiver;

    public Message(String content, User sender, User receiver) {
        this.content = content;
        this.sender = sender;
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

}
