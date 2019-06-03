package WebAppServer;


import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class ToDatabase {
    private static Connection conn = connect();
    private static final int SUCCESS = 1;
    private static final int SERVER_FAILURE = -1;
    private static final int FAILURE = 0;
    private static final int USER_NOT_EXSIST = 2;
    private static final int INCORRECT_PWD = 3;

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
        try {
            Statement st = conn.createStatement();
            ResultSet userDetail = st.executeQuery("select * from users where userid = " + userId);
            if(userDetail.next()){
                String username = userDetail.getString(2);
                userDetail.close();
                st.close();
                return new User(userId, username);
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
            return SERVER_FAILURE;
        }

    }

    public static String login(String userIDEntered, String passwordEntered){
        int responseCode = 0;
        String userJson = "";
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
                    responseCode = SUCCESS;
                    userJson = JSONConvert.userToJSON(new User(userID, username, password));
                }
                else{
                    responseCode = INCORRECT_PWD;
                }
            }else{ //user not exist
                responseCode = USER_NOT_EXSIST;
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
        try {
            Statement st = conn.createStatement();
            String sqlString = String.format("UPDATE users SET friendreq = friendreq || '{%d}' WHERE userid = %d", senderid, friendid);
            int rowAffected = st.executeUpdate(sqlString);
            System.out.println("affected " + rowAffected +"rows");
            st.close();
            return SUCCESS;
        }catch (SQLException e){
            return SERVER_FAILURE;
        }
    }


    //TODO duplicates not handled
    public static Long[] getFriendRequestList(int id){
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
        try {
            Statement st = conn.createStatement();
            String sqlString = String.format("UPDATE users SET friendreq = array_remove(friendreq, '%d') WHERE userid=%d;", requestid, myid);
            st.execute(sqlString);
            st.close();
            return SUCCESS;

        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public static String addFriend(int myid, int requestid){
        try {
            Statement st = conn.createStatement();
            String baseString = "UPDATE users SET friends = array_append(friends, '%d') WHERE userid=%d;";
            st.execute(String.format(baseString, myid, requestid));
            st.execute(String.format(baseString, requestid, myid));
            String addInbox = "UPDATE users SET friendupdate = array_append(friendupdate, '%d') WHERE userid=%d;";
            st.execute(String.format(addInbox, myid, requestid));
            st.close();
            return JSONConvert.userToJSON(getUser(requestid));
        }catch (SQLException e){
            //or some universal error control
            return "failure";
        }
    }

    public static int deleteFriend(int myid, int requestid){
        try {
            Statement st = conn.createStatement();
            String baseString = "UPDATE users SET friends = array_remove(friends, '%d') WHERE userid=%d;";
            st.execute(String.format(baseString, myid, requestid));
            st.execute(String.format(baseString, requestid, myid));
            String addInbox = "UPDATE users SET friendupdate = array_append(friendupdate, '%d') WHERE userid=%d;";
            st.execute(String.format(addInbox, -myid, requestid));
            st.close();
            return SUCCESS;
        }catch (SQLException e){
            //or some universal error control
            return FAILURE;
        }
    }

    public static String getInbox(int userid){
        String arrayString = "failure";
        try {
            Statement st = conn.createStatement();
            String command = "select friendupdate from users where userid = " + userid;
            ResultSet resultSet = st.executeQuery(command);
            if(resultSet.next()){
                arrayString = resultSet.getString(1);
            }
            String baseString = "UPDATE users SET friendupdate = '{}' WHERE userid=%d;";
            st.execute(String.format(baseString, userid));
            resultSet.close();
            st.close();
        }catch (SQLException e){
            return "failure";
            //or some universal error control
        }
        return arrayString;
    }

    public static String getFriendId(int userid) {
        String arrayString = "failure";
        try {
            Statement st = conn.createStatement();
            String command = "select friends from users where userid = " + userid;
            ResultSet resultSet = st.executeQuery(command);
            if(resultSet.next()){
                arrayString = resultSet.getString(1);
            }
            resultSet.close();
            st.close();
        }catch (SQLException e){
            return "failure";
            //or some universal error control
        }
        return arrayString;
    }
}
