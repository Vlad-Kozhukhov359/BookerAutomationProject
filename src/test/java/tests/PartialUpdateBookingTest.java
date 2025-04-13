package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.client.APIClient;
import core.models.BookingDates;
import core.models.CreatedBooking;
import core.models.NewBooking;
import core.models.PartialUpdateRequest;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class PartialUpdateBookingTest {
    private static final Logger logger = LoggerFactory.getLogger(PartialUpdateBookingTest.class);
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private CreatedBooking createdBooking;
    private NewBooking originalBooking;
    private PartialUpdateRequest partialUpdate;

    // Модель для частичного обновления (models/PartialUpdateRequest)
    public static class PartialUpdateRequest {
        private String firstname;
        private String additionalneeds;

        // Геттеры и сеттеры
        public String getFirstname() { return firstname; }
        public void setFirstname(String firstname) { this.firstname = firstname; }

        public String getAdditionalneeds() { return additionalneeds; }
        public void setAdditionalneeds(String additionalneeds) {
            this.additionalneeds = additionalneeds;
        }
    }

    @BeforeEach
    public void setup() throws JsonProcessingException {
        logger.info("Setup start");
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();

        // Создаем исходное бронирование
        originalBooking = new NewBooking();
        originalBooking.setFirstname("Michael");
        originalBooking.setLastname("Brown");
        originalBooking.setTotalprice(250);
        originalBooking.setDepositpaid(true);
        originalBooking.setAdditionalneeds("Lunch");
        originalBooking.setBookingdates(new BookingDates("2024-04-01", "2024-04-05"));

        String requestBody = objectMapper.writeValueAsString(originalBooking);
        Response response = apiClient.createBooking(requestBody);
        createdBooking = objectMapper.readValue(response.asString(), CreatedBooking.class);
        logger.info("Created initial booking with ID: {}", createdBooking.getBookingid());

        // Изменение 2 полей
        partialUpdate = new PartialUpdateRequest();
        partialUpdate.setFirstname("UpdatedMichael");
        partialUpdate.setAdditionalneeds("Dinner");

        // Логируем данные для PATCH
        String patchData = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(partialUpdate);
        logger.info("Prepared PATCH data:\n{}", patchData);
    }

    @Test
    public void partialUpdateBooking() throws JsonProcessingException {
        logger.info("Starting partial update test");

        // Аутентификация
        apiClient.createToken("admin", "password123");

        // Отправляем PATCH
        String patchBody = objectMapper.writeValueAsString(partialUpdate);
        logger.info("Sending PATCH body:\n{}", patchBody);

        Response patchResponse = apiClient.patchBooking(
                createdBooking.getBookingid(),
                patchBody
        );

        // Проверка статуса
        assertThat(patchResponse.getStatusCode()).isEqualTo(200);
        logger.info("PATCH response status: {}", patchResponse.getStatusCode());

        // Получаем актуальные данные
        Response getResponse = apiClient.getBookingId(createdBooking.getBookingid());
        NewBooking updatedBooking = objectMapper.readValue(
                getResponse.asString(),
                NewBooking.class
        );
        logger.info("Updated booking data:\n{}", getResponse.getBody().prettyPrint());

        // Проверка измененных полей
        assertThat(updatedBooking.getFirstname())
                .isEqualTo(partialUpdate.getFirstname());
        assertThat(updatedBooking.getAdditionalneeds())
                .isEqualTo(partialUpdate.getAdditionalneeds());

        // Проверка неизмененных полей
        assertThat(updatedBooking.getLastname())
                .isEqualTo(originalBooking.getLastname());
        assertThat(updatedBooking.getTotalprice())
                .isEqualTo(originalBooking.getTotalprice());
        assertThat(updatedBooking.isDepositpaid())
                .isEqualTo(originalBooking.isDepositpaid());
        assertThat(updatedBooking.getBookingdates())
                .usingRecursiveComparison()
                .isEqualTo(originalBooking.getBookingdates());
    }

    @AfterEach
    public void tearDown() {
        logger.info("Cleaning up...");
        apiClient.createToken("admin", "password123");
        apiClient.deleteBooking(createdBooking.getBookingid());

        Response response = apiClient.getBookingId(createdBooking.getBookingid());
        assertThat(response.getStatusCode()).isEqualTo(404);
        logger.info("Cleanup completed for booking ID: {}", createdBooking.getBookingid());
    }
}