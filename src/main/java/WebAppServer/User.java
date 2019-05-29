package WebAppServer;

public class User {
    private int userId;
    private String username, password;
    public User(int userId, String username, String password){
        this.userId = userId;
        this.password = password;
        this.username = username;
    }

    @Override
    public String toString() {
        return username + " " + password;
    }
}
