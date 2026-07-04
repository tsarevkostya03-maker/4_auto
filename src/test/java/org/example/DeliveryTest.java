package org.example;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;

import java.time.Duration;
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
        // Настройки Selenide. Без ручного указания браузера и WebDriverManager.
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 15000; // Общий таймаут, но лучше использовать Duration в ассертах.

        open("http://localhost:9999");
        // Используем комбинированный селектор на базе data-test-id
        $("[data-test-id='city'] input").shouldBe(visible, Duration.ofSeconds(15));

        faker = new Faker(new Locale("ru"));
        city = generateCity();
        name = faker.name().firstName() + " " + faker.name().lastName();
        phone = "+7" + faker.number().digits(10);
    }

    @Test
    void shouldSubmitDeliveryForm() {
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.click();
        cityField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        cityField.setValue(city);

        String deliveryDate = generateDeliveryDate();
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.click();
        dateField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        dateField.setValue(deliveryDate);

        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.click();
        nameField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        nameField.setValue(name);

        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.click();
        phoneField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        phoneField.setValue(phone);

        SelenideElement agreementCheckbox = $("[data-test-id='agreement']");
        if (!agreementCheckbox.has(checked)) {
            agreementCheckbox.click();
        }

        $("[data-test-id='order'] .button").click();

        // Явные ожидания с Duration
        $("[data-test-id='order'] .button_loading")
                .shouldBe(visible, Duration.ofSeconds(15))
                .shouldBe(disappear, Duration.ofSeconds(15));

        $("[data-test-id='success-notification'] .notification__content")
                .shouldBe(visible, Duration.ofSeconds(15))
                .shouldHave(text("Встреча успешно забронирована"), Duration.ofSeconds(15));
    }

    @Test
    void shouldShowValidationErrorForInvalidCity() {
        $("[data-test-id='city'] input").setValue("InvalidCity");
        $("[data-test-id='date'] input").setValue(generateDeliveryDate());
        $("[data-test-id='name'] input").setValue(name);
        $("[data-test-id='phone'] input").setValue(phone);
        $("[data-test-id='agreement']").click();
        $("[data-test-id='order'] .button").click();

        $("[data-test-id='city'] .input__sub")
                .shouldBe(visible, Duration.ofSeconds(15))
                .shouldHave(text("Доставка в выбранный город недоступна"));
    }

    @Test
    void shouldShowValidationErrorForInvalidName() {
        $("[data-test-id='city'] input").setValue(city);
        $("[data-test-id='date'] input").setValue(generateDeliveryDate());
        $("[data-test-id='name'] input").setValue("John Doe");
        $("[data-test-id='phone'] input").setValue(phone);
        $("[data-test-id='agreement']").click();
        $("[data-test-id='order'] .button").click();

        $("[data-test-id='name'] .input__sub")
                .shouldBe(visible, Duration.ofSeconds(15))
                .shouldHave(text("Имя и Фамилия должны содержать только русские буквы, дефисы и пробелы"));
    }

    @Test
    void shouldShowValidationErrorForInvalidPhone() {
        $("[data-test-id='city'] input").setValue(city);
        $("[data-test-id='date'] input").setValue(generateDeliveryDate());
        $("[data-test-id='name'] input").setValue(name);
        $("[data-test-id='phone'] input").setValue("8912345678");
        $("[data-test-id='agreement']").click();
        $("[data-test-id='order'] .button").click();

        $("[data-test-id='phone'] .input__sub")
                .shouldBe(visible, Duration.ofSeconds(15))
                .shouldHave(text("Телефон должен содержать 11 цифр и начинаться с +"));
    }

    @Test
    void shouldShowValidationErrorForEmptyDate() {
        $("[data-test-id='city'] input").setValue(city);
        $("[data-test-id='date'] input").setValue("");
        $("[data-test-id='name'] input").setValue(name);
        $("[data-test-id='phone'] input").setValue(phone);
        $("[data-test-id='agreement']").click();
        $("[data-test-id='order'] .button").click();

        $("[data-test-id='date'] .input__sub")
                .shouldBe(visible, Duration.ofSeconds(15))
                .shouldHave(text("Неверно введена дата"));
    }

    @Test
    void shouldShowValidationErrorForPastDate() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        String pastDateString = pastDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        $("[data-test-id='city'] input").setValue(city);
        $("[data-test-id='date'] input").setValue(pastDateString);
        $("[data-test-id='name'] input").setValue(name);
        $("[data-test-id='phone'] input").setValue(phone);
        $("[data-test-id='agreement']").click();
        $("[data-test-id='order'] .button").click();

        $("[data-test-id='date'] .input__sub")
                .shouldBe(visible, Duration.ofSeconds(15))
                .shouldHave(text("Дата должна быть не ранее трёх дней с текущей даты"));
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