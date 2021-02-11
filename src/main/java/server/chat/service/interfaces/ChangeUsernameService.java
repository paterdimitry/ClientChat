package server.chat.service.interfaces;

import java.sql.SQLException;

public interface ChangeUsernameService {
    void startAuthentication() throws ClassNotFoundException, SQLException;
    void stopAuthentication() throws SQLException;
    String updateUsername(String username, String password, String newUsername) throws SQLException;
    boolean isUsernameEmpty(String newUsername) throws SQLException;
}
