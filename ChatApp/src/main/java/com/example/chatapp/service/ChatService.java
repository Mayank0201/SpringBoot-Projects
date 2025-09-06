package com.example.chatapp.service;

import com.example.chatapp.model.Message;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    private final List<Message> messages=new ArrayList<>();

    public void sendMessage(Message message){
        messages.add(message);
        System.out.println(message.getSender().name()+" sent a message to "+
                message.getReceiver().name()+
                ": "+message.getContent()
        );
    }

    public void displayChatHistory(){
        System.out.println("Chat History: ");
        for(Message message:messages){
            System.out.println(message.getSender().name()+" sent a message to "+
                    message.getReceiver().name()+
                    ": "+message.getContent());
        }
    }

}
