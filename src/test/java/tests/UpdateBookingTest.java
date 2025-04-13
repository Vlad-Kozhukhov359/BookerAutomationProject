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

public class UpdateBookingTest {
    private static final Logger logger = LoggerFactory.getLogger(UpdateBookingTest.class);
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private CreatedBooking createdBooking;
    private NewBooking originalBooking;
    private NewBooking updatedBooking;

    @BeforeEach
    public void setup() throws JsonProcessingException {
        logger.info("Setup start");
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();

        // Создаем исходное бронирование
        originalBooking = new NewBooking();
        originalBooking.setFirstname("Emma");
        originalBooking.setLastname("Wilson");
        originalBooking.setTotalprice(300);
        originalBooking.setDepositpaid(false);
        originalBooking.setAdditionalneeds("WiFi");
        originalBooking.setBookingdates(new BookingDates("2024-03-01", "2024-03-10"));

        String requestBody = objectMapper.writeValueAsString(originalBooking);
        Response response = apiClient.createBooking(requestBody);
        createdBooking = objectMapper.readValue(response.asString(), CreatedBooking.class);
        logger.info("Created initial booking with ID: {}", createdBooking.getBookingid());

        // Подготавливаем обновленные данные
        updatedBooking = new NewBooking();
        updatedBooking.setFirstname("UpdatedEmma");
        updatedBooking.setLastname("UpdatedWilson");
        updatedBooking.setTotalprice(450);
        updatedBooking.setDepositpaid(true);
        updatedBooking.setAdditionalneeds("Breakfast");
        updatedBooking.setBookingdates(new BookingDates("2024-03-05", "2024-03-15"));
    }

    @Test
    public void updateBooking() throws JsonProcessingException {
        logger.info("Starting updateBooking test");

        // Получаем токен для аутентификации
        apiClient.createToken("admin", "password123");

        // Отправляем PUT-запрос
        String updateBody = objectMapper.writeValueAsString(updatedBooking);
        Response response = apiClient.putBooking(createdBooking.getBookingid(), updateBody);

        // Проверяем статус код
        assertThat(response.getStatusCode()).isEqualTo(200);
        logger.info("PUT response status: {}", response.getStatusCode());

        // Проверяем обновленные данные
        NewBooking responseBooking = objectMapper.readValue(response.asString(), NewBooking.class);

        assertThat(responseBooking.getFirstname()).isEqualTo(updatedBooking.getFirstname());
        assertThat(responseBooking.getLastname()).isEqualTo(updatedBooking.getLastname());
        assertThat(responseBooking.getTotalprice()).isEqualTo(updatedBooking.getTotalprice());
        assertThat(responseBooking.isDepositpaid()).isEqualTo(updatedBooking.isDepositpaid());
        assertThat(responseBooking.getAdditionalneeds()).isEqualTo(updatedBooking.getAdditionalneeds());
        assertThat(responseBooking.getBookingdates().getCheckin()).isEqualTo(updatedBooking.getBookingdates().getCheckin());
        assertThat(responseBooking.getBookingdates().getCheckout()).isEqualTo(updatedBooking.getBookingdates().getCheckout());
    }

    @AfterEach
    public void tearDown() {
        logger.info("tearDown Start");
        apiClient.createToken("admin", "password123");
        apiClient.deleteBooking(createdBooking.getBookingid());

        // Проверяем что бронирование удалено
        Response response = apiClient.getBookingId(createdBooking.getBookingid());
        assertThat(response.getStatusCode()).isEqualTo(404);
        logger.info("Cleanup completed for booking ID: {}", createdBooking.getBookingid());
    }
}
