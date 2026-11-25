package gr.assignment.citizens.service;

import gr.assignment.citizens.Citizen;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/citizens")
public class CitizenController {

    private static final Set<String> ALLOWED_FILTERS =
            Set.of("id", "firstName", "lastName", "gender", "birthDate", "taxId", "address");

    private static final DateTimeFormatter STRICT_BIRTH_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final CitizenService service;

    public CitizenController(CitizenService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Citizen> create(@Valid @RequestBody Citizen citizen) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(citizen));
    }

    @GetMapping
    public List<Citizen> getAllOrFilter(@RequestParam(required = false) Map<String, String> filters) {
        if (filters == null || filters.isEmpty()) {
            return service.search(Map.of());
        }
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!ALLOWED_FILTERS.contains(key) || value == null || value.isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid search field: " + key);
            }
            if ("id".equals(key)) {
                CitizenService.validId(value);
            }
            if ("taxId".equals(key) && !value.matches("[0-9]{9}")) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid tax ID");
            }
            if ("birthDate".equals(key)) {
                try {
                    LocalDate.parse(value, STRICT_BIRTH_DATE_FORMATTER);
                } catch (DateTimeParseException ex) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid birth date");
                }
            }
        }
        return service.search(filters);
    }

    @GetMapping("/{id}")
    public Citizen get(@PathVariable String id) {
        return service.get(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @PatchMapping("/{id}")
    public Citizen update(@PathVariable String id, @Valid @RequestBody CitizenUpdate update) {
        return service.update(id, update);
    }
}
