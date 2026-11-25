package gr.assignment.citizens.service;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

import static io.restassured.RestAssured.delete;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CitizenApiIT {

    @LocalServerPort
    private int port;

    @Autowired
    private CitizenRepository repository;

    private static final String SAMPLE_CITIZEN = """
            {
              "id": "AB123456",
              "firstName": "Anna",
              "lastName": "Smith",
              "gender": "F",
              "birthDate": "02-01-2000",
              "taxId": "123456789",
              "address": "Athens"
            }
            """;

    private static final String SECOND_CITIZEN = """
            {
              "id": "CD654321",
              "firstName": "Nikos",
              "lastName": "Papadopoulos",
              "gender": "M",
              "birthDate": "15-05-1995",
              "taxId": "987654321",
              "address": "Patras"
            }
            """;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        repository.deleteAll();
    }

    private void seedCitizen(String payload) {
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/citizens")
                .then()
                .statusCode(201);
    }

    @Test
    void createValidCitizenReturns201() {
        given()
                .contentType(ContentType.JSON)
                .body(SAMPLE_CITIZEN)
                .when()
                .post("/api/citizens")
                .then()
                .statusCode(201)
                .body("id", equalTo("AB123456"))
                .body("firstName", equalTo("Anna"));
    }

    @Test
    void createDuplicateCitizenReturns409() {
        seedCitizen(SAMPLE_CITIZEN);

        given()
                .contentType(ContentType.JSON)
                .body(SAMPLE_CITIZEN)
                .when()
                .post("/api/citizens")
                .then()
                .statusCode(409)
                .body("error", equalTo("Citizen already exists"));
    }

    @Test
    void createCitizenWithInvalidIdReturns400() {
        given()
                .contentType(ContentType.JSON)
                .body(SAMPLE_CITIZEN.replace("AB123456", "short"))
                .when()
                .post("/api/citizens")
                .then()
                .statusCode(400)
                .body("error", equalTo("id: ID must contain exactly 8 characters"));
    }

    @Test
    void createCitizenWithMalformedJsonReturns400() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"id\": \"AB123456\", \"birthDate\": \"invalid-date\"}")
                .when()
                .post("/api/citizens")
                .then()
                .statusCode(400);
    }

    @Test
    void getExistingCitizenReturns200() {
        seedCitizen(SAMPLE_CITIZEN);

        get("/api/citizens/AB123456")
                .then()
                .statusCode(200)
                .body("firstName", equalTo("Anna"))
                .body("lastName", equalTo("Smith"));
    }

    @Test
    void getMissingCitizenReturns404() {
        get("/api/citizens/ZZ123456")
                .then()
                .statusCode(404)
                .body("error", equalTo("Citizen not found"));
    }

    @Test
    void getCitizenWithInvalidIdReturns400() {
        get("/api/citizens/123")
                .then()
                .statusCode(400)
                .body("error", equalTo("Citizen ID must contain exactly 8 characters"));
    }

    @Test
    void updateAllowedFieldsReturns200() {
        seedCitizen(SAMPLE_CITIZEN);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("taxId", "987654321", "address", "Rhodes"))
                .when()
                .patch("/api/citizens/AB123456")
                .then()
                .statusCode(200)
                .body("taxId", equalTo("987654321"))
                .body("address", equalTo("Rhodes"));
    }

    @Test
    void updateWithInvalidTaxIdReturns400() {
        seedCitizen(SAMPLE_CITIZEN);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("taxId", "invalid"))
                .when()
                .patch("/api/citizens/AB123456")
                .then()
                .statusCode(400)
                .body("error", equalTo("taxId: Tax ID must contain exactly 9 digits"));
    }

    @Test
    void updateWithUnknownFieldReturns400() {
        seedCitizen(SAMPLE_CITIZEN);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("firstName", "UnauthorizedChange"))
                .when()
                .patch("/api/citizens/AB123456")
                .then()
                .statusCode(400);
    }

    @Test
    void updateMissingCitizenReturns404() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("address", "Rhodes"))
                .when()
                .patch("/api/citizens/ZZ123456")
                .then()
                .statusCode(404);
    }

    @Test
    void getAllCitizensReturns200() {
        seedCitizen(SAMPLE_CITIZEN);
        seedCitizen(SECOND_CITIZEN);

        get("/api/citizens")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2));
    }

    @Test
    void filterCitizensByValidFieldsReturnsMatchingCitizens() {
        seedCitizen(SAMPLE_CITIZEN);
        seedCitizen(SECOND_CITIZEN);

        given()
                .queryParam("firstName", "Anna")
                .queryParam("gender", "F")
                .when()
                .get("/api/citizens")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].id", equalTo("AB123456"));
    }

    @Test
    void filterCitizensByInvalidTaxIdReturns400() {
        given()
                .queryParam("taxId", "invalid")
                .when()
                .get("/api/citizens")
                .then()
                .statusCode(400)
                .body("error", equalTo("Invalid tax ID"));
    }

    @Test
    void filterCitizensByUnknownParameterReturns400() {
        given()
                .queryParam("unknownField", "value")
                .when()
                .get("/api/citizens")
                .then()
                .statusCode(400);
    }

    @Test
    void filterCitizensByInvalidBirthDateReturns400() {
        given()
                .queryParam("birthDate", "31-02-2000")
                .when()
                .get("/api/citizens")
                .then()
                .statusCode(400)
                .body("error", equalTo("Invalid birth date"));
    }

    @Test
    void deleteExistingCitizenReturns204() {
        seedCitizen(SAMPLE_CITIZEN);

        delete("/api/citizens/AB123456")
                .then()
                .statusCode(204);
    }

    @Test
    void deleteMissingCitizenReturns404() {
        delete("/api/citizens/ZZ123456")
                .then()
                .statusCode(404)
                .body("error", equalTo("Citizen not found"));
    }
}
