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
        Configuration.browser = "firefox";
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 30000;
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
        // Заполняем город
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.click();
        cityField.clear();
        cityField.setValue(city);
        $(".menu-item").shouldBe(visible, Duration.ofSeconds(5));
        $(".menu-item").click();
        $("body").click();
        sleep(300);

        // Заполняем дату
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.click();
        dateField.clear();
        dateField.setValue(deliveryDate);
        dateField.sendKeys(Keys.ENTER);
        $("body").click();
        sleep(300);

        // Заполняем имя
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.click();
        nameField.clear();
        nameField.setValue(name);
        nameField.sendKeys(Keys.TAB);
        sleep(300);

        // Заполняем телефон
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.click();
        phoneField.clear();
        phoneField.setValue(phone);
        phoneField.sendKeys(Keys.TAB);
        sleep(300);

        // Согласие
        SelenideElement agreementLabel = $("[data-test-id='agreement']");
        agreementLabel.click();
        sleep(300);

        // Кнопка "Запланировать"
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        button.click();

        // Ждем появления уведомления
        SelenideElement notification = $("[data-test-id='success-notification']");
        notification.shouldBe(visible, Duration.ofSeconds(30));

        // Исправленный текст уведомления
        String expectedMessage = "Встреча успешно запланирована на " + deliveryDate;
        notification.$(".notification__content").shouldHave(text(expectedMessage), Duration.ofSeconds(30));
    }

    @Test
    void shouldShowValidationErrorForInvalidCity() {
        // Невалидный город
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.click();
        cityField.clear();
        cityField.setValue("InvalidCity");
        cityField.sendKeys(Keys.ENTER);
        $("body").click();
        sleep(500);

        // Заполняем дату
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.click();
        dateField.clear();
        dateField.setValue(deliveryDate);
        dateField.sendKeys(Keys.ENTER);
        $("body").click();
        sleep(300);

        // Заполняем имя
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.click();
        nameField.clear();
        nameField.setValue(name);
        nameField.sendKeys(Keys.TAB);
        sleep(300);

        // Заполняем телефон
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.click();
        phoneField.clear();
        phoneField.setValue(phone);
        phoneField.sendKeys(Keys.TAB);
        sleep(300);

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
        // Заполняем город
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.click();
        cityField.clear();
        cityField.setValue(city);
        $(".menu-item").shouldBe(visible, Duration.ofSeconds(5));
        $(".menu-item").click();
        $("body").click();
        sleep(300);

        // Заполняем дату
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.click();
        dateField.clear();
        dateField.setValue(deliveryDate);
        dateField.sendKeys(Keys.ENTER);
        $("body").click();
        sleep(300);

        // Невалидное имя (латиница)
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.click();
        nameField.clear();
        nameField.setValue("John Doe");
        nameField.sendKeys(Keys.TAB);
        sleep(300);

        // Заполняем телефон
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.click();
        phoneField.clear();
        phoneField.setValue(phone);
        phoneField.sendKeys(Keys.TAB);
        sleep(300);

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
        // Заполняем город
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.click();
        cityField.clear();
        cityField.setValue(city);
        $(".menu-item").shouldBe(visible, Duration.ofSeconds(5));
        $(".menu-item").click();
        $("body").click();
        sleep(300);

        // Пустая дата
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.click();
        dateField.clear();
        dateField.setValue("");
        $("body").click();
        sleep(500);

        // Заполняем имя
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.click();
        nameField.clear();
        nameField.setValue(name);
        nameField.sendKeys(Keys.TAB);
        sleep(300);

        // Заполняем телефон
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.click();
        phoneField.clear();
        phoneField.setValue(phone);
        phoneField.sendKeys(Keys.TAB);
        sleep(300);

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

        // Заполняем город
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.click();
        cityField.clear();
        cityField.setValue(city);
        $(".menu-item").shouldBe(visible, Duration.ofSeconds(5));
        $(".menu-item").click();
        $("body").click();
        sleep(300);

        // Дата в прошлом
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.click();
        dateField.clear();
        dateField.setValue(pastDateString);
        dateField.sendKeys(Keys.ENTER);
        $("body").click();
        sleep(300);

        // Заполняем имя
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.click();
        nameField.clear();
        nameField.setValue(name);
        nameField.sendKeys(Keys.TAB);
        sleep(300);

        // Заполняем телефон
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.click();
        phoneField.clear();
        phoneField.setValue(phone);
        phoneField.sendKeys(Keys.TAB);
        sleep(300);

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
