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
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateBookingTest {

    private static final Logger logger = LoggerFactory.getLogger(CreateBookingTest.class);
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private CreatedBooking createdBooking; // храним созданное бронирование , Ответ
    private NewBooking newBooking; // Новый объект для создания бронирования, Запрос

    // Инициализация API клиента и создание объекта Booking перед каждым тестом
    @BeforeEach
    public void setup() {
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

        logger.info("New booking created: {}", newBooking);
    }

    @Test
    public void createBooking() throws JsonProcessingException {
        logger.info("Starting the createBooking test...");

        // Выполняем запрос к эндпоинту /booking через APIClient
        String requestBody = objectMapper.writeValueAsString(newBooking);
        logger.debug("Request body: {}", requestBody);
        Response response = apiClient.createBooking(requestBody);

        // Проверяем, что статус код ответа равен 200
        assertThat(response.getStatusCode()).isEqualTo(200);


        String responseBody = response.asString();
        createdBooking = objectMapper.readValue(responseBody, CreatedBooking.class);
        logger.debug("Response body: {}", responseBody);

        assertThat(createdBooking).isNotNull();
        logger.info("Created booking: {}", createdBooking);

        assertEquals(createdBooking.getBooking().getFirstname(), newBooking.getFirstname());
        assertEquals(createdBooking.getBooking().getLastname(), newBooking.getLastname());
        assertEquals(createdBooking.getBooking().getTotalprice(), newBooking.getTotalprice());
        assertEquals(createdBooking.getBooking().isDepositpaid(), newBooking.isDepositpaid());
        assertEquals(createdBooking.getBooking().getBookingdates().getCheckin(), newBooking.getBookingdates().getCheckin());
        assertEquals(createdBooking.getBooking().getBookingdates().getCheckout(), newBooking.getBookingdates().getCheckout());
        assertEquals(createdBooking.getBooking().getAdditionalneeds(), newBooking.getAdditionalneeds());

        logger.info("createBooking test completed successfully.");
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