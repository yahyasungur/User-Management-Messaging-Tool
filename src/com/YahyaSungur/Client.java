package com.YahyaSungur;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args){
        String host = "localhost";
        int port = 8818;
        try (Socket socket = new Socket(host,port)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in);
            String line;
            System.out.println("Use the 'help' command to see the list of commands.");
            while (true){
                line = scanner.nextLine();
                if ("help".equalsIgnoreCase(line)){
                    System.out.println("    ...REGULAR USER...\n" +
                            "    login|<email>|<password>\n" +
                            "    logoff\n" +
                            "    msg|<recipient's email>|<body of message>\n" +
                            "    inbox\n"+
                            "    outbox\n\n"+
                            "    ...ADMINS...\n"+
                            "    admin|login|<username>|<password>\n"+
                            "    create|<name>|<surname>|<birthdate>|<gender>|<email>|<password>\n"+
                            "    read\n"+
                            "    update|<email of updated user>|<name>|<surname>|<birthdate>|<gender>|<email>|<password>\n"+
                            "    delete|<email of user>");
                    continue;
                }
                out.println(line);
                out.flush();
                System.out.println("Server response: " + in.readLine());
                if ("logoff".equalsIgnoreCase(line)){
                    break;
                }
            }
            socket.close();
            scanner.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}

