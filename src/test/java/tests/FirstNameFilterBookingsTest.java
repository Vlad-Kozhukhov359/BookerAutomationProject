package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.client.APIClient;
import core.models.BookingDates;
import core.models.CreatedBooking;
import core.models.NewBooking;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FirstNameFilterBookingsTest {
    private static final Logger logger = LoggerFactory.getLogger(FirstNameFilterBookingsTest.class);
    private APIClient apiClient = new APIClient();
    private ObjectMapper objectMapper = new ObjectMapper();
    private int createdBookingId;

    @Test
    public void createFilterAndDeleteBooking() throws JsonProcessingException {
        // 1. Создаем тестовое бронирование
        NewBooking testBooking = new NewBooking();
        testBooking.setFirstname("Alice");
        testBooking.setLastname("Johnson");
        testBooking.setTotalprice(200);
        testBooking.setDepositpaid(true);
        testBooking.setBookingdates(new BookingDates("2024-05-01", "2024-05-10"));

        Response createResponse = apiClient.createBooking(objectMapper.writeValueAsString(testBooking));
        CreatedBooking createdBooking = objectMapper.readValue(createResponse.asString(), CreatedBooking.class);
        createdBookingId = createdBooking.getBookingid();
        logger.info("Created booking ID: {}", createdBookingId);

        // 2. Фильтруем по firstname
        Response filterResponse = apiClient.getBookingWithParams("firstname", "Alice");
        logger.info("Filter response:\n{}", filterResponse.getBody().prettyPrint());

        // 3. Проверяем результаты фильтрации
        assertThat(filterResponse.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = filterResponse.jsonPath();
        List<Integer> bookingIds = jsonPath.getList("bookingid");

        assertThat(bookingIds)
                .hasSize(1)
                .containsExactly(createdBookingId);

        // 4. Дополнительная проверка данных бронирования
        Response getResponse = apiClient.getBookingId(createdBookingId);
        NewBooking retrievedBooking = objectMapper.readValue(getResponse.asString(), NewBooking.class);

        assertThat(retrievedBooking.getFirstname()).isEqualTo("Alice");
        assertThat(retrievedBooking.getLastname()).isEqualTo("Johnson");
    }

    @AfterEach
    public void tearDown() {
        // 5. Удаляем созданное бронирование
        if (apiClient != null && createdBookingId != 0) {
            apiClient.createToken("admin", "password123");
            apiClient.deleteBooking(createdBookingId);

            // Проверяем удаление
            Response checkResponse = apiClient.getBookingId(createdBookingId);
            assertThat(checkResponse.getStatusCode()).isEqualTo(404);
            logger.info("Booking ID {} successfully deleted", createdBookingId);
        }
    }
}