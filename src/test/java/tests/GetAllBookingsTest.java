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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class GetAllBookingsTest {

    private static final Logger logger = LoggerFactory.getLogger(GetAllBookingsTest.class);
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private CreatedBooking createdBooking; // Храним созданное бронирование
    private NewBooking newBooking; // Новый объект для создания бронирования

    // Инициализация API клиента и создание объекта Booking перед каждым тестом
    @BeforeEach
    public void setup() throws JsonProcessingException {
        logger.info("Setting up the test environment...");
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();

        // Создаем объект Booking с необходимыми данными
        newBooking = new NewBooking();
        newBooking.setFirstname("John");
        newBooking.setLastname("Doe");
        newBooking.setTotalprice(150);
        newBooking.setDepositpaid(true);
        newBooking.setBookingdates(new BookingDates("2024-01-01", "2024-01-05")); // Даты

        // Создаем бронирование перед тестом
        String requestBody = objectMapper.writeValueAsString(newBooking);
        Response response = apiClient.createBooking(requestBody);
        createdBooking = objectMapper.readValue(response.asString(), CreatedBooking.class);
        logger.info("Created booking with ID: {}", createdBooking.getBookingid());
    }

    @Test
    public void getAllBookings() {
        logger.info("Starting the getAllBookings test");

        // Отправляем GET-запрос на /booking
        Response response = apiClient.getBooking();

        // Проверяем, что статус код ответа равен 200
        assertThat(response.getStatusCode()).isEqualTo(200);
        logger.info("Response status code: {}", response.getStatusCode());

        // Проверяем, что тело ответа содержит массив с объектами бронирований
        List<?> bookings = response.jsonPath().getList("$");
        assertThat(bookings).isNotEmpty(); // Проверяем, что массив не пустой
        logger.info("Number of bookings returned: {}", bookings.size());

        // Проверяем, что каждое бронирование имеет bookingid
        for (Object booking : bookings) {
            assertThat(((Map<?, ?>) booking).get("bookingid")).isNotNull();
        }
    }

        @AfterEach
        public void tearDown() {
            logger.info("Tearing down start");
            apiClient.createToken("admin", "password123");
            apiClient.deleteBooking(createdBooking.getBookingid());
            assertThat(apiClient.getBookingId(createdBooking.getBookingid()).getStatusCode()).isEqualTo(404);
            logger.info("Deleted booking with ID: {}", createdBooking.getBookingid());
    }
}