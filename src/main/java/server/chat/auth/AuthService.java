package server.chat.auth;

public interface AuthService {

    String getUsernameByLogin(String login, String password);
    void startAuthentication();
    void stopAuthentication();
}
