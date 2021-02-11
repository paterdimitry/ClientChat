package server.chat.service.classes;

import server.chat.service.interfaces.ChangeUsernameService;

import java.sql.*;

public class DBChangeUsernameService implements ChangeUsernameService {

    private Connection connection;
    private Statement stmt;
    private ResultSet rs;

    @Override
    public String updateUsername(String username, String password, String newUsername) throws SQLException {
        try {
            startAuthentication();
            rs = stmt.executeQuery(String.format("SELECT password, ID FROM auth WHERE username = '%s';", username));
            if (rs.isClosed())
                return null;
            String passwordDB = rs.getString("password");
            String id = rs.getString("ID");
            if (passwordDB.equals(password) & id != null) {
                stmt.executeUpdate(String.format("UPDATE auth SET username = '%s' WHERE ID = '%s';", newUsername.replace(" ", "_"), id));
                return stmt.executeQuery(String.format("SELECT username FROM auth WHERE ID = '%s';", id)).getString("username");
            } else {
                return null;
            }
        } catch (ClassNotFoundException e) {
            return null;
        } finally {
            stopAuthentication();
        }
    }

    @Override
    public boolean isUsernameEmpty(String newUsername) throws SQLException {
        try {
            startAuthentication();
            if (stmt.executeQuery(String.format("SELECT username FROM auth WHERE username = '%s';", newUsername)).isClosed())
                return true;
        } catch (ClassNotFoundException e) {
            return false;
        } finally {
            stopAuthentication();
        }
        return false;
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
