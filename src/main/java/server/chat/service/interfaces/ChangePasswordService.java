package server.chat.service.interfaces;

import java.sql.SQLException;

public interface ChangePasswordService {
    void startAuthentication() throws ClassNotFoundException, SQLException;
    void stopAuthentication() throws SQLException;
    boolean updatePassword(String username, String password, String newPassword) throws SQLException;
    String getPassword(String username) throws SQLException;
}
