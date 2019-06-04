package WebAppServer;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ToDatabase {
    private static Connection conn = connect();
    private static final int SERVER_FAILURE = -1;
    private static final int SUCCESS = 1;
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

    public static String getFriends(int userid){
        try {
            Statement st = conn.createStatement();
            String command = "select friends from users where userid = " + userid;
            ResultSet resultSet = st.executeQuery(command);
            if(resultSet.next()){
                Array array = resultSet.getArray(1);
                Long[] IDs = (Long[]) array.getArray();
                resultSet.close();
                List<Friend> friendList = new ArrayList <>();
                for(Long id: IDs){
                    String findFriend = "select username from users where userid = " + id;
                    ResultSet friendResult = st.executeQuery(findFriend);
                    if(friendResult.next()){
                        String friendName = friendResult.getString(1);
                       friendList.add(new Friend(id.intValue(), friendName));
                    }
                }
                st.close();
                return JSONConvert.friendsToJSON(friendList);
            }
            else{
                return "NULL";
            }
        }catch (SQLException e){
            return "failure";
        }
    }


    //create task
    public static int createIndvTask(int myId, String taskName, String repetition, int frequency, int[] supervisors, String date){
        try {
            //connect
            Statement st = conn.createStatement();

            // cannot supervise your self
            if(Arrays.stream(supervisors).anyMatch(x -> x == myId)) {
                return SERVER_FAILURE;
            }

            //get taskid
            ResultSet largestId = st.executeQuery("select max(taskid) from individual");
            largestId.next();
            String taskNum = largestId.getString(1);
            int taskId;
            if (taskNum == null) {
                taskId = 0;
            } else {
                taskId =  Integer.parseInt(taskNum) + 1;
            }

            //create task update individual table
            String supvStr = Arrays.toString(supervisors);
            int last = supvStr.length() - 1;
            supvStr = supvStr.substring(1, last);
            int rowAffected = st.executeUpdate("INSERT INTO individual VALUES (" + taskId + ", " + myId + ", '{" + taskName +
                    "}', '{" + repetition + "}' , 0, " + frequency + ", '{ " + supvStr + "}' , '" + date + "' )");
            System.out.println("insert  " + rowAffected +" rows into individual");

            //update user table for the owner
            String updateMyTask = "UPDATE users SET myindividual = array_append(myindividual, '%d') WHERE userid=%d;";
            st.execute(String.format(updateMyTask, taskId, myId));

            String updateSupvTask = "UPDATE users SET superviseindividual = array_append(superviseindividual, '%d') WHERE userid=%d;";
            String updateNewSupv = "UPDATE users SET newindividualinvite = array_append(newindividualinvite, '%d') WHERE userid=%d;";
            for (int id : supervisors) {
                //add taskid into superviseindividual for supervisors
                st.execute(String.format(updateSupvTask, taskId, id));
                //add taskid into newindividualinvite for supervisors
                st.execute(String.format(updateNewSupv, taskId, id));
            }
            st.close();
            return taskId;
        }catch (SQLException e){
            return SERVER_FAILURE;
        }
    }

    public static int deleteIndvTask(int taskId){
        try {
            Statement st = conn.createStatement();

            //find task, userid, supervisors in individual
            int myId;
            Long[] supvIds;
            String findUserId = "select * from individual where taskid = " + taskId;
            ResultSet resultSet = st.executeQuery(findUserId);
            System.out.println("findUserId");
            if(resultSet.next()){
                myId = Integer.parseInt(resultSet.getString("userid"));
                System.out.println("get id");
                supvIds = (Long[]) resultSet.getArray("supervisors").getArray();
                resultSet.close();

                System.out.println("get supv");
                //delete task in individual
                st.executeUpdate("DELETE FROM individual WHERE taskid = " + taskId);
                System.out.println("delete task");
            }
            else{
                return SERVER_FAILURE;
            }

            //remove taskId from myIndv and supvIndv in user
            String updateMyIndv =
                    "UPDATE users SET myindividual = array_remove(myindividual, '%d') WHERE userid=%d;";
            st.execute(String.format(updateMyIndv, taskId, myId));
            String updateSupvIndv =
                    "UPDATE users SET superviseindividual = array_remove(superviseindividual, '%d') WHERE userid=%d;";
            for (long supvId : supvIds) {
                st.execute(String.format(updateSupvIndv, taskId, supvId));
            }
            st.close();
            return SUCCESS;

        }catch (SQLException e){
            return SERVER_FAILURE;
        }
    }

    //get task info in new IndvInvite
    public static String getNewIndvInvite(int userId){
        try {
            Statement st = conn.createStatement();
            String getNewInvite = "select newindividualinvite from users where userid = " + userId;
            ResultSet newIndvInviteResult = st.executeQuery(getNewInvite);
            if(newIndvInviteResult.next()){
                Long[] newIndvTaskIds = (Long[]) newIndvInviteResult.getArray(1).getArray();
                newIndvInviteResult.close();
                String newIndvTasks = Arrays.toString(newIndvTaskIds);
                String indvInviteTaskInfo = "";
                int last = newIndvTasks.length() - 1;
                newIndvTasks = newIndvTasks.substring(1, last);
                //if there is no new invitation, return empty string
                if(newIndvTasks.length() == 0) {
                    return "empty";
                }
                String getInviteTaskInfo = "select * from individual where taskid in ( " + newIndvTasks + ")";
                ResultSet inviteTaskInfoResult = st.executeQuery(getInviteTaskInfo);
                JSONArray jsonArray = new JSONArray();
                String[] jasonIds =
                        {"taskID", "ownerID", "taskName", "repetition","frequency",  "deadline", "related"};
                String[] columnName =
                        {"taskid", "userid", "taskname", "repetition","frequency",  "deadline", "supervisors"};
                while(inviteTaskInfoResult.next()) {
                    JSONObject jObj = new JSONObject();
                    for(int c = 0; c < 7; c++) {
                        jObj.put(jasonIds[c], inviteTaskInfoResult.getObject(columnName[c]));
                    }
                    jsonArray.put(jObj);
                }
                st.close();
                return jsonArray.toString();
            } else {
                return "failure";
            }
        }catch (SQLException e){
            return "failure";
            //or some universal error control
        }

    }

    //clear newIndvInvite
    public static int clearNewIndvInvite(int userId){
        try {
            Statement st = conn.createStatement();
            String updateMyIndv = "UPDATE users SET newindividualinvite = '{}' WHERE userid=" + userId;
            st.executeUpdate(updateMyIndv);
            st.close();
            return SUCCESS;
        } catch (SQLException e) {
            return SERVER_FAILURE;
        }
    }


}
