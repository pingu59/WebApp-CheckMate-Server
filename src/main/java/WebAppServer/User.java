package WebAppServer;

public class User {
    private int userId;
    private String username, password;
    public User(int userId, String username, String password){
        this.userId = userId;
        this.password = password;
        this.username = username;
    }

    public User(int userId, String username){
        this.userId = userId;
        this.username = username;
    }

    public int getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User){
            return ((User) obj).userId == userId &&
                    ((User) obj).password == password;
        }else{
            return false;
        }
    }

    @Override
    public int hashCode() {
        return password.hashCode() * userId;
    }

    @Override
    public String toString() {
        return username + " " + password;
    }
}
