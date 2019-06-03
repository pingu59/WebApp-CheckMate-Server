package WebAppServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
        //added for testing
//        ConfigurableApplicationContext context = SpringApplication.run(App.class, args);
//        System.out.println(ToDatabase.createTask(0, "sleep", "weekly", 7));
//        context.close();

    }
}
