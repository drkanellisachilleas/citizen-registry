package gr.assignment.citizens.client;

import gr.assignment.citizens.Citizen;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

@SpringBootApplication
public class CitizenClientApplication {

    private static final DateTimeFormatter BIRTH_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CitizenClientApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }

    @Bean
    CommandLineRunner menu() {
        return args -> {
            Scanner in = new Scanner(System.in);
            RestClient api = RestClient.builder()
                    .baseUrl("http://localhost:8080/api/citizens")
                    .build();

            while (true) {
                System.out.println("\n1 Create | 2 Read | 3 Update | 4 Delete | 5 Search / List | Other: Exit");
                String choice = in.nextLine();
                if (!Set.of("1", "2", "3", "4", "5").contains(choice)) {
                    break;
                }
                try {
                    switch (choice) {
                        case "1" -> {
                            Citizen citizen = new Citizen();
                            citizen.setId(ask(in, "ID (8 characters)"));
                            citizen.setFirstName(ask(in, "First name"));
                            citizen.setLastName(ask(in, "Last name"));
                            citizen.setGender(ask(in, "Gender"));
                            citizen.setBirthDate(LocalDate.parse(ask(in, "Birth date dd-MM-yyyy"), BIRTH_DATE_FORMATTER));
                            citizen.setTaxId(blankNull(ask(in, "Tax ID (optional)")));
                            citizen.setAddress(blankNull(ask(in, "Address (optional)")));
                            Citizen created = api.post().body(citizen).retrieve().body(Citizen.class);
                            if (created != null) {
                                System.out.println(created.getId());
                            }
                        }
                        case "2" -> {
                            Citizen found = api.get().uri("/{id}", ask(in, "ID")).retrieve().body(Citizen.class);
                            show(found);
                        }
                        case "3" -> {
                            String id = ask(in, "ID");
                            Map<String, String> changes = new HashMap<>();
                            String tax = ask(in, "New tax ID (blank = unchanged)");
                            String address = ask(in, "New address (blank = unchanged)");
                            if (!tax.isBlank()) {
                                changes.put("taxId", tax);
                            }
                            if (!address.isBlank()) {
                                changes.put("address", address);
                            }
                            Citizen updated = api.patch().uri("/{id}", id).body(changes).retrieve().body(Citizen.class);
                            show(updated);
                        }
                        case "4" -> {
                            api.delete().uri("/{id}", ask(in, "ID")).retrieve().toBodilessEntity();
                            System.out.println("Deleted");
                        }
                        case "5" -> {
                            String field = ask(in, "Field (id,firstName,lastName,gender,birthDate,taxId,address or blank for all)");
                            Citizen[] found;
                            if (field.isBlank()) {
                                found = api.get().retrieve().body(Citizen[].class);
                            } else {
                                String value = ask(in, "Value");
                                found = api.get()
                                        .uri(b -> b.queryParam(field, value).build())
                                        .retrieve()
                                        .body(Citizen[].class);
                            }
                            if (found != null) {
                                for (Citizen x : found) {
                                    show(x);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Request failed: " + e.getMessage());
                }
            }
        };
    }

    static String ask(Scanner in, String text) {
        System.out.print(text + ": ");
        return in.nextLine().trim();
    }

    static String blankNull(String s) {
        return s.isBlank() ? null : s;
    }

    static void show(Citizen c) {
        if (c != null) {
            System.out.printf("%s | %s %s | %s | %s | %s | %s%n",
                    c.getId(), c.getFirstName(), c.getLastName(),
                    c.getGender(), c.getBirthDate(), c.getTaxId(), c.getAddress());
        }
    }
}
