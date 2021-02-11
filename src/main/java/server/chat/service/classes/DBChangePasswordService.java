package server.chat.service.classes;

import server.chat.service.interfaces.ChangePasswordService;

import java.sql.*;

public class DBChangePasswordService implements ChangePasswordService {
    private Connection connection;
    private Statement stmt;
    private ResultSet rs;

    @Override
    public boolean updatePassword(String username, String password, String newPassword) throws SQLException {
        try {
            startAuthentication();
            rs = stmt.executeQuery(String.format("SELECT password, ID FROM auth WHERE username = '%s';", username));
            if (rs.isClosed())
                return false;
            String passwordDB = rs.getString("password");
            String id = rs.getString("ID");
            if (passwordDB.equals(password) & id != null) {
                stmt.executeUpdate(String.format("UPDATE auth SET password = '%s' WHERE ID = '%s';", newPassword, id));
                return true;
            } else {
                return false;
            }
        } catch (ClassNotFoundException e) {
            return false;
        } finally {
            stopAuthentication();
        }
    }

    public String getPassword(String username) throws SQLException {
        try {
            startAuthentication();
            return stmt.executeQuery(String.format("SELECT password FROM auth WHERE username = '%s';", username)).getString("password");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            stopAuthentication();
        }
        return null;
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
