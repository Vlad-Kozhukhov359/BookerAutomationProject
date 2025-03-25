package tests;

import core.client.APIClient;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthCheckTests {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckTests.class);

    private APIClient apiClient;

    //Инициализация АПИ клиента перед каждым тестом , прериквест
    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
    }

    // Тест на метод ping()
    @Test
    public void testPing() {
        logger.info("Starting testPing");

        // Выполнение GET запроса на /ping через APIClient
        Response response = apiClient.ping();

        // Логирование статуса ответа и тела ответа
        logger.info("Код ответа: {}", response.getStatusCode());
        logger.info("Тело ответа: {}", response.getBody().asString());

        assertThat(response.getStatusCode()).isEqualTo(201);
    }
}
