package gr.assignment.citizens.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ApplicationContextTest {

    @Autowired
    private CitizenController controller;

    @Autowired
    private CitizenService service;

    @Autowired
    private CitizenRepository repository;

    @Test
    void contextLoadsCoreBeans() {
        assertNotNull(controller);
        assertNotNull(service);
        assertNotNull(repository);
    }
}
