package gr.assignment.citizens.service;

import gr.assignment.citizens.Citizen;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CitizenUnitTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setup() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    private Citizen validCitizen() {
        return new Citizen(
                "AB123456",
                "Anna",
                "Smith",
                "F",
                LocalDate.of(2000, 1, 2),
                "123456789",
                "Athens"
        );
    }

    @Test
    void constructorAndAccessorsStoreAndReturnValues() {
        Citizen c = new Citizen("AB123456", "Anna", "Smith", "F", LocalDate.of(2000, 1, 2), null, null);
        assertEquals("AB123456", c.getId());
        assertEquals("Anna", c.getFirstName());
        assertEquals("Smith", c.getLastName());
        assertEquals("F", c.getGender());
        assertEquals(LocalDate.of(2000, 1, 2), c.getBirthDate());

        c.setTaxId("123456789");
        c.setAddress("Athens");
        assertEquals("123456789", c.getTaxId());
        assertEquals("Athens", c.getAddress());
    }

    @Test
    void validCitizenAccepted() {
        Set<ConstraintViolation<Citizen>> violations = validator.validate(validCitizen());
        assertTrue(violations.isEmpty());
    }

    @Test
    void validCitizenWithOptionalFieldsNullAccepted() {
        Citizen c = validCitizen();
        c.setTaxId(null);
        c.setAddress(null);
        assertTrue(validator.validate(c).isEmpty());
    }

    @Test
    void invalidIdLengthRejectedWithExpectedMessage() {
        Citizen c = validCitizen();
        c.setId("123");

        Set<ConstraintViolation<Citizen>> violations = validator.validate(c);
        assertEquals(1, violations.size());
        ConstraintViolation<Citizen> violation = violations.iterator().next();
        assertEquals("id", violation.getPropertyPath().toString());
        assertEquals("ID must contain exactly 8 characters", violation.getMessage());
    }

    @Test
    void blankIdRejectedWithExpectedMessage() {
        Citizen c = validCitizen();
        c.setId("        ");

        Set<ConstraintViolation<Citizen>> violations = validator.validate(c);
        assertEquals(1, violations.size());
        ConstraintViolation<Citizen> violation = violations.iterator().next();
        assertEquals("id", violation.getPropertyPath().toString());
        assertEquals("ID is required", violation.getMessage());
    }

    @Test
    void blankFirstNameRejectedWithExpectedMessage() {
        Citizen c = validCitizen();
        c.setFirstName("   ");

        Set<ConstraintViolation<Citizen>> violations = validator.validate(c);
        assertEquals(1, violations.size());
        ConstraintViolation<Citizen> violation = violations.iterator().next();
        assertEquals("firstName", violation.getPropertyPath().toString());
        assertEquals("First name is required", violation.getMessage());
    }

    @Test
    void blankLastNameRejectedWithExpectedMessage() {
        Citizen c = validCitizen();
        c.setLastName("");

        Set<ConstraintViolation<Citizen>> violations = validator.validate(c);
        assertEquals(1, violations.size());
        ConstraintViolation<Citizen> violation = violations.iterator().next();
        assertEquals("lastName", violation.getPropertyPath().toString());
        assertEquals("Last name is required", violation.getMessage());
    }

    @Test
    void blankGenderRejectedWithExpectedMessage() {
        Citizen c = validCitizen();
        c.setGender("");

        Set<ConstraintViolation<Citizen>> violations = validator.validate(c);
        assertEquals(1, violations.size());
        ConstraintViolation<Citizen> violation = violations.iterator().next();
        assertEquals("gender", violation.getPropertyPath().toString());
        assertEquals("Gender is required", violation.getMessage());
    }

    @Test
    void nullBirthDateRejectedWithExpectedMessage() {
        Citizen c = validCitizen();
        c.setBirthDate(null);

        Set<ConstraintViolation<Citizen>> violations = validator.validate(c);
        assertEquals(1, violations.size());
        ConstraintViolation<Citizen> violation = violations.iterator().next();
        assertEquals("birthDate", violation.getPropertyPath().toString());
        assertEquals("Birth date is required", violation.getMessage());
    }

    @Test
    void futureBirthDateRejectedWithExpectedMessage() {
        Citizen c = validCitizen();
        c.setBirthDate(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<Citizen>> violations = validator.validate(c);
        assertEquals(1, violations.size());
        ConstraintViolation<Citizen> violation = violations.iterator().next();
        assertEquals("birthDate", violation.getPropertyPath().toString());
        assertEquals("Birth date must be in the past or present", violation.getMessage());
    }

    @Test
    void invalidTaxIdRejectedWithExpectedMessage() {
        Citizen c = validCitizen();
        c.setTaxId("123");

        Set<ConstraintViolation<Citizen>> violations = validator.validate(c);
        assertEquals(1, violations.size());
        ConstraintViolation<Citizen> violation = violations.iterator().next();
        assertEquals("taxId", violation.getPropertyPath().toString());
        assertEquals("Tax ID must contain exactly 9 digits", violation.getMessage());
    }

    @Test
    void validCitizenUpdateAccepted() {
        CitizenUpdate update = new CitizenUpdate();
        update.setTaxId("987654321");
        update.setAddress("Rhodes");

        assertTrue(validator.validate(update).isEmpty());
    }

    @Test
    void invalidCitizenUpdateTaxIdRejectedWithExpectedMessage() {
        CitizenUpdate update = new CitizenUpdate();
        update.setTaxId("ABC123");

        Set<ConstraintViolation<CitizenUpdate>> violations = validator.validate(update);
        assertEquals(1, violations.size());
        ConstraintViolation<CitizenUpdate> violation = violations.iterator().next();
        assertEquals("taxId", violation.getPropertyPath().toString());
        assertEquals("Tax ID must contain exactly 9 digits", violation.getMessage());
    }
}
