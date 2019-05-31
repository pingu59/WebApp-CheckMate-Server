package WebAppServer;
import java.util.Arrays;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


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
}
