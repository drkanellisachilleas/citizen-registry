package gr.assignment.citizens.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan("gr.assignment.citizens")
@SpringBootApplication(scanBasePackages = "gr.assignment.citizens")
public class CitizenRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitizenRegistryApplication.class, args);
    }
}
