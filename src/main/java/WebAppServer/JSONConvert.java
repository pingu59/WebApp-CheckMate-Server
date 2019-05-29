package WebAppServer;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.json.JSONObject;

import java.util.LinkedHashMap;

public class JSONConvert {
    public static JSONObject userToJSON(User user){
        JSONObject jobj = new JSONObject();
        jobj.put("userid", user.getUserId());
        jobj.put("username", user.getUsername());
        jobj.put("password", user.getPassword());
        return jobj;
    }

    public static User JSONToUser(JSONObject jobj){
        JSONParser parser = new JSONParser(jobj.toString());
        try{
            LinkedHashMap<String, Object> hashMap = parser.parseObject();
            int userid = Integer.parseInt((String)hashMap.get("userid"));
            String password = (String) hashMap.get("password");
            return new User(userid, password);
        }catch (ParseException e){
            //REFACTOR THIS
            return null;
        }
    }

}
