package com.YahyaSungur;

import java.sql.Connection;
import java.sql.Statement;

public class CreateMessageTable {
    public static void main(String[] args) {
        Connection connection;
        Statement statement;

        ConnectDB obj_ConnectDB = new ConnectDB();
        connection = obj_ConnectDB.get_connection();

        try {
            String query ="create table messages(msgfrom varchar(200),msgto varchar(200),message varchar(200),date varchar(200))";
            statement = connection.createStatement();
            statement.executeUpdate(query);
            System.out.println("Message table was created");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

