package WebAppServer;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
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
            return getUserInfo(requestid);
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

    public static String getUserInfo(int userid){
        try {
            Statement st = conn.createStatement();
            String getUserInfo = "select * from users where userid = " + userid;
            ResultSet userInfo = st.executeQuery(getUserInfo);
            userInfo.next();
            System.out.println("1");
            JSONObject jobj = new JSONObject();
            System.out.println("2");
            jobj.put("FriendID", userInfo.getObject(1));
            jobj.put("FriendName", userInfo.getObject(2));
            System.out.println("3");
            st.close();
            return jobj.toString();
        } catch (Exception e) {
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
            if(resultSet.next()){
                myId = Integer.parseInt(resultSet.getString("userid"));
                supvIds = (Long[]) resultSet.getArray("supervisors").getArray();
                resultSet.close();

                //delete task in individual
                st.executeUpdate("DELETE FROM individual WHERE taskid = " + taskId);
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
                int last = newIndvTasks.length() - 1;
                newIndvTasks = newIndvTasks.substring(1, last);
                JSONArray jsonArray = new JSONArray();
                //if there is no new invitation, return empty string
                if(newIndvTasks.length() == 0) {
                    return jsonArray.toString();
                }
                String getInviteTaskInfo = "select * from individual where taskid in ( " + newIndvTasks + ")";
                ResultSet inviteTaskInfoResult = st.executeQuery(getInviteTaskInfo);

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

    public static String getAllMyFriendIndv(int userId){
        try {
            Statement st = conn.createStatement();
            String friendTask = "select superviseindividual from users where userid = " + userId;
            ResultSet tasks = st.executeQuery(friendTask);
            if(tasks.next()){
                Long[] indvTaskIds = (Long[]) tasks.getArray(1).getArray();
                tasks.close();
                String indvTasks = Arrays.toString(indvTaskIds);
                int last = indvTasks.length() - 1;
                indvTasks = indvTasks.substring(1, last);
                JSONArray jsonArray = new JSONArray();
                //if there is no new invitation, return empty string
                if(indvTasks.length() == 0) {
                    return jsonArray.toString();
                }
                String getInviteTaskInfo = "select * from individual where taskid in ( " + indvTasks + ")";
                ResultSet inviteTaskInfoResult = st.executeQuery(getInviteTaskInfo);

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

    public static String getAllMyIndv(int userId){
        try {
            Statement st = conn.createStatement();
            String mytasksStr = "select myindividual from users where userid = " + userId;
            ResultSet mytasks = st.executeQuery(mytasksStr);
            if(mytasks.next()){
                Long[] indvTaskIds = (Long[]) mytasks.getArray(1).getArray();
                mytasks.close();
                String indvTasks = Arrays.toString(indvTaskIds);
                int last = indvTasks.length() - 1;
                indvTasks = indvTasks.substring(1, last);
                JSONArray jsonArray = new JSONArray();
                //if there is no new invitation, return empty string
                if(indvTasks.length() == 0) {
                    return jsonArray.toString();
                }
                String getInviteTaskInfo = "select * from individual where taskid in ( " + indvTasks + ")";
                ResultSet inviteTaskInfoResult = st.executeQuery(getInviteTaskInfo);

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

    //add progress update, when a task owner does something, return update number of this task
    public static int addIndvProgressUpdate(int taskid, String image){
        try {
            Statement st = conn.createStatement();
            int updateNum;
            //select supervisors for this task
            String getSupervisors = "select supervisors from individual where taskid = " + taskid;
            ResultSet supvResult = st.executeQuery(getSupervisors);
            if(supvResult.next()) {
                Long[] supervisors = (Long[]) supvResult.getArray(1).getArray();
                supvResult.close();
                //find number of update for this task
                ResultSet maxUpdateNum = st.executeQuery("select max(updatenum) from indvprogressupdate");
                maxUpdateNum.next();
                String updateNumStr = maxUpdateNum.getString(1);
                updateNum = (updateNumStr == null) ? 1 : Integer.parseInt(updateNumStr) + 1;
                String updateProgress = "INSERT INTO indvprogressupdate VALUES(%d, %d, -1, %s)";
                String sndBaseString = String.format(updateProgress, updateNum, taskid, "'{"+image+"}'");
                PreparedStatement ps = conn.prepareStatement(sndBaseString);
                ps.executeUpdate();
                ps.close();
                //update user for supervisors for this task
                for(Long supv: supervisors) {
                    st.executeUpdate("UPDATE users SET indvsupvupdate = array_append(indvsupvupdate, '" + updateNum +"') WHERE userid = " + supv);
                    System.out.println("update indvsupvupdate in users for supervisor " + supv);
                }
            } else {
                return SERVER_FAILURE;
            }
            st.close();
            return updateNum;
        } catch (SQLException e) {
            return SERVER_FAILURE;
        }
    }

    //push task update to supervisors
    public static String supvUpdate(int supvid) { //taskid and update number
        int failureid = -1;
        try {
            Statement st = conn.createStatement();
            //select update numbers for tasks that this supervisor supervises
            String getUpdate = "select indvsupvupdate from users where userid = " + supvid;
            ResultSet updateResult = st.executeQuery(getUpdate);
            if(updateResult.next()) {
                Long[] updateNums = (Long[]) updateResult.getArray(1).getArray();
                updateResult.close();
                failureid = -2;
                JSONArray updates = new JSONArray();
                //for each update number get taskid in indvprogressupdate
                for (Long num : updateNums) {
                    PreparedStatement ps = conn.prepareStatement("select * from indvprogressupdate where updatenum = " + num);
                    ResultSet taskUpdates = ps.executeQuery();
                    failureid = -3;
                    taskUpdates.next();
                    failureid = -6;
                    String image = taskUpdates.getString(4);
                    failureid = -7;
                    int taskid = Integer.parseInt(taskUpdates.getString(2));
                    failureid = -4;
                    taskUpdates.close();
                    JSONObject update = new JSONObject();
                    update.put("TaskID", taskid);
                    update.put("UpdateNumber", num);
                    update.put("image", image);
                    updates.put(update);
                    ps.close();
                    failureid = -5;
                }
                st.close();
                return updates.toString();
            }else {
                return "failure";
            }
        } catch (SQLException e) {
            return "" + failureid;
        }
    }

    //when a supervisor check a task
    public static int supvCheck(int supvid, int taskid, int updatenum){
        try {
            Statement st = conn.createStatement();

            //add checkerid in indvprogressupdate for this updatenum
            String addChecker = "UPDATE indvprogressupdate SET checkerid = %d WHERE  updatenum = " + updatenum ;
            st.executeUpdate(String.format(addChecker, supvid));

            //remove updatenum from indvsupvupdate in user for supv
            String removeUpdate = "UPDATE users SET indvsupvupdate = array_remove(indvsupvupdate, '%d') WHERE  userid = " + supvid ;
            st.executeUpdate(String.format(removeUpdate, updatenum));

            //update task owner progress(increment) in individual TODO:check deadline, repetition etc.
            String updateMyIndv = "UPDATE individual SET progress = progress + 1 WHERE taskid =" + taskid;
            st.executeUpdate(updateMyIndv);

            //add updatenum to indvupdate in user
            ResultSet ownerResult = st.executeQuery("SELECT userid FROM individual WHERE taskid =" + taskid);
            ownerResult.next();
            int ownerid = Integer.parseInt(ownerResult.getString("userid"));
            String addIndvUpdate = "UPDATE users SET indvupdate = array_append(indvupdate, '%d') WHERE userid=%d;";
            st.execute(String.format(addIndvUpdate, updatenum , ownerid));
            st.close();
            return SUCCESS;
        } catch (SQLException e) {
            return SERVER_FAILURE;
        }
    }

    //when task owner get the indvupdate //error: always empty
    public static String indvOwnerUpdate(int ownerId) {
        try {
            Statement st = conn.createStatement();
            //find list of update number in user
            String getUpdate = "select indvupdate from users where userid = " + ownerId;
            ResultSet updateResult = st.executeQuery(getUpdate);

            JSONArray jsonArray = new JSONArray();
            if(updateResult.next()) {
                Long[] updateNums = (Long[]) updateResult.getArray(1).getArray();
                for (Long num : updateNums) {
                    ResultSet taskResult = st.executeQuery("SELECT * FROM indvprogressupdate WHERE updatenum =" + num);
                    if(taskResult.next()) {
                        int taskid = Integer.parseInt(taskResult.getString("taskid"));
                        int checkerid = Integer.parseInt(taskResult.getString("checkerid"));

                        JSONObject jobj = new JSONObject();
                        jobj.put("TaskID", taskid);
                        jobj.put("UpdateNum", num);
                        jobj.put("CheckerID", checkerid);
                        jsonArray.put(jobj);
                        taskResult.close();

                        //delete entries in indvprogressupdate DON'T DELETE FOR HISTORY
//                        String deleteEntries = "DELETE FROM indvprogressupdate WHERE  updatenum = " + num ;
//                        st.executeUpdate(deleteEntries);
                    }
                }
            }
            //empty individual update in user
            String deleteUpdate = "UPDATE users SET indvupdate = '{}' where userid = " + ownerId;
            st.executeUpdate(deleteUpdate);
            updateResult.close();
            st.close();
            return jsonArray.toString();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //task history: checkerName/updateNumber/taskName
    public static String getIndvHistory(int userid) {
        try {
            Statement st = conn.createStatement();
            //get individual tasks for this user
            String getIndvTaskId = "select myindividual from users where userid = " + userid;
            ResultSet taskIdResult = st.executeQuery(getIndvTaskId);
            taskIdResult.next();
            Long[] taskIds = (Long[]) taskIdResult.getArray(1).getArray();
            JSONArray history = new JSONArray();

            for (Long taskId : taskIds) {
                //get taskName from individual for each individual task
                String getIndvTaskName = "select taskname from individual where taskid = " + taskId;
                ResultSet taskNameResult = st.executeQuery(getIndvTaskName);
                if(taskNameResult.next()) {
                    String taskName = taskNameResult.getString(1);

                    //get checkerid for each taskupdate
                    String getCheckerId = "select * from indvprogressupdate where taskid = " + taskId;
                    ResultSet checkerIdResult = st.executeQuery(getCheckerId);
                    while(checkerIdResult.next()) {
                        //get checkerName from users
                        int checkerId = Integer.parseInt(checkerIdResult.getString("checkerid"));
                        int updateNum = Integer.parseInt(checkerIdResult.getString("updatenum"));

                        Statement st1 = conn.createStatement();
                        String getCheckerName = "select username from users where userid = " + checkerId;
                        ResultSet checkerNameResult = st1.executeQuery(getCheckerName);
                        if(checkerNameResult.next()) {
                            String checkerName = checkerNameResult.getString(1);
                            JSONObject obj = new JSONObject();
                            obj.put("taskName", taskName);
                            obj.put("updateNumber", updateNum);
                            obj.put("checkerName", checkerName);
                            history.put(obj);
                        }
                        st1.close();
                    }
                }
            }
            st.close();
            return history.toString();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
