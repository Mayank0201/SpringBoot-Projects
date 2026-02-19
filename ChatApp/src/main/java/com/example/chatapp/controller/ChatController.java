package com.example.chatapp.controller;

import com.example.chatapp.model.Message;
import com.example.chatapp.model.User;
import com.example.chatapp.service.ChatService;
import org.springframework.stereotype.Controller;

import java.util.Scanner;

@Controller
public class ChatController {

    private final ChatService chatService;
    private final Scanner scanner;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
        this.scanner = new Scanner(System.in);
    }

    public void startChat() {

        User user1 = new User("John");
        User user2 = new User("Jane");

        while (true) {

            System.out.println("Who is sending the message: (John/Jane)");
            if (!scanner.hasNextLine()) break;
            String senderName = scanner.nextLine();

            System.out.println("Enter your message:");
            if (!scanner.hasNextLine()) break;
            String content = scanner.nextLine();

            User sender = senderName.equalsIgnoreCase("John") ? user1 : user2;
            User receiver = senderName.equalsIgnoreCase("Jane") ? user1 : user2;

            Message message = new Message(content, sender, receiver);
            chatService.sendMessage(message);

            System.out.println("Type History to view chat history, continue to send another message and exit to leave the service");

            if (!scanner.hasNextLine()) break;
            String command = scanner.nextLine().toLowerCase();

            switch (command) {
                case "history":
                    chatService.displayChatHistory();
                    break;

                case "continue":
                    continue;

                case "exit":
                    System.out.println("Thanks for using the service, see you later!");
                    return;

                default:
                    System.out.println("Invalid command");
            }
        }
    }
}