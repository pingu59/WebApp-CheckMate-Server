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

}
