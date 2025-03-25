package core.client;


import core.settings.ApiEndpoints;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class APIClient {

    // Логирование
    private static final Logger logger = LoggerFactory.getLogger(APIClient.class);

    private final String baseUrl;

    public APIClient() {
        this.baseUrl = determineBaseUrl();
    }

    // Определение базового URL на основе файла конфигурации
    private String determineBaseUrl() {
        String environment = System.getProperty("env", "test");
        String configFileName = "application-" + environment + ".properties";

        Properties properties = new Properties();
        try (InputStream input =
                     getClass().getClassLoader().getResourceAsStream(configFileName)) {
            if (input == null) {
                logger.error("Configuration file not found: {}", configFileName);
                throw new IllegalStateException("Configuration file not found: "
                        + configFileName);
            }
            properties.load(input);
        } catch (IOException e) {
            logger.error("Unable to load configuration file: {}", configFileName, e);
            throw new IllegalStateException("Unable to load configuration file: " + configFileName, e);
        }

        return properties.getProperty("baseUrl");
    }

    //Настройка базовых параметров HTTP-запросов
    private RequestSpecification getRequestSpec() {
        return RestAssured.given()
                .baseUri(baseUrl)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
    }

    // GET запрос на эндпоинт / ping
    public Response ping() {
        logger.info("Sending GET request to /ping");
        Response response = getRequestSpec()
                .when()
                .get(ApiEndpoints.PING.getPath()) // Используем ENUM для эндпоинта / ping
                .then()
                .statusCode(201)
                .extract()
                .response();
        logger.info("Received response for /ping: status code {}", response.getStatusCode());
        return response;
    }

    // GET запрос на эндпоинт / booking
    public Response getBooking() {
        logger.info("Sending GET request to /booking");
        Response response = getRequestSpec()
                .when()
                .get(ApiEndpoints.BOOKING.getPath()) // Используем ENUM для эндпоинта / ping
                .then()
                .statusCode(200)
                .extract()
                .response();
        logger.info("Received response for /booking: status code {}", response.getStatusCode());
        return response;
    }

    // GET запрос на эндпоинт / booking/{id}
    public Response getBookingId(int id) {
        logger.info("Sending GET request to /booking/{}", id);
        Response response = getRequestSpec()
                .when()
                .get(ApiEndpoints.BOOKING.getPathWithId(id)) // Используем метод в который можно подставлять id
                .then()
                .statusCode(200)
                .extract()
                .response();
        logger.info("Received response for /booking/{}: status code {}", id, response.getStatusCode());
        return response;
    }
}
