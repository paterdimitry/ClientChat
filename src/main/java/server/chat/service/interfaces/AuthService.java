package server.chat.service.interfaces;

import java.sql.SQLException;

public interface AuthService {

    String getUsernameByLogin(String login, String password) throws SQLException;
    void startAuthentication() throws ClassNotFoundException, SQLException;
    void stopAuthentication() throws SQLException;

}
