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

public class LastNameFilterBookingsTest {
    private static final Logger logger = LoggerFactory.getLogger(LastNameFilterBookingsTest.class);
    private APIClient apiClient = new APIClient();
    private ObjectMapper objectMapper = new ObjectMapper();
    private int createdBookingId;

    @Test
    public void createFilterByLastNameAndDelete() throws JsonProcessingException {
        // 1. Создаем бронирование с нужным lastName
        NewBooking testBooking = new NewBooking();
        testBooking.setFirstname("Alex");
        testBooking.setLastname("Kozhukhov");
        testBooking.setTotalprice(300);
        testBooking.setDepositpaid(false);
        testBooking.setBookingdates(new BookingDates("2024-06-01", "2024-06-05"));

        Response createResponse = apiClient.createBooking(objectMapper.writeValueAsString(testBooking));
        CreatedBooking createdBooking = objectMapper.readValue(createResponse.asString(), CreatedBooking.class);
        createdBookingId = createdBooking.getBookingid();
        logger.info("Created booking ID: {} for {}", createdBookingId, testBooking.getLastname());

        // 2. Фильтруем по lastName
        Response filterResponse = apiClient.getBookingWithParams("lastname", "Kozhukhov");
        logger.info("Filter response:\n{}", filterResponse.getBody().prettyPrint());

        // 3. Проверяем результаты фильтрации
        assertThat(filterResponse.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = filterResponse.jsonPath();
        List<Integer> bookingIds = jsonPath.getList("bookingid");

        assertThat(bookingIds)
                .hasSize(1)
                .containsExactly(createdBookingId);

        // 4. Дополнительная проверка данных
        Response getResponse = apiClient.getBookingId(createdBookingId);
        NewBooking retrievedBooking = objectMapper.readValue(getResponse.asString(), NewBooking.class);

        assertThat(retrievedBooking.getLastname())
                .as("Check lastname in response")
                .isEqualTo("Kozhukhov");
    }

    @AfterEach
    public void cleanUp() {
        // 5. Удаление с проверкой
        if (apiClient != null && createdBookingId != 0) {
            apiClient.createToken("admin", "password123");
            apiClient.deleteBooking(createdBookingId);

            Response checkResponse = apiClient.getBookingId(createdBookingId);
            assertThat(checkResponse.getStatusCode())
                    .as("Check booking deletion")
                    .isEqualTo(404);

            logger.info("Deleted booking with lastName 'Kozhukhov', ID: {}", createdBookingId);
        }
    }
}