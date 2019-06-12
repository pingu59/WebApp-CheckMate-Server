package WebAppServer;
import java.util.List;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;

public class JSONConvert {
    public static String userToJSON(User user){
        JSONObject jobj = new JSONObject();
        jobj.put("userid", user.getUserId());
        jobj.put("username", user.getUsername());
        jobj.put("password", user.getPassword());
        jobj.put("avatarNum", user.getAvatar());
        return jobj.toString();
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

    public static String friendsToJSON(List<Friend> friends){
        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i < friends.size();i++){
            JSONObject jobj = new JSONObject();
            jobj.put("FriendID", friends.get(i).getFriendID());
            jobj.put("FriendName", friends.get(i).getFriendName());
            jsonArray.put(jobj);
        }
        return jsonArray.toString();
    }
}
