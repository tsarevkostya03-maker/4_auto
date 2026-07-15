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
    private String deliveryDate;

    @BeforeEach
    void setUp() {
        // Только необходимые настройки для запуска браузера
        Configuration.browser = "firefox";
        Configuration.headless = true;

        open("http://localhost:9999");

        $("[data-test-id='city']").shouldBe(visible, Duration.ofSeconds(10));

        faker = new Faker(new Locale("ru"));
        city = generateCity();
        name = faker.name().firstName() + " " + faker.name().lastName();
        phone = "+7" + faker.number().digits(10);
        deliveryDate = generateDeliveryDate();
    }

    @Test
    void shouldSubmitDeliveryForm() {
        // Город
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.setValue(city);
        $(".menu-item").shouldBe(visible, Duration.ofSeconds(5));
        $(".menu-item").click();

        // Дата
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.setValue(deliveryDate);
        dateField.sendKeys(Keys.ENTER);

        // Имя
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.setValue(name);
        nameField.sendKeys(Keys.TAB);

        // Телефон
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.setValue(phone);
        phoneField.sendKeys(Keys.TAB);

        // Согласие
        SelenideElement agreementLabel = $("[data-test-id='agreement']");
        agreementLabel.click();

        // Кнопка
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        button.click();

        // Проверка уведомления
        SelenideElement notification = $("[data-test-id='success-notification']");
        notification.shouldBe(visible, Duration.ofSeconds(30));

        String expectedMessage = "Встреча успешно запланирована на " + deliveryDate;
        notification.$(".notification__content").shouldHave(text(expectedMessage), Duration.ofSeconds(30));
    }

    @Test
    void shouldShowValidationErrorForInvalidCity() {
        // Невалидный город
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.setValue("InvalidCity");
        cityField.sendKeys(Keys.ENTER);

        // Дата
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.setValue(deliveryDate);
        dateField.sendKeys(Keys.ENTER);

        // Имя
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.setValue(name);
        nameField.sendKeys(Keys.TAB);

        // Телефон
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.setValue(phone);
        phoneField.sendKeys(Keys.TAB);

        // Согласие
        SelenideElement agreementLabel = $("[data-test-id='agreement']");
        agreementLabel.click();

        // Кнопка
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        button.click();

        // Проверка ошибки
        $("[data-test-id='city'] .input__sub")
                .shouldBe(visible, Duration.ofSeconds(15))
                .shouldHave(text("Доставка в выбранный город недоступна"));
    }

    @Test
    void shouldShowValidationErrorForInvalidName() {
        // Город
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.setValue(city);
        $(".menu-item").shouldBe(visible, Duration.ofSeconds(5));
        $(".menu-item").click();

        // Дата
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.setValue(deliveryDate);
        dateField.sendKeys(Keys.ENTER);

        // Невалидное имя
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.setValue("John Doe");
        nameField.sendKeys(Keys.TAB);

        // Телефон
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.setValue(phone);
        phoneField.sendKeys(Keys.TAB);

        // Согласие
        SelenideElement agreementLabel = $("[data-test-id='agreement']");
        agreementLabel.click();

        // Кнопка
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        button.click();

        // Проверка ошибки
        $("[data-test-id='name'] .input__sub")
                .shouldBe(visible, Duration.ofSeconds(15))
                .shouldHave(text("Имя и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы."));
    }

    @Test
    void shouldShowValidationErrorForEmptyDate() {
        // Город
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.setValue(city);
        $(".menu-item").shouldBe(visible, Duration.ofSeconds(5));
        $(".menu-item").click();

        // Пустая дата
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.setValue("");
        dateField.sendKeys(Keys.ENTER);

        // Имя
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.setValue(name);
        nameField.sendKeys(Keys.TAB);

        // Телефон
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.setValue(phone);
        phoneField.sendKeys(Keys.TAB);

        // Согласие
        SelenideElement agreementLabel = $("[data-test-id='agreement']");
        agreementLabel.click();

        // Кнопка
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        button.click();

        // Проверка ошибки
        $("[data-test-id='date'] .input__sub")
                .shouldBe(visible, Duration.ofSeconds(15))
                .shouldHave(text("Неверно введена дата"));
    }

    @Test
    void shouldShowValidationErrorForPastDate() {
        // Дата в прошлом
        LocalDate pastDate = LocalDate.now().minusDays(1);
        String pastDateString = pastDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        // Город
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.setValue(city);
        $(".menu-item").shouldBe(visible, Duration.ofSeconds(5));
        $(".menu-item").click();

        // Дата в прошлом
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.setValue(pastDateString);
        dateField.sendKeys(Keys.ENTER);

        // Имя
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.setValue(name);
        nameField.sendKeys(Keys.TAB);

        // Телефон
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.setValue(phone);
        phoneField.sendKeys(Keys.TAB);

        // Согласие
        SelenideElement agreementLabel = $("[data-test-id='agreement']");
        agreementLabel.click();

        // Кнопка
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        button.click();

        // Проверка ошибки
        $("[data-test-id='date'] .input__sub")
                .shouldBe(visible, Duration.ofSeconds(15))
                .shouldHave(text("Заказ на выбранную дату невозможен"));
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
