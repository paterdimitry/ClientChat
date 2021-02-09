package server.chat.auth;

import server.chat.User;

import java.util.List;

public class BaseAuthService implements AuthService {

    List<User> clients = List.of(
            new User("frodo", "111", "Фродор_Михайлович"),
            new User("pendalf", "123", "Гендальф_Серый"),
            new User("saruman", "321", "Сарумян")
    );

    @Override
    public String getUsernameByLogin(String login, String password) {
        for (User client : clients) {
            if (client.getLogin().equals(login) & client.getPassword().equals(password)) {
                return client.getUsername();
            }
        }
        return null;
    }

    @Override
    public void startAuthentication() {

    }

    @Override
    public void stopAuthentication() {

    }
}
