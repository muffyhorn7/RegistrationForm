package com.theironyard.javawithclojure.jhporter;

import jodd.json.JsonParser;
import jodd.json.JsonSerializer;
import org.h2.tools.Server;
import spark.Spark;

import java.sql.*;
import java.util.ArrayList;

public class Main
{


    public static void createTables(Connection conn) throws SQLException
    {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, username VARCHAR, address VARCHAR, email VARCHAR)");
    }

    public static void insertUser(Connection conn, User user) throws SQLException
    {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES(NULL, ?,?,?)");
        stmt.setString(1,user.username);
        stmt.setString(2,user.address);
        stmt.setString(3,user.email);
        stmt.execute();
    }

    public static ArrayList<User> selectUsers(Connection conn) throws SQLException
    {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users");
        ResultSet results = stmt.executeQuery();
        ArrayList<User> users = new ArrayList<>();
        while(results.next())
        {
            int id = results.getInt("id");
            String username = results.getString("username");
            String address = results.getString("address");
            String email = results.getString("email");
            User user = new User(id,username,address,email);
            users.add(user);
        }
        return users;
    }

    public static void updateUser(Connection conn, User user) throws SQLException
    {
        PreparedStatement stmt = conn.prepareStatement("UPDATE users SET username = ?, address = ?, email = ?");
        stmt.setString(1,user.username);
        stmt.setString(2,user.address);
        stmt.setString(3,user.email);
        stmt.execute();
    }

    public static void deleteUser(Connection conn, int userid) throws SQLException
    {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?");
        stmt.setInt(1,userid);
        stmt.execute();
    }


    public static void main(String[] args) throws SQLException
    {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);

        Spark.externalStaticFileLocation("public");
        Spark.init();


        Spark.get(
                "/get-messages",
                (request, response) -> {
                    ArrayList<User> users = selectUsers(conn);
                    JsonSerializer s = new JsonSerializer();
                    return s.serialize(users);

                }
        );

        Spark.post(
                "/add-message",
                (request, response) -> {
                    String body = request.body();
                    JsonParser p = new JsonParser();
                    User user = p.parse(body,User.class);
                    insertUser(conn,user);
                    return"";
                }
        );
        Spark.put(
                "/edit-message",
                (request, response) ->
                {
                    String body = request.body();
                    JsonParser p = new JsonParser();
                    User user = p.parse(body,User.class);
                    insertUser(conn,user);
                    return"";
                }
        );

        Spark.delete(
                "/delete-message",
                (request, response) ->
                {
                    String body = request.body();
                    JsonParser p = new JsonParser();
                    User user = p.parse(body,User.class);
                    deleteUser(conn,user.id);
                    return"";
                }
        );
    }
}
