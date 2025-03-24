package tests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.client.APIClient;
import core.models.Booking;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GetBookingTest {

    private static final Logger logger = LoggerFactory.getLogger(GetBookingTest.class);

    private APIClient apiClient;
    private ObjectMapper objectMapper;

    // Инициализация Апи клиента перед каждым тестом
    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testGetBooking() throws Exception {

        logger.info("Starting testGetBooking");

        // Выполнение запроса к эндпоинту /booking через APIClient
        Response response = apiClient.getBooking();

        // Логирование статуса ответа и тела ответа
        logger.info("Код ответа: {}", response.getStatusCode());
        logger.info("Тело Ответа: {}", response.getBody().asString());

        assertThat(response.getStatusCode()).isEqualTo(200);

        // Десериализуем тело ответа в список объектов Booking
        String responseBody = response.getBody().asString();
        List<Booking> bookings = objectMapper.readValue(responseBody, new TypeReference<List<Booking>>() {
        });

        // Проверяем, что тело ответа содержит объект Booking
        assertThat(bookings).isNotEmpty(); // Проверяем, что список не пуст

        // Проверяем, что каждый объект Booking содержит валидное значение bookingid
        for (Booking booking : bookings) {
            assertThat(booking.getBookingid()).isGreaterThan(0); // bookingid должен быть больше 0
        }
    }
}
