package WebAppServer;

import static org.junit.Assert.*;

import org.junit.Test;

import javax.swing.plaf.synth.SynthScrollBarUI;
import java.util.Date;

public class ToDatabaseTest {

  @Test
  public void encrypt() {
  }

  @Test
  public void getUser() {
  }

//  @Test
//  public void friend(){
//    //System.out.println(ToDatabase.getFriendRequestList(14)[0]);
//    System.out.println(ToDatabase.getFriendRequestList1(14));
//  }
//  @Test
//  public void register() {
//    //ToDatabase.register("testavatar", "testingavatar",2);
//    //System.out.println(ToDatabase.getUserInfo(10));
//    System.out.println(ToDatabase.getFriends(2));
//  }
//
//  @Test
//  public void login() {
//  }
//  @Test
//  public void getSummary(){
//    System.out.println(ToDatabase.getProgressHistory(2));
//  }

//  @Test
//  public void createTask(){
//    int[] members = {2,3};
//    ToDatabase.createTask(2,"testbywpz","Daily",99,members,"12/06/2019","betbywpz");
//  }
//  @Test
//  public void getAllMyTaskTest(){
//    ToDatabase.getAllMyTask(2);
//  }
//
//  @Test
//  public void getAllMyCompleteTest(){
//    System.out.println(ToDatabase.getCompletedStat(2));
//  }
//  @Test
//  public void getSummary(){
//    System.out.println(ToDatabase.getProgressHistory(3));
//  }
//  @Test
//  public void getCompleteStats(){
//    ToDatabase.getCompletedStat(2);
//  }
  @Test
  public void t(){
    System.out.println(ToDatabase.getAllMyTask(27));
  }

}