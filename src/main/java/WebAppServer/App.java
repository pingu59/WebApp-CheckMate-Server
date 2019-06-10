package WebAppServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);


//        while(true){
//            LocalTime now = LocalTime.now();
//            LocalTime noon = LocalTime.of(12,0,0,0);
//            if(now.equals(noon)){
//                ToDatabase.checkDeadline(0);
//            }
//        }


    }
}
