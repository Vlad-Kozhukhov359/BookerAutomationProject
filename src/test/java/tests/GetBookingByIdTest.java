package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.client.APIClient;
import core.models.BookingDates;
import core.models.CreatedBooking;
import core.models.NewBooking;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class GetBookingByIdTest {
    private static final Logger logger = LoggerFactory.getLogger(GetBookingByIdTest.class);
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private CreatedBooking createdBooking;
    private NewBooking expectedBooking;

    @BeforeEach
    public void setup() throws JsonProcessingException {
        logger.info("Setting up test environment...");
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();

        // Создаем тестовое бронирование
        expectedBooking = new NewBooking();
        expectedBooking.setFirstname("Alice");
        expectedBooking.setLastname("Smith");
        expectedBooking.setTotalprice(200);
        expectedBooking.setDepositpaid(true);
        expectedBooking.setAdditionalneeds("Breakfast");
        expectedBooking.setBookingdates(new BookingDates("2024-02-01", "2024-02-05"));

        String requestBody = objectMapper.writeValueAsString(expectedBooking);
        Response response = apiClient.createBooking(requestBody);
        createdBooking = objectMapper.readValue(response.asString(), CreatedBooking.class);
        logger.info("Created booking with ID: {}", createdBooking.getBookingid());
    }

    @Test
    public void getBookingById() throws JsonProcessingException {
        logger.info("Starting getBookingById test");

        // Отправляем запрос на получение бронирования
        Response response = apiClient.getBookingId(createdBooking.getBookingid());

        // Проверяем статус код
        assertThat(response.getStatusCode()).isEqualTo(200);
        logger.info("Response status code: {}", response.getStatusCode());

        // Десериализуем ответ
        NewBooking actualBooking = objectMapper.readValue(response.asString(), NewBooking.class);

        // Проверяем поля бронирования
        assertThat(actualBooking.getFirstname()).isEqualTo(expectedBooking.getFirstname());
        assertThat(actualBooking.getLastname()).isEqualTo(expectedBooking.getLastname());
        assertThat(actualBooking.getTotalprice()).isEqualTo(expectedBooking.getTotalprice());
        assertThat(actualBooking.isDepositpaid()).isEqualTo(expectedBooking.isDepositpaid());
        assertThat(actualBooking.getAdditionalneeds()).isEqualTo(expectedBooking.getAdditionalneeds());

        // Проверяем даты
        assertThat(actualBooking.getBookingdates().getCheckin())
                .isEqualTo(expectedBooking.getBookingdates().getCheckin());
        assertThat(actualBooking.getBookingdates().getCheckout())
                .isEqualTo(expectedBooking.getBookingdates().getCheckout());
    }

    @AfterEach
    public void tearDown() {
        logger.info("Starting teardown...");
        // Получаем токен для удаления
        apiClient.createToken("admin", "password123");

        // Удаляем тестовое бронирование
        apiClient.deleteBooking(createdBooking.getBookingid());

        // Проверяем что бронирование удалено
        Response response = apiClient.getBookingId(createdBooking.getBookingid());
        assertThat(response.getStatusCode()).isEqualTo(404);
        logger.info("Booking with ID {} successfully deleted", createdBooking.getBookingid());
    }
}