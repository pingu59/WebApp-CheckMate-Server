package WebAppServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import java.util.Arrays;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
        //added for testing
 //       ConfigurableApplicationContext context = SpringApplication.run(App.class, args);
//        int[] sup = {1};
//        int taskId = ToDatabase.createIndvTask(0, "sleep", "weekly", 7,
//                sup, "2020-12-12");
//
//        System.out.println("created task : " + taskId);

        //System.out.println(ToDatabase.deleteIndvTask(0));
//        System.out.println(ToDatabase.getNewIndvInvite(1));
//        System.out.println(ToDatabase.clearNewIndvInvite(1));
       // System.out.println(ToDatabase.getUserInfo(0));
//        context.close();

    }
}
