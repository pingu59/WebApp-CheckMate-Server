package WebAppServer;
import java.util.Arrays;

import org.springframework.web.bind.annotation.*;

import static WebAppServer.ToDatabase.*;

@RestController
public class ToXamarin {
    @GetMapping("/login")
    public String xlogin(@RequestParam(value="userid") String userid,
                        @RequestParam(value="password") String password) {
        return login(userid, password);
    }

    @GetMapping("/register")
    public String xregister(@RequestParam(value="username") String username,
                           @RequestParam(value="password") String password) {
        return String.valueOf(register(username, password));
    }

    @GetMapping("/friendreq")
    public String xfriendReq(@RequestParam(value="senderid") int senderid,
                        @RequestParam(value="friendid") int friendid) {
        return String.valueOf(friendRequest(senderid, friendid));
    }

    @GetMapping("/friendreqlist")
    public String xgetFriendRequestList(@RequestParam(value="id") int id){
        return Arrays.toString(getFriendRequestList(id));
    }

    @GetMapping("/deletefriendreq")
    public String xdeleteFriendRequest(@RequestParam(value="myid") int myid,
                                        @RequestParam(value="requestid") int requestid){
        return String.valueOf(deleteFriendRequest(myid, requestid));
    }

    @GetMapping("/addfriend")
    public String xaddfriend(@RequestParam(value="myid") int myid,
                            @RequestParam(value="requestid") int requestid){
        return String.valueOf(addFriend(myid, requestid));
    }

    @GetMapping("/deletefriend")
    public int xdeletefriend(@RequestParam(value="myid") int myid,
                             @RequestParam(value="requestid") int requestid){
        return deleteFriend(myid, requestid);
    }

    @GetMapping("/getinbox")
    public String xgetinbox(@RequestParam(value="myid") int myid){
        //format: {}, {14}, {-9}. positive for adding new friend, negative for deleted
        return String.valueOf(getInbox(myid));
    }

    @GetMapping("/getfriendid")
    public String xgetfriendid(@RequestParam(value="myid") int myid){
        //format: {}, {14}, {14, 9}.
        return getFriendId(myid);
    }

    @GetMapping("/getfriends")
    public String xgetfriends(@RequestParam(value="myid") int myid){
        //format: {}, {14}, {14, 9}.
        return getFriends(myid);
    }

    //not tested

    @GetMapping("/getUserInfo")
    public String xgetUserInfo(@RequestParam(value="userId") int userId){
        return getUserInfo(userId);
    }

    @GetMapping("/createTask")
    public String xcreatIndividualTask(@RequestParam(value="myid") int myId,
                                       @RequestParam(value="taskname") String taskName,
                                       @RequestParam(value="repetition") String repetition,
                                       @RequestParam(value="frequency") int frequency,
                                       @RequestParam(value="members") int[] members,
                                       @RequestParam(value="deadline") String date,
                                       @RequestParam(value="bet") String bet){
        return String.valueOf(createTask(myId, taskName, repetition, frequency, members, date, bet));
    }


    @GetMapping("/deleteIndvTask")
    public String xdeleteIndividualTask(@RequestParam(value="taskId") int taskId){
        return String.valueOf(deleteIndvTask(taskId));

    }

    @GetMapping("/getMyTask")
    public String xgetMyTask(@RequestParam(value="userId") int userId){
        return String.valueOf(getAllMyTask(userId));
    }

    @GetMapping("/getFriendIndividual")
    public String xgetFriendIndividual(@RequestParam(value="userId") int userId){
        return String.valueOf(getAllMyFriendIndv(userId));
    }



    @GetMapping("/clearInvitation")
    public String xclearInvitation(@RequestParam(value="userId") int userId){
        return String.valueOf(clearNewInvite(userId));

    }

    @GetMapping("/getNewInvite")
    public String xgetNewInvite(@RequestParam(value="userId") int userId){
        return String.valueOf(getNewInvite(userId));
    }


    @PostMapping(path = "/addProgressUpdate")
    public String xaddIndvProgressUpdate(@RequestParam(value="taskId") int taskId,
                                         @RequestParam(value="myId") int myId,
                                         @RequestBody String image){
        return String.valueOf(addProgressUpdate(taskId, image, myId));
    }

    @GetMapping("/friendUpdate")
    public String xsupvUpdate(@RequestParam(value="myId") int supvId){
        return String.valueOf(supvUpdate(supvId));
    }

    @GetMapping("/sendNewCheck")
    public String xsupvCheck(@RequestParam(value="myId") int myId,
                             @RequestParam(value="taskId") int taskId,
                             @RequestParam(value="updateNumber") int updateNum){
        return String.valueOf(supvCheck(myId, taskId, updateNum));
    }

    @GetMapping("/checkedNotification")
    public String xcheckedNotification(@RequestParam(value="myId") int myId){
        return String.valueOf(checkedNotification(myId));
    }

    @GetMapping("/getHistory")
    public String xgetHistory(@RequestParam(value="userId") int userId){
        return String.valueOf(getIndvHistory(userId));
    }

//    @GetMapping("/checkDeadline")
//    public String xcheckDeadline(@RequestParam(value="userId") int userId){
//        return String.valueOf(checkDeadline(userId));
//    }

    @GetMapping("getProgress")
    public String xgetMembersProgress(@RequestParam(value="taskId") int taskId){
        return String.valueOf(getMembersProgress(taskId));
    }

    @GetMapping("getPenalty")
    public String xgetPenalty(@RequestParam(value="userId") int userId){
        return getPenalty(userId);
    }

    @GetMapping("removePenalty")
    public String xremovePenalty(@RequestParam(value="date") String date,
                                @RequestParam(value="taskid") int taskid,
                                @RequestParam(value="member") int member,
                                 @RequestParam(value="owner") int owner) {
        return removePenalty(date, taskid, member, owner);
    }
}
