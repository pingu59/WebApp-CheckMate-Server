package WebAppServer;


import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class ToDatabase {

    private static Connection connect(){
        final String url = "jdbc:postgresql://db.doc.ic.ac.uk:5432/g1827127_u";
        final String account = "g1827127_u";
        final String password = "kuI1yeTGHW";
        Connection connection = null;
        try{
            connection = DriverManager.getConnection(url, account, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return connection;
    }

    public static String encrypt(String password){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update(password.getBytes("utf8"));
            return String.format("%064x", new BigInteger(1, digest.digest()));
        }catch (NoSuchAlgorithmException | UnsupportedEncodingException e){
            // will never be here!!!!
            return null;
        }
    }

    public static User getUser(int userId){
        Connection conn = connect();
        try {
            Statement st = conn.createStatement();
            ResultSet userDetail = st.executeQuery("select * from users where userid = " + userId);
            if(userDetail.next()){
                String username = userDetail.getString(2);
                String password = userDetail.getString(3);
                userDetail.close();
                st.close();
                return new User(userId, username, password);
            }else{
                System.err.println("NO SUCH USER!!");
                userDetail.close();
                st.close();
                // CHANGE THIS !! REFACTOR
                return null;
            }
        }catch (SQLException e){
            //  System.out.println("Here");
            // CHANGE THIS ??
            throw new RuntimeException(e);
        }
    }

    public static int register(String username, String password){
        // add assertion to the length of the user name at xamarin!!
        Connection conn = connect();
        try {
            Statement st = conn.createStatement();
            ResultSet largestId = st.executeQuery("select count(*) from users");
            largestId.next();
            int thisId =  Integer.parseInt(largestId.getString(1));
            System.out.println(" " + thisId);
            String encryptedPwd = encrypt(password);
            System.out.println("thisId = "+ thisId + " encryptedPwd = " + encryptedPwd);
            System.out.println("INSERT INTO users VALUES (" + thisId +", '{" + username +
                    "}', '{" + encryptedPwd + "}')");
            int rowAffected = st.executeUpdate("INSERT INTO users VALUES (" + thisId +", '{" + username +
                    "}', '{" + encryptedPwd + "}')");
            System.out.println("affected " + rowAffected +"rows");
            largestId.close();
            st.close();
            return thisId;
        }catch (SQLException e){
            return -1;
        }

    }

    public static String login(String userIDEntered, String passwordEntered){
        int responseCode = 0;
        String userJson = "";
        Connection conn = connect();
        try {
            int userID = Integer.parseInt(userIDEntered);
            Statement st = conn.createStatement();
            ResultSet userDetail = st.executeQuery("select * from users where userid = " + userID);
            if(userDetail.next()){
                String username = userDetail.getString(2);
                String password = userDetail.getString(3);
                userDetail.close();
                st.close();
                String encryptedPasswordEntered = "{"+encrypt(passwordEntered)+"}";
                //if id and password matches, return all the info needed in json afterwards
                if(password.equals(encryptedPasswordEntered)){
                    responseCode = 1;
                    userJson = JSONConvert.userToJSON(new User(userID, username, password));
                }
                else{
                    responseCode = 3;
                }
            }else{ //user not exist
                responseCode = 2;
            }
            userDetail.close();
            st.close();
            return responseCode+"!"+userJson;
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    //TODO duplicates not handled
    public static int friendRequest(int senderid, int friendid){
        Connection conn = connect();
        try {
            Statement st = conn.createStatement();

            String sqlString = String.format("UPDATE users SET friendreq = friendreq || '{%d}' WHERE userid = %d", senderid, friendid);
            int rowAffected = st.executeUpdate(sqlString);
            System.out.println("affected " + rowAffected +"rows");
            st.close();
            return 1;
        }catch (SQLException e){
            return -1;
        }
    }


    //TODO duplicates not handled
    public static Long[] getFriendRequestList(int id){
        Connection conn = connect();
        try {
            Statement st = conn.createStatement();
            ResultSet userDetail = st.executeQuery("select * from users where userid = " + id);
            if(userDetail.next()){
                Array firendReqListArray = userDetail.getArray(5);
                Long[] friendReqList = (Long[]) firendReqListArray.getArray();
                userDetail.close();
                st.close();
                return friendReqList;
            }else{
                return new Long[0];
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    //TODO working on it
    public static int deleteFriendRequest(int myid, int requestid){
        Connection conn = connect();
        try {
            Statement st = conn.createStatement();
            String sqlString = String.format("UPDATE users SET friendreq = array_remove(friendreq, '%d') WHERE userid=%d;", requestid, myid);
            st.execute(sqlString);
            st.close();
            return 1;

        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

}
