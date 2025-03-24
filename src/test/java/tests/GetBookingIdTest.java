package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.client.APIClient;
import core.models.BookingId;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class GetBookingIdTest {

    private static final Logger logger = LoggerFactory.getLogger(GetBookingIdTest.class);

    private APIClient apiClient;
    private ObjectMapper objectMapper;

    // Инициализация API клиента перед каждым тестом
    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testGetBookingId() throws Exception {
        logger.info("Starting testGetBookingId");

        // Указываем ID бронирования, которое хотим протестировать
        int bookingId = 3;

        // Выполняем запрос к конкретному бронированию по ID
        Response bookingResponse = apiClient.getBookingId(bookingId);

        // Логирование статуса ответа и тела ответа
        logger.info("Код ответа для /booking/{}: {}", bookingId, bookingResponse.getStatusCode());
        logger.info("Тело ответа для /booking/{}: {}", bookingId, bookingResponse.getBody().asString());

        // Проверяем, что код ответа 200
        assertThat(bookingResponse.getStatusCode()).isEqualTo(200);

        // Десериализуем тело ответа в объект BookingId
        BookingId bookingDetails = objectMapper.readValue(bookingResponse.getBody().asString(), BookingId.class);

        // Проверяем, что полученные данные соответствуют ожидаемым
        assertThat(bookingDetails).isNotNull();
        assertThat(bookingDetails.getFirstname()).isNotEmpty(); // Проверяем, что имя не пустое
        assertThat(bookingDetails.getLastname()).isNotEmpty(); // Проверяем, что фамилия не пустая
        assertThat(bookingDetails.getTotalprice()).isGreaterThan(0); // Проверяем, что цена больше 0
        assertThat(bookingDetails.isDepositpaid()).isFalse(); // Проверяем, что депозит не оплачен ( false )
        assertThat(bookingDetails.getAdditionalneeds()).isNotEmpty(); // Проверяем, что Additionalneeds не пустые

        // Проверяем, что bookingdates не равен null и содержит корректные даты
        assertThat(bookingDetails.getBookingdates()).isNotNull();
        assertThat(bookingDetails.getBookingdates().getCheckin()).isNotEmpty(); // Проверяем, что дата заезда не пустая
        assertThat(bookingDetails.getBookingdates().getCheckout()).isNotEmpty(); // Проверяем, что дата выезда не пустая
    }
}