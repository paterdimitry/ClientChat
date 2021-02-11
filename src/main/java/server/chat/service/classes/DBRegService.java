package server.chat.service.classes;

import server.chat.service.interfaces.RegService;

import java.sql.*;

public class DBRegService implements RegService {

    private Connection connection;
    private Statement stmt;

    @Override
    public void startRegistration() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/database/maindb.db");
        stmt = connection.createStatement();
    }

    @Override
    public void stopRegistration() throws SQLException {
        connection.close();
    }

    @Override
    public boolean registration(String login, String password, String username) throws SQLException {
        try {
            startRegistration();
            stmt.executeUpdate(String.format("INSERT INTO auth (login, password, username) VALUES ('%s', '%s', '%s');",login, password, username));
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            stopRegistration();
        }

        return false;
    }

    @Override
    public boolean isUsernameEmpty(String username) throws SQLException {
        try {
            startRegistration();
            if (stmt.executeQuery(String.format("SELECT username FROM auth WHERE username = '%s';", username)).isClosed())
                return true;
        } catch (ClassNotFoundException e) {
            return false;
        } finally {
            stopRegistration();
        }
        return false;
    }

    @Override
    public boolean isLoginEmpty(String login) throws SQLException {
        try {
            startRegistration();
            if (stmt.executeQuery(String.format("SELECT username FROM auth WHERE login = '%s'", login)).isClosed())
                return true;
        } catch (ClassNotFoundException e) {
            return false;
        } finally {
            stopRegistration();
        }
        return false;
    }
}
