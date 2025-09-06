package com.example.chatapp.controller;

import com.example.chatapp.model.Message;
import com.example.chatapp.model.User;
import com.example.chatapp.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Scanner;

@Controller
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    public void startChat(){
        Scanner sc=new Scanner(System.in);

        User user1=new User("John");
        User user2=new User("Jane");

        while(true){
            System.out.println("Who is sending the message: (John/Jane)");
            String senderName=sc.nextLine();

            System.out.println("Enter your message: ");
            String content=sc.nextLine();

            User sender=senderName.equalsIgnoreCase("John")?user1:user2;
            //elvis operator , if equal to john , user1 else user2

            User receiver=senderName.equalsIgnoreCase("Jane")?user1:user2;

            Message message=new Message(content,sender,receiver);

            chatService.sendMessage(message);

            System.out.println("Type History to view chat history ," +
                    " continue to send another message and " +
                    "exit to leave the service");

            String command=(sc.nextLine()).toLowerCase();

            switch(command){
                case "history":
                    chatService.displayChatHistory();
                    break;

                case "continue":
                    continue;

                case "exit":
                    System.out.println("Thanks for using the service, see you later!");
                    break;

                default:
                    System.out.println("Invalid command");
                    break;
            }

            if (command.equals("exit")) {
                break;
            }

        }

    }

}
