package WebAppServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ToXamarin {
    @RequestMapping("/login")
    public int greeting(@RequestParam(value="userid") int userid) {
        return userid;
    }
}
