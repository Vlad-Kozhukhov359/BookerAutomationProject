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

    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private CreatedBooking createdBooking; // храним созданное бронирование , Ответ
    private NewBooking newBooking; // Новый объект для создания бронирования, Запрос

    // Инициализация API клиента и создание объекта Booking перед каждым тестом
    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();

        // Создаем объект Booking с необходимыми данными
        newBooking = new NewBooking();
        newBooking.setFirstname("John");
        newBooking.setLastname("Doe");
        newBooking.setTotalprice(150);
        newBooking.setDepositpaid(true);
        newBooking.setBookingdates(new BookingDates("2024-01-01", "2024-01-05")); // Даты
    }

    @Test
    public void createBooking() throws JsonProcessingException {
        // Выполняем запрос к эндпоинту /booking через APIClient
        objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(newBooking);
        Response response = apiClient.createBooking(requestBody);

        // Проверяем, что статус код ответа равен 200
        assertThat(response.getStatusCode()).isEqualTo(200);


        String responseBody = response.asString();
        createdBooking = objectMapper.readValue(responseBody, CreatedBooking.class);

        assertThat(createdBooking).isNotNull();

        assertEquals(createdBooking.getBooking().getFirstname(), newBooking.getFirstname());
        assertEquals(createdBooking.getBooking().getLastname(), newBooking.getLastname());
        assertEquals(createdBooking.getBooking().getTotalprice(), newBooking.getTotalprice());
        assertEquals(createdBooking.getBooking().isDepositpaid(), newBooking.isDepositpaid());
        assertEquals(createdBooking.getBooking().getBookingdates().getCheckin(), newBooking.getBookingdates().getCheckin());
        assertEquals(createdBooking.getBooking().getBookingdates().getCheckout(), newBooking.getBookingdates().getCheckout());
        assertEquals(createdBooking.getBooking().getAdditionalneeds(), newBooking.getAdditionalneeds());
    }

    @AfterEach
    public void tearDown() {
        apiClient.createToken("admin", "password123");
        apiClient.deleteBooking(createdBooking.getBookingid());
        assertThat(apiClient.getBookingId(createdBooking.getBookingid()).getStatusCode()).isEqualTo(404);
    }
}