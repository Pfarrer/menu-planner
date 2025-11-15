package de.brianp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "de.brianp.domain")
public class TestMenuPlannerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestMenuPlannerApplication.class, args);
    }
}