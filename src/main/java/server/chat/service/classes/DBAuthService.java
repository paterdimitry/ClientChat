package server.chat.service.classes;

import server.chat.service.interfaces.AuthService;

import java.sql.*;

public class DBAuthService implements AuthService {


    private Connection connection;
    private Statement stmt;
    private ResultSet rs;

    @Override
    public String getUsernameByLogin(String login, String password) throws SQLException {
        try {
            startAuthentication();
            rs = stmt.executeQuery(String.format("SELECT password, username FROM auth WHERE login = '%s';", login));
            if (rs.isClosed())
                return null;
            String passwordDB = rs.getString("password");
            if (passwordDB != null)
                return (password.equals(passwordDB)) ? rs.getString("username") : null;
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        } finally {
            stopAuthentication();
        }
    }

    @Override
    public void startAuthentication() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/database/maindb.db");
        stmt = connection.createStatement();
    }

    @Override
    public void stopAuthentication() throws SQLException {
        connection.close();
    }
}
