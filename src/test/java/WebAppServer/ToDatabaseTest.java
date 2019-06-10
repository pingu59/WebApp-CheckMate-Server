package WebAppServer;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.Date;

public class ToDatabaseTest {

  @Test
  public void encrypt() {
  }

  @Test
  public void getUser() {
  }

  @Test
  public void register() {
  }

  @Test
  public void login() {
  }

  @Test
  public void bbbb(){
      int[] array = {10,11};
      ToDatabase.createTask(10,"just a task", "None",5, array, "2099-01-01", "some bet");
  }

//  @Test
//  public void friendRequest() {
//    ToDatabase.friendRequest(10, 9);
//
//  }
//
//  @Test
//  public void getFriendRequestList() {
//    ToDatabase.getFriendRequestList(10);
//  }
//
//  @Test
//  public void deleteFriendRequest(){
//    ToDatabase.deleteFriendRequest(9,14);
//  }
}