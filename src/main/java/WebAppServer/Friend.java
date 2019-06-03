package WebAppServer;

public class Friend {
  private int FriendID;
  private String FriendName;

  public Friend(int FriendID, String FriendName){
    this.FriendID = FriendID;
    this.FriendName = FriendName;
  }

  public int getFriendID() { return FriendID; }
  public String getFriendName() {
    return FriendName;
  }
}
