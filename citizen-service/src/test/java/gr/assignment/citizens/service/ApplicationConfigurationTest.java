package gr.assignment.citizens.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import gr.assignment.citizens.Citizen;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ApplicationConfigurationTest {

    @Autowired
    private Environment environment;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @Test
    void datasourcePropertiesConfigureH2Database() throws Exception {
        assertEquals("jdbc:h2:mem:citizens_test;DB_CLOSE_DELAY=-1",
                environment.getProperty("spring.datasource.url"));
        assertEquals("org.h2.Driver",
                environment.getProperty("spring.datasource.driverClassName"));
        try (Connection connection = dataSource.getConnection()) {
            assertEquals("H2", connection.getMetaData().getDatabaseProductName());
        }
    }

    @Test
    void jpaPropertiesDisableOpenInViewAndSetDdlAuto() {
        assertEquals("false", environment.getProperty("spring.jpa.open-in-view"));
        assertEquals("create-drop", environment.getProperty("spring.jpa.hibernate.ddl-auto"));
    }

    @Test
    void jacksonFailOnUnknownPropertiesSettingIsEnabled() {
        assertEquals("true",
                environment.getProperty("spring.jackson.deserialization.fail-on-unknown-properties"));
        assertTrue(objectMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    @Test
    void objectMapperRejectsUnknownJsonProperties() {
        String jsonWithUnknownField = """
                {"taxId":"123456789","unknownField":"unexpected"}
                """;
        assertThrows(UnrecognizedPropertyException.class,
                () -> objectMapper.readValue(jsonWithUnknownField, CitizenUpdate.class));
    }

    @Test
    void objectMapperSerializesAndDeserializesBirthDateInDdMmYyyyFormat() throws Exception {
        Citizen citizen = new Citizen(
                "AB123456", "Anna", "Smith", "F",
                LocalDate.of(2000, 1, 2), "123456789", "Athens"
        );
        String json = objectMapper.writeValueAsString(citizen);
        assertTrue(json.contains("\"birthDate\":\"02-01-2000\""));

        Citizen deserialized = objectMapper.readValue(json, Citizen.class);
        assertEquals(LocalDate.of(2000, 1, 2), deserialized.getBirthDate());
    }
}
