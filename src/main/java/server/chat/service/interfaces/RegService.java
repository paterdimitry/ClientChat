package server.chat.service.interfaces;

import java.sql.SQLException;

public interface RegService {
    void startRegistration() throws ClassNotFoundException, SQLException;
    void stopRegistration() throws SQLException;
    boolean registration(String login, String password, String username) throws SQLException;
    boolean isUsernameEmpty(String username)throws SQLException;
    boolean isLoginEmpty(String login) throws SQLException;
}
