package com.YahyaSungur;


import org.apache.commons.lang3.StringUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Objects;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class Server{

    private static ArrayList<ClientHandler> clients = new ArrayList<>();


    public static void main(String[] args) {
        ServerSocket server = null;
        try {
            server = new ServerSocket(8818);
            server.setReuseAddress(true);

            while (true){
                System.out.println("About to accept client connection...");
                Socket client = server.accept();
                System.out.println("New client connected " + client.getInetAddress().getHostAddress());
                ClientHandler clientSock = new ClientHandler(client);
                clients.add(clientSock);
                new Thread(clientSock).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null){
                try {
                    server.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ClientHandler implements Runnable{

        private final Socket clientSocket;
        private String login = null;
        private boolean adminLogin = false;
        private boolean IsLogin = false;

        Connection connection;
        Statement statement;

        public ClientHandler(Socket socket){
            this.clientSocket = socket;
            ConnectDB obj_ConnectDB = new ConnectDB();
            connection = obj_ConnectDB.get_connection();
        }

        @Override
        public void run() {
            PrintWriter out = null;
            BufferedReader in = null;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String line;
                while ((line = in.readLine()) != null){
                    String[] tokens = StringUtils.split(line,'|');
                    System.out.printf("Sent from client: %s\n", line);
                    if (tokens != null && tokens.length > 0){
                        String cmd =tokens[0];
                        if("logoff".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd)){
                            String msg = "You have successfully logged out.";
                            out.println(msg);
                            out.flush();
                            System.out.println("One user logged out");
                            this.IsLogin = false;
                            //clientSocket.close();
                            break;
                        }
                        else if ("admin".equalsIgnoreCase(cmd) && tokens.length == 4){
                            if ("login".equalsIgnoreCase(tokens[1])){
                                String login = tokens[2];
                                String password = tokens[3];
                                if (checkadmin(login,password)){
                                    String msg = "You have successfully logged in as --ADMIN--.";
                                    out.println(msg);
                                    out.flush();
                                    this.login = login;
                                    this.adminLogin = true;
                                    System.out.println("Admin logged in: " + login);
                                }
                            }

                        }
                        else if ("login".equalsIgnoreCase(cmd) && tokens.length == 3){
                            String login = tokens[1];
                            String password = tokens[2];
                            if (check(login,password)){
                                String msg = "You have successfully logged in.";
                                out.println(msg);
                                out.flush();
                                this.login = login;
                                this.IsLogin = true;
                                System.out.println("User logged in: " + login);
                            }
                            else {
                                out.println("Incorrect email or password");
                                out.flush();
                            }
                        }
                        else if ("msg".equalsIgnoreCase(cmd)) {
                            if (!IsLogin){
                                out.println("You cannot send a message without logging in. Please login first.");
                                out.flush();
                                continue;
                            }
                            String[] tokensMsg = StringUtils.split(line, "|", 3);
                            Message message = new Message(login,tokensMsg[1],tokensMsg[2]);
                            create_msg(message);
                        }
                        else if ("create".equalsIgnoreCase(cmd) && tokens.length == 7){
                            if (!this.adminLogin){
                                out.println("This command requires admin privileges, please login as --ADMIN--");
                                continue;
                            }
                            String mail = tokens[5];
                            if(isThereAnyMessageContainsMail(mail)){
                                out.println("This e-mail address is in use, please try another one.");
                                continue;
                            }
                            User user = new User(tokens[1],tokens[2],tokens[3],tokens[4],tokens[5],tokens[6]);
                            add_user(user);
                        }
                        else if ("read".equalsIgnoreCase(cmd) && tokens.length == 1){
                            if (!this.adminLogin){
                                out.println("This command requires admin privileges, please login as --ADMIN--");
                                continue;
                            }
                            read_user();
                        }
                        else if ("update".equalsIgnoreCase(cmd) && tokens.length == 8){
                            if (!this.adminLogin){
                                out.println("This command requires admin privileges, please login as --ADMIN--");
                                continue;
                            }
                            String oldMail = tokens[1];
                            if (contains(oldMail)){
                                User user = new User(tokens[2],tokens[3],tokens[4],tokens[5],tokens[6],tokens[7]);
                                for (int i = 0 ; i < clients.size() ; i++){
                                    if(Objects.equals(clients.get(i).login, oldMail)){
                                        clients.get(i).clientSocket.close();
                                    }
                                }
                                if (!Objects.equals(oldMail, tokens[6])){
                                    updateMessages(oldMail,tokens[6]);
                                }
                                update_user(user,oldMail);
                            }
                            else {
                                out.println("No such user found.");
                            }

                        }
                        else if ("delete".equalsIgnoreCase(cmd) && tokens.length == 2){
                            if (!this.adminLogin){
                                out.println("This command requires admin privileges, please login as --ADMIN--");
                                continue;
                            }
                            String email = tokens[1];
                            if (contains(email)){
                                delete_user(email);
                            }
                            else {
                                out.println("No such user found.");
                            }

                        }
                        else if ("inbox".equalsIgnoreCase(cmd)){
                            if (!IsLogin){
                                out.println("You cannot see the inbox without logging in. Please login first.");
                                out.flush();
                                continue;
                            }
                            read_inbox(this.login);
                        }
                        else if ("outbox".equalsIgnoreCase(cmd)){
                            if (!IsLogin){
                                out.println("You cannot see the outbox without logging in. Please login first.");
                                out.flush();
                                continue;
                            }
                            read_outbox(this.login);
                        }
                        else{
                            String msg = "unknown command " + "'"+cmd+"'";
                            out.println(msg);
                            out.flush();
                        }
                    }
                }
                //clientSocket.close();
            } catch (IOException e){
                e.printStackTrace();
            } finally {
                try {
                    if (out != null){
                        out.close();
                    }
                    if (in != null){
                        in.close();
                    }
                    clientSocket.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

        private void updateMessages(String oldMail, String newMail) {
            try {
                String query = "update messages set msgfrom='"+newMail+"' where msgfrom='"+oldMail+"'";
                statement = connection.createStatement();
                statement.executeUpdate(query);
                String kuery = "update messages set msgto='"+newMail+"' where msgto='"+oldMail+"'";
                statement = connection.createStatement();
                statement.executeUpdate(kuery);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        private boolean isThereAnyMessageContainsMail(String mail) {
            ResultSet rs;
            try {
                String query ="select * from messages";
                statement = connection.createStatement();
                rs = statement.executeQuery(query);

                while (rs.next()){
                    if (Objects.equals(rs.getString("msgfrom"), mail) || Objects.equals(rs.getString("msgto"), mail)){
                        return true;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return false;
        }

        private boolean contains(String mail) {
            ResultSet rs;
            try {
                String query ="select * from users";
                statement = connection.createStatement();
                rs = statement.executeQuery(query);

                while (rs.next()){
                    if (Objects.equals(rs.getString("email"), mail)){
                        return true;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return false;
        }

        private void read_outbox(String login) {
            PrintWriter out;
            ResultSet rs;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(),true);
                String query ="select * from messages where msgfrom='"+login+"'";
                statement = connection.createStatement();
                rs = statement.executeQuery(query);

                while (rs.next()){
                    out.print(rs.getString("date") + " -- ");
                    out.print(rs.getString("msgto")+ " <-- :");
                    out.println(rs.getString("message")+ "\n");
                    out.flush();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        private void read_inbox(String login) {
            PrintWriter out;
            ResultSet rs;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(),true);
                String query ="select * from messages where msgto='"+login+"'";
                statement = connection.createStatement();
                rs = statement.executeQuery(query);

                while (rs.next()){
                    out.print(rs.getString("date") + " -- ");
                    out.print(rs.getString("msgfrom")+ " -- :");
                    out.println(rs.getString("message"));
                    out.flush();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        private void create_msg(Message message) {
            PrintWriter out;
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            try {
                out = new PrintWriter(clientSocket.getOutputStream(),true);
                String query = "insert into messages(msgfrom,msgto,message,date) values('"+message.getMsgFrom()+"','"+message.getMsgTo()+"','" +message.getBody()+ "','"+dtf.format(now)+"')";
                statement = connection.createStatement();
                statement.executeUpdate(query);
                out.println("Message sent.");
                out.flush();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        private boolean checkadmin(String username, String password) {
            ResultSet rs;
            try {
                String query ="select * from admins";
                statement = connection.createStatement();
                rs = statement.executeQuery(query);

                while (rs.next()){
                    if (Objects.equals(rs.getString("username"), username) && Objects.equals(rs.getString("password"), password)){
                        return true;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return false;
        }

        private boolean check(String login, String password) {
            ResultSet rs;
            try {
                String query ="select * from users";
                statement = connection.createStatement();
                rs = statement.executeQuery(query);

                while (rs.next()){
                    if (Objects.equals(rs.getString("email"), login) && Objects.equals(rs.getString("password"), password)){
                        return true;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return false;
        }

        private void delete_user(String email) {
            PrintWriter out;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(),true);
                String query = "delete from users where email='"+email+"'";
                statement = connection.createStatement();
                statement.executeUpdate(query);
                out.println("Deleted.");
                out.flush();
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        private void update_user(User user, String oldMail) {
            String name = user.getName();
            String surname = user.getSurname();
            String gender = user.getGender();
            String mail = user.getEmail();
            String birthdate = user.getBirthdate();
            String password = user.getPassword();

            PrintWriter out;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(),true);
                String query = "update users set name='"+name+"', surname='"+surname+"', birthdate='"+birthdate+"', gender ='"+gender+"', email ='"+mail+"', password='"+password+"' where email='"+oldMail+"'";
                statement = connection.createStatement();
                statement.executeUpdate(query);
                out.println("Updated.");
                out.flush();
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        private void read_user() {
            PrintWriter out;
            ResultSet rs;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(),true);
                String query ="select * from users";
                statement = connection.createStatement();
                rs = statement.executeQuery(query);

                while (rs.next()){
                    out.print(rs.getString("name") + " ");
                    out.print(rs.getString("surname")+ " ");
                    out.print(rs.getString("birthdate")+ " ");
                    out.print(rs.getString("gender")+ " ");
                    out.println(rs.getString("email"));
                    out.flush();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        private void add_user(User user) {
            PrintWriter out;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(),true);
                String query = "insert into users(name,surname,birthdate,gender,email,password) values('"+user.getName()+"','"+user.getSurname()+"','" +user.getBirthdate()+ "','" +user.getGender()+ "','"+user.getEmail()+"','"+user.getPassword()+"')";
                statement = connection.createStatement();
                statement.executeUpdate(query);
                out.println("User created.");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}