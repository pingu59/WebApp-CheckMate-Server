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
import java.text.ParseException;
import java.time.LocalDate;
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
    private static final String NO_DEADLINE = "ALL GOOD";


    private static final int STARTDATE_COLUMN = 8;
    private static final int DEADLINE_COLUMN = 7;
    private static final int REPETITION_COLUMN = 4;
    private static final int TRACK_PROGRESS_COLUMN = 3;
    private static final int TRACK_FREQUENCY_COLUMN = 4;
    private static final int GROUPTASK_MEMBERS_COLUMN = 6;

    private static final int MEET_FINAL_DEADLINE = 100;
    private static final int MEET_RECENT_DEADLINE = 101;
    private static final int NO_RECENT_DEADLINE = 101;



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
    public static int createTask(int myId, String taskName, String repetition, int frequency, int[] members,
                                 String date, String bet){
        try {
            //connect
            Statement st = conn.createStatement();
            //get taskid
            ResultSet largestId = st.executeQuery("select max(taskid) from grouptask");
            largestId.next();
            String taskNum = largestId.getString(1);
            int taskId;
            if (taskNum == null) {
                taskId = 0;
            } else {
                taskId =  Integer.parseInt(taskNum) + 1;
            }
            //create task update individual table
            String supvStr = Arrays.toString(members);
            int last = supvStr.length() - 1;
            supvStr = supvStr.substring(1, last);
            String insertTaskCommand = "INSERT INTO grouptask (taskid, creatorid, taskname, repetition, frequency, member, deadline, bet) "+
                                        "VALUES(%d, %d, '{%s}','{%s}', %d,'{%s}','{%s}','{%s}')";
            String sqlCommand = String.format(insertTaskCommand, taskId, myId, taskName, repetition, frequency, supvStr, date, bet);
            int rowAffected = st.executeUpdate(sqlCommand);
            System.out.println("insert  " + rowAffected +" rows into grouptask");
            //update user table for the owner
            String memberString = Arrays.toString(members);
            int l = memberString.length() - 1;
            memberString = memberString.substring(1, l);
            String updateMyTask = "UPDATE users SET mytask = array_append(mytask, '%d') WHERE userid in ( " + memberString + ")";
            st.execute(String.format(updateMyTask, taskId, myId));
            String updateNewSupv = "UPDATE users SET newtaskinvite = array_append(newtaskinvite, '%d') WHERE userid=%d;";
            for (int id : members) {
                if(id != myId){
                    //add taskid into newtaskinvite
                    st.execute(String.format(updateNewSupv, taskId, id));
                }
            }
            //TODO initialize progress track for each member
            for(int memberid : members){
                String insertProgressCommand = String.format("INSERT INTO progresstrack VALUES(%d, %d, 0, %d)",
                                                            taskId, memberid, frequency);
                st.executeQuery(insertProgressCommand);
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
    public static String getNewInvite(int userId){
        try {
            Statement st = conn.createStatement();
            String getNewInvite = "select newtaskinvite from users where userid = " + userId;
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
                String getInviteTaskInfo = "select * from grouptask where taskid in ( " + newIndvTasks + ")";
                ResultSet inviteTaskInfoResult = st.executeQuery(getInviteTaskInfo);

                String[] jasonIds =
                        {"taskid", "creatorid", "taskname", "repetition","frequency",  "deadline", "member"};
                String[] columnName =
                        {"taskid", "creatorid", "taskname", "repetition","frequency",  "deadline", "member"};
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
    public static int clearNewInvite(int userId){
        try {
            Statement st = conn.createStatement();
            String updateMyIndv = "UPDATE users SET newtaskinvite = '{}' WHERE userid=" + userId;
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

    public static String getAllMyTask(int userId){
        try {
            Statement st = conn.createStatement();
            String mytasksStr = "select mytask from users where userid = " + userId;
            ResultSet mytasks = st.executeQuery(mytasksStr);
            if(mytasks.next()){
                Long[] taskid = (Long[]) mytasks.getArray(1).getArray();
                mytasks.close();
                String tasks = Arrays.toString(taskid);
                int last = tasks.length() - 1;
                tasks = tasks.substring(1, last);
                JSONArray jsonArray = new JSONArray();
                //if there is no new invitation, return empty string
                if(tasks.length() == 0) {
                    return jsonArray.toString();
                }
                String getInviteTaskInfo = "select * from grouptask where taskid in ( " + tasks + ")";
                ResultSet inviteTaskInfoResult = st.executeQuery(getInviteTaskInfo);

                String[] jasonIds =
                        {"taskid", "creatorid", "taskname", "repetition", "frequency",  "deadline", "member"};
                String[] columnName =
                        {"taskid", "creatorid", "taskname", "repetition", "frequency",  "deadline", "member"};
                while(inviteTaskInfoResult.next()) {
                    JSONObject jObj = new JSONObject();
                    for(int c = 0; c < 7; c++) {
                        jObj.put(jasonIds[c], inviteTaskInfoResult.getObject(columnName[c]));
                    }
                    jObj.put("bet", inviteTaskInfoResult.getString(9));
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
    public static int addProgressUpdate(int taskid, String image, int myId){
        try {
            Statement st = conn.createStatement();
            int updateNum;
            //select supervisors for this task
            String getSupervisors = "select member from grouptask where taskid = " + taskid;
            ResultSet supvResult = st.executeQuery(getSupervisors);
            if(supvResult.next()) {
                Long[] supervisors = (Long[]) supvResult.getArray(1).getArray();
                supvResult.close();
                //find number of update for this task
                ResultSet maxUpdateNum = st.executeQuery("select max(updatenum) from progressupdate");
                maxUpdateNum.next();
                String updateNumStr = maxUpdateNum.getString(1);
                updateNum = (updateNumStr == null) ? 1 : Integer.parseInt(updateNumStr) + 1;
                String updateProgress = "INSERT INTO progressupdate VALUES(%d, %d, -1,'{%s}', %d)";
                String sndBaseString = String.format(updateProgress, updateNum, taskid, "'{"+image+"}'", myId);
                PreparedStatement ps = conn.prepareStatement(sndBaseString);
                ps.executeUpdate();
                ps.close();
                //update user for supervisors for this task
                for(Long supv: supervisors) {
                    st.executeUpdate("UPDATE users SET otherstaskupdate = array_append(otherstaskupdate, '" + updateNum +"') WHERE userid = " + supv);
                    System.out.println("update otherstaskupdate in users for members " + supv);
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
            String getUpdate = "select otherstaskupdate from users where userid = " + supvid;
            ResultSet updateResult = st.executeQuery(getUpdate);
            if(updateResult.next()) {
                Long[] updateNums = (Long[]) updateResult.getArray(1).getArray();
                updateResult.close();
                failureid = -2;
                JSONArray updates = new JSONArray();
                //for each update number get taskid in indvprogressupdate
                for (Long num : updateNums) {
                    PreparedStatement ps = conn.prepareStatement("select * from progressupdate where updatenum = " + num);
                    ResultSet taskUpdates = ps.executeQuery();
                    failureid = -3;
                    taskUpdates.next();
                    failureid = -6;
                    String image = taskUpdates.getString(4);
                    failureid = -7;
                    int taskid = Integer.parseInt(taskUpdates.getString(2));
                    int userId = Integer.parseInt(taskUpdates.getString(5));
                    failureid = -4;
                    taskUpdates.close();
                    JSONObject update = new JSONObject();
                    update.put("TaskID", taskid);
                    update.put("UpdateNumber", num);
                    update.put("userId", userId);
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

    //when a supervisor check a task, myid is supervisor id
    public static int supvCheck(int myId, int taskid, int updatenum){
        try {
            Statement st = conn.createStatement();

            //add checkerid in indvprogressupdate for this updatenum
            String addChecker = "UPDATE progressupdate SET checkerid = %d WHERE  updatenum = " + updatenum ;
            st.executeUpdate(String.format(addChecker, myId));

            //remove updatenum from indvsupvupdate in user for supv
            //TODO remove all otherstaskupdate of all group member
            String removeUpdate = "UPDATE users SET otherstaskupdate = array_remove(otherstaskupdate, '%d') WHERE  userid = " + myId ;
            st.executeUpdate(String.format(removeUpdate, updatenum));

            //update task owner progress(increment) in individual TODO:check deadline, repetition etc.
            //String updateMyIndv = "UPDATE grouptask SET progress = progress + 1 WHERE taskid =" + taskid;
            //st.executeUpdate(updateMyIndv);

            //add updatenum to indvupdate in user
            ResultSet ownerResult = st.executeQuery("SELECT userid FROM progressupdate WHERE updatenum =" + updatenum);
            ownerResult.next();
            int ownerid = Integer.parseInt(ownerResult.getString("userid"));
            String addIndvUpdate = "UPDATE users SET mytaskupdate = array_append(mytaskupdate, '%d') WHERE userid=%d;";
            st.execute(String.format(addIndvUpdate, updatenum , ownerid));

            //TODO update progress
            String progressTrackCommand = String.format("UPDATE progresstrack SET progress = progress+1 WHERE taskid = %d AND memberid = %d",
                    taskid, ownerid);
            st.executeUpdate(progressTrackCommand);

            st.close();
            return SUCCESS;
        } catch (SQLException e) {
            return SERVER_FAILURE;
        }
    }

    //when task owner get the indvupdate //error: always empty
    public static String checkedNotification(int myId) {
        try {
            Statement st = conn.createStatement();
            //find list of update number in user
            String getUpdate = "select mytaskupdate from users where userid = " + myId;
            ResultSet updateResult = st.executeQuery(getUpdate);

            JSONArray jsonArray = new JSONArray();
            if(updateResult.next()) {
                Long[] updateNums = (Long[]) updateResult.getArray(1).getArray();
                for (Long num : updateNums) {
                    ResultSet taskResult = st.executeQuery("SELECT * FROM progressupdate WHERE updatenum =" + num);
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
            String deleteUpdate = "UPDATE users SET mytaskupdate = '{}' where userid = " + myId;
            st.executeUpdate(deleteUpdate);
            updateResult.close();
            st.close();
            return jsonArray.toString();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //task history: checkerName/updateNumber/taskName
    //TODO NEED TO CHANGE SQL, NO INDIVIDUAL COLUMN ANYMORE!!!!!!
    public static String getIndvHistory(int userid) {
        try {
            Statement st = conn.createStatement();
            //get individual tasks for this user
            String getIndvTaskId = "select mytask from users where userid = " + userid;
            ResultSet taskIdResult = st.executeQuery(getIndvTaskId);
            taskIdResult.next();
            Long[] taskIds = (Long[]) taskIdResult.getArray(1).getArray();
            JSONArray history = new JSONArray();

            for (Long taskId : taskIds) {
                //get taskName from individual for each individual task
                String getIndvTaskName = "select taskname from grouptask where taskid = " + taskId;
                ResultSet taskNameResult = st.executeQuery(getIndvTaskName);
                if(taskNameResult.next()) {
                    String taskName = taskNameResult.getString(1);

                    //get checkerid for each taskupdate
                    String getCheckerId = "select * from progressupdate where taskid = " + taskId;
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

    // check any deadline has passed
    public static String checkDeadline(int userid) {
        try {
            Statement st = conn.createStatement();
            //get all tasks of the user
            String getTasksCommand = "SELECT mytask FROM users WHERE userid = " + userid;
            ResultSet taskIdResult = st.executeQuery(getTasksCommand);
            taskIdResult.next();
            Long[] taskIds = (Long[]) taskIdResult.getArray(1).getArray();
            JSONArray unfinishedTasks = new JSONArray();

            for (Long taskId : taskIds) {
                //get task info from group task table
                String getTaskInfoCommand  = "SELECT * FROM grouptask WHERE taskid = " + taskId;
                ResultSet taskInfo = st.executeQuery(getTaskInfoCommand);
                if(taskInfo.next()) {


                    LocalDate startdate = taskInfo.getDate(STARTDATE_COLUMN).toLocalDate();
                    LocalDate deadlineDate = taskInfo.getDate(DEADLINE_COLUMN).toLocalDate();
                    String repetition = taskInfo.getString(REPETITION_COLUMN);
                    int deadlineStatus = meetRecentDeadline(startdate, deadlineDate, repetition);
                    if(deadlineStatus == NO_RECENT_DEADLINE){
                        return NO_DEADLINE;
                    }
                    else{   //check progress and frequency
                        String getProgressCommand = "SELECT * FROM progresstrack WHERE memberid = "
                                                    + userid + " AND taskid = " + taskId;
                        ResultSet progressInfo = st.executeQuery(getProgressCommand);
                        if(progressInfo.next()){
                            int progress = progressInfo.getInt(TRACK_PROGRESS_COLUMN);
                            int frequency = progressInfo.getInt(TRACK_FREQUENCY_COLUMN);
                            if(progress < frequency){
                                JSONObject task = new JSONObject();
                                task.put("taskid", taskId);
                                unfinishedTasks.put(task);
                            }
                            else{
                                break;
                            }
                        }
                    }
                }
            }
            st.close();
            return unfinishedTasks.toString();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //helper method of checkDeadline
    private static int meetRecentDeadline(LocalDate startdate, LocalDate deadlineDate, String repetition){

        LocalDate today = LocalDate.now();

        if(deadlineDate.isEqual(today)){
            return MEET_FINAL_DEADLINE;
        }
        switch (repetition){
            case "Daily":
                if(today.isEqual(startdate.plusDays(1))){
                    return MEET_RECENT_DEADLINE;
                }
            case "Weekly":
                if(today.isEqual(startdate.plusDays(7))){
                    return MEET_RECENT_DEADLINE;
                }
            case "Monthly":
                if(today.isEqual(startdate.plusMonths(1))){
                    return MEET_RECENT_DEADLINE;
                }
        }
        return NO_RECENT_DEADLINE;
    }

    public static String getMembersProgress(int taskid) {
        try {
            Statement st = conn.createStatement();
            //get all members of the task
            String getMembersCommand = "SELECT * FROM grouptask WHERE taskid = " + taskid;
            ResultSet membersResult = st.executeQuery(getMembersCommand);
            membersResult.next();
            Long[] memberIds = (Long[]) membersResult.getArray(GROUPTASK_MEMBERS_COLUMN).getArray();
            JSONArray progressArray = new JSONArray();

            for (Long memberId : memberIds) {

                //get progress of each memeber in progress track table
                String getProgressCommand  = "SELECT * FROM progresstrack WHERE taskid = " + taskid
                                                + " AND memberid = " + memberId;
                ResultSet progressInfo = st.executeQuery(getProgressCommand);
                if(progressInfo.next()) {

                    int progress = progressInfo.getInt(TRACK_PROGRESS_COLUMN);
                    JSONObject memberProgress = new JSONObject();
                    memberProgress.put("userid", memberId);
                    memberProgress.put("progress", progress);
                    progressArray.put(memberProgress);
                }
            }
            st.close();
            return progressArray.toString();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
