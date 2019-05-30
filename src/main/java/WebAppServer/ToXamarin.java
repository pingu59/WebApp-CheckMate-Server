package WebAppServer;
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
}
