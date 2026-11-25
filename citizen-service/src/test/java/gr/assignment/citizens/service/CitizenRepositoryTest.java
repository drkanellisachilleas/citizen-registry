package gr.assignment.citizens.service;

import gr.assignment.citizens.Citizen;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class CitizenRepositoryTest {

    @Autowired
    private CitizenRepository repository;

    private Citizen sampleCitizen(String id, String firstName) {
        return new Citizen(id, firstName, "Smith", "F", LocalDate.of(2000, 1, 2), "123456789", "Athens");
    }

    @Test
    void savePersistsCitizen() {
        Citizen saved = repository.saveAndFlush(sampleCitizen("AB123456", "Anna"));

        assertEquals("AB123456", saved.getId());
        assertEquals("Anna", saved.getFirstName());
        assertEquals(1, repository.count());
    }

    @Test
    void findByIdReturnsExistingCitizen() {
        repository.saveAndFlush(sampleCitizen("AB123456", "Anna"));

        Optional<Citizen> found = repository.findById("AB123456");

        assertTrue(found.isPresent());
        assertEquals("Anna", found.get().getFirstName());
        assertEquals("Athens", found.get().getAddress());
    }

    @Test
    void findByIdReturnsEmptyWhenCitizenDoesNotExist() {
        Optional<Citizen> found = repository.findById("ZZ999999");

        assertTrue(found.isEmpty());
    }

    @Test
    void findAllReturnsAllPersistedCitizens() {
        repository.saveAndFlush(sampleCitizen("AB123456", "Anna"));
        repository.saveAndFlush(sampleCitizen("CD654321", "Maria"));

        List<Citizen> citizens = repository.findAll();

        assertEquals(2, citizens.size());
    }

    @Test
    void updateModifiesPersistedCitizenFields() {
        Citizen citizen = repository.saveAndFlush(sampleCitizen("AB123456", "Anna"));
        citizen.setTaxId("987654321");
        citizen.setAddress("Thessaloniki");
        repository.saveAndFlush(citizen);

        Citizen reloaded = repository.findById("AB123456").orElseThrow();
        assertEquals("987654321", reloaded.getTaxId());
        assertEquals("Thessaloniki", reloaded.getAddress());
    }

    @Test
    void deleteByIdRemovesCitizen() {
        repository.saveAndFlush(sampleCitizen("AB123456", "Anna"));

        repository.deleteById("AB123456");
        repository.flush();

        assertFalse(repository.existsById("AB123456"));
        assertEquals(0, repository.count());
    }

    @Test
    void existsByIdReturnsTrueForPersistedCitizen() {
        repository.saveAndFlush(sampleCitizen("AB123456", "Anna"));

        assertTrue(repository.existsById("AB123456"));
    }

    @Test
    void existsByIdReturnsFalseForMissingCitizen() {
        assertFalse(repository.existsById("ZZ999999"));
    }
}
