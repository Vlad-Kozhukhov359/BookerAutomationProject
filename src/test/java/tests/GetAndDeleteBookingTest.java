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

public class GetAndDeleteBookingTest {

    private static final Logger logger = LoggerFactory.getLogger(GetAndDeleteBookingTest.class);

    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private int bookingId; // Переменная для хранения ID бронирования

    // Инициализация Апи клиента перед каждым тестом
    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
        apiClient.createToken("admin", "password123");
    }

    @Test
    public void testGetAndDeleteBooking() throws Exception {
        logger.info("Starting testGetAndDeleteBooking");

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

        // Сохраняем ID первого бронирования для использования в тесте удаления
        bookingId = bookings.get(0).getBookingid(); // Сохраняем ID первого бронирования
        logger.info("Сохраненный bookingId: {}", bookingId); // Логируем значение bookingId

        // Выполнение запроса на удаление бронирования
        Response deleteResponse = apiClient.deleteBooking(bookingId);

        // Логирование статуса ответа на удаление
        logger.info("Код ответа на удаление: {}", deleteResponse.getStatusCode());

        // Проверяем, что статус ответа 200 (ОК)
        assertThat(deleteResponse.getStatusCode()).isEqualTo(201);
    }
}
