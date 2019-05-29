package WebAppServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static WebAppServer.ToDatabase.getUser;
import static WebAppServer.ToDatabase.register;

@RestController
public class ToXamarin {
    @RequestMapping("/login")
    public String xlogin(@RequestParam(value="userid") int userid) {
        return getUser(userid).toString();
    }

    @RequestMapping("/register")
    public String xregister(@RequestParam(value="username") String username,
                           @RequestParam(value="password") String password) {
        return register(username, password);
    }
}
