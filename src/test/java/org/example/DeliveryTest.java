package org.example;

import com.codeborne.selenide.Configuration;
import com.github.javafaker.Faker;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;

public class DeliveryTest {
    private Faker faker;
    private String city;
    private String name;
    private String phone;

    @BeforeEach
    void setUp() {
        WebDriverManager.firefoxdriver().setup();
        Configuration.browser = "firefox";
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 15000;
        Configuration.headless = false;

        open("http://localhost:9999");
        $("h1").shouldBe(visible);

        faker = new Faker(new Locale("ru"));
        city = generateCity();
        name = faker.name().firstName() + " " + faker.name().lastName();
        phone = "+7" + faker.number().digits(10);
    }

    @Test
    void shouldSubmitDeliveryForm() {
        // Заполняем форму
        $("#city").setValue(city);
        $("#date").setValue(generateDeliveryDate());
        $("#name").setValue(name);
        $("#phone").setValue(phone);
        $("#agreement").click();
        $("#submitBtn").click();

        // Проверяем, что кнопка стала загрузочной
        $("#submitBtn.button_loading").shouldBe(visible);

        // Ждём, пока загрузка исчезнет
        $("#submitBtn.button_loading").shouldBe(disappear);

        // Проверяем, что появилось уведомление об успехе
        $("#successNotification").shouldBe(visible);
        $("#successMessage").shouldHave(text("Встреча успешно забронирована"));
    }

    @Test
    void shouldShowValidationErrorForInvalidCity() {
        // Вводим невалидный город
        $("#city").setValue("InvalidCity");
        $("#date").setValue(generateDeliveryDate());
        $("#name").setValue(name);
        $("#phone").setValue(phone);
        $("#agreement").click();
        $("#submitBtn").click();

        // Проверяем ошибку для города
        $("#cityError").shouldBe(visible);
        $("#cityError").shouldHave(text("Доставка в выбранный город недоступна"));
    }

    @Test
    void shouldShowValidationErrorForInvalidName() {
        // Вводим имя латиницей
        $("#city").setValue(city);
        $("#date").setValue(generateDeliveryDate());
        $("#name").setValue("John Doe");
        $("#phone").setValue(phone);
        $("#agreement").click();
        $("#submitBtn").click();

        // Проверяем ошибку для имени
        $("#nameError").shouldBe(visible);
        $("#nameError").shouldHave(text("Имя и Фамилия должны содержать только русские буквы, дефисы и пробелы"));
    }

    @Test
    void shouldShowValidationErrorForInvalidPhone() {
        // Вводим невалидный телефон
        $("#city").setValue(city);
        $("#date").setValue(generateDeliveryDate());
        $("#name").setValue(name);
        $("#phone").setValue("8912345678");
        $("#agreement").click();
        $("#submitBtn").click();

        // Проверяем ошибку для телефона
        $("#phoneError").shouldBe(visible);
        $("#phoneError").shouldHave(text("Телефон должен содержать 11 цифр и начинаться с +"));
    }

    @Test
    void shouldShowValidationErrorForEmptyDate() {
        // Оставляем дату пустой
        $("#city").setValue(city);
        $("#date").setValue("");
        $("#name").setValue(name);
        $("#phone").setValue(phone);
        $("#agreement").click();
        $("#submitBtn").click();

        // Проверяем ошибку для даты
        $("#dateError").shouldBe(visible);
        $("#dateError").shouldHave(text("Неверно введена дата"));
    }

    @Test
    void shouldShowValidationErrorForPastDate() {
        // Вводим дату в прошлом
        LocalDate pastDate = LocalDate.now().minusDays(1);
        String pastDateString = pastDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        $("#city").setValue(city);
        $("#date").setValue(pastDateString);
        $("#name").setValue(name);
        $("#phone").setValue(phone);
        $("#agreement").click();
        $("#submitBtn").click();

        // Проверяем ошибку для даты
        $("#dateError").shouldBe(visible);
        $("#dateError").shouldHave(text("Дата должна быть не ранее трёх дней с текущей даты"));
    }

    private String generateCity() {
        String[] cities = {
                "Москва", "Санкт-Петербург", "Новосибирск", "Екатеринбург",
                "Казань", "Нижний Новгород", "Челябинск", "Омск",
                "Самара", "Ростов-на-Дону", "Уфа", "Красноярск",
                "Воронеж", "Пермь", "Волгоград", "Краснодар"
        };
        return cities[faker.number().numberBetween(0, cities.length)];
    }

    private String generateDeliveryDate() {
        LocalDate currentDate = LocalDate.now();
        LocalDate deliveryDate = currentDate.plusDays(faker.number().numberBetween(3, 30));
        return deliveryDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }
}