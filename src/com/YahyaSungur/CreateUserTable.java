package com.YahyaSungur;

import java.sql.Connection;
import java.sql.Statement;

public class CreateUserTable {
    public static void main(String[] args) {
        Connection connection;
        Statement statement;

        ConnectDB obj_ConnectDB = new ConnectDB();
        connection = obj_ConnectDB.get_connection();

        try {
            String query ="create table users(name varchar(200),surname varchar(200),birthdate varchar(200),gender varchar(200),email varchar(200),password varchar(200))";
            statement = connection.createStatement();
            statement.executeUpdate(query);
            System.out.println("User table was created");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
