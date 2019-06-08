package WebAppServer;
import java.util.Arrays;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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
                                       @RequestParam(value="deadline") String date){
        return String.valueOf(createTask(myId, taskName, repetition, frequency, members, date));
    }


    @GetMapping("/deleteIndvTask")
    public String xdeleteIndividualTask(@RequestParam(value="taskId") int taskId){
        return String.valueOf(deleteIndvTask(taskId));

    }

    @GetMapping("/getMyIndividual")
    public String xgetMyIndividual(@RequestParam(value="userId") int userId){
        return String.valueOf(getAllMyIndv(userId));
    }

    @GetMapping("/getFriendIndividual")
    public String xgetFriendIndividual(@RequestParam(value="userId") int userId){
        return String.valueOf(getAllMyFriendIndv(userId));
    }



    @GetMapping("/clearIndvInvitation")
    public String xclearIndvInvitation(@RequestParam(value="userId") int userId){
        return String.valueOf(clearNewIndvInvite(userId));

    }

    @GetMapping("/getNewIndvInvite")
    public String xgetNewIndvInvite(@RequestParam(value="userId") int userId){
        return String.valueOf(getNewIndvInvite(userId));
    }

    @PostMapping(path = "/addIndvProgressUpdate")
    public String xaddIndvProgressUpdate(@RequestParam(value="taskId") int taskId,
                                         @RequestBody String image){
        return String.valueOf(addIndvProgressUpdate(taskId, image));
    }

    @GetMapping("/supvUpdate")
    public String xsupvUpdate(@RequestParam(value="supervisorId") int supvId){
        return String.valueOf(supvUpdate(supvId));
    }

    @GetMapping("/supvCheck")
    public String xsupvCheck(@RequestParam(value="supervisorId") int supvId,
                             @RequestParam(value="taskId") int taskId,
                             @RequestParam(value="updateNumber") int updateNum){
        return String.valueOf(supvCheck(supvId, taskId, updateNum));
    }

    @GetMapping("/indvOwnerUpdate")
    public String xindvOwnerUpdate(@RequestParam(value="ownerId") int ownerId){
        return String.valueOf(indvOwnerUpdate(ownerId));
    }

    @GetMapping("/getHistory")
    public String xgetHistory(@RequestParam(value="userId") int userId){
        return String.valueOf(getIndvHistory(userId));
    }

}
