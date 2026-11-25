package gr.assignment.citizens.service;

import gr.assignment.citizens.Citizen;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CitizenService {

    private static final DateTimeFormatter BIRTH_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-uuuu");

    private final CitizenRepository repository;

    public CitizenService(CitizenRepository repository) {
        this.repository = repository;
    }

    public static void validId(String id) {
        if (id == null || id.length() != 8 || id.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Citizen ID must contain exactly 8 characters");
        }
    }

    public Citizen create(Citizen citizen) {
        validId(citizen.getId());
        if (repository.existsById(citizen.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Citizen already exists");
        }
        return repository.save(citizen);
    }

    public Citizen get(String id) {
        validId(id);
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Citizen not found"));
    }

    public void delete(String id) {
        get(id);
        repository.deleteById(id);
    }

    public Citizen update(String id, CitizenUpdate update) {
        Citizen citizen = get(id);
        if (update.getTaxId() != null) {
            citizen.setTaxId(update.getTaxId());
        }
        if (update.getAddress() != null) {
            citizen.setAddress(update.getAddress());
        }
        return repository.save(citizen);
    }

    public List<Citizen> search(Map<String, String> filters) {
        List<Citizen> all = repository.findAll();
        if (filters == null || filters.isEmpty()) {
            return all;
        }
        return all.stream()
                .filter(c -> filters.entrySet().stream()
                        .allMatch(e -> matches(c, e.getKey(), e.getValue())))
                .toList();
    }

    private boolean matches(Citizen c, String key, String value) {
        String actual = switch (key) {
            case "id" -> c.getId();
            case "firstName" -> c.getFirstName();
            case "lastName" -> c.getLastName();
            case "gender" -> c.getGender();
            case "birthDate" -> c.getBirthDate().format(BIRTH_DATE_FORMATTER);
            case "taxId" -> c.getTaxId();
            case "address" -> c.getAddress();
            default -> null;
        };
        return Objects.equals(actual, value);
    }
}
