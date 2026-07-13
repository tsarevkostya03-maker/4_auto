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
        Configuration.browser = "firefox";
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 15000;
        Configuration.headless = true;
        
        open("http://localhost:9999");
        
        // Ждём загрузки страницы
        $("[data-test-id='city']").shouldBe(visible, Duration.ofSeconds(15));
        
        faker = new Faker(new Locale("ru"));
        city = generateCity();
        name = faker.name().firstName() + " " + faker.name().lastName();
        phone = "+7" + faker.number().digits(10);
    }

    @Test
    void shouldSubmitDeliveryForm() {
        // Город
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.click();
        cityField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        cityField.setValue(city);
        cityField.sendKeys(Keys.ENTER);
        $("body").click();
        
        // Дата
        String deliveryDate = generateDeliveryDate();
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.click();
        dateField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        dateField.setValue(deliveryDate);
        dateField.sendKeys(Keys.ENTER);
        $("body").click();
        
        // Имя
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.click();
        nameField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        nameField.setValue(name);
        
        // Телефон
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.click();
        phoneField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        phoneField.setValue(phone);
        
        // Согласие
        SelenideElement agreementLabel = $("[data-test-id='agreement']");
        agreementLabel.click();
        
        // Кнопка "Запланировать" — ищем по тексту
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        button.click();
        
        // Проверяем уведомление об успехе
        SelenideElement notification = $("[data-test-id='success-notification']");
        notification.shouldBe(visible, Duration.ofSeconds(30));
        
        // Проверяем текст уведомления с датой
        String expectedMessage = "Встреча успешно забронирована на " + deliveryDate;
        notification.$(".notification__content").shouldHave(text(expectedMessage), Duration.ofSeconds(30));
    }

    @Test
    void shouldShowValidationErrorForInvalidCity() {
        // Невалидный город
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.click();
        cityField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        cityField.setValue("InvalidCity");
        cityField.sendKeys(Keys.ENTER);
        $("body").click();
        
        // Заполняем остальные поля
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.click();
        dateField.setValue(generateDeliveryDate());
        dateField.sendKeys(Keys.ENTER);
        $("body").click();
        
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.click();
        nameField.setValue(name);
        
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.click();
        phoneField.setValue(phone);
        
        SelenideElement agreementLabel = $("[data-test-id='agreement']");
        agreementLabel.click();
        
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        button.click();
        
        // Проверяем ошибку
        $("[data-test-id='city'] .input__sub")
            .shouldBe(visible, Duration.ofSeconds(15))
            .shouldHave(text("Доставка в выбранный город недоступна"));
    }

    @Test
    void shouldShowValidationErrorForInvalidName() {
        // Заполняем город
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.click();
        cityField.setValue(city);
        cityField.sendKeys(Keys.ENTER);
        $("body").click();
        
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.click();
        dateField.setValue(generateDeliveryDate());
        dateField.sendKeys(Keys.ENTER);
        $("body").click();
        
        // Невалидное имя (латиница)
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.click();
        nameField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        nameField.setValue("John Doe");
        
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.click();
        phoneField.setValue(phone);
        
        SelenideElement agreementLabel = $("[data-test-id='agreement']");
        agreementLabel.click();
        
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        button.click();
        
        // Проверяем ошибку
        $("[data-test-id='name'] .input__sub")
            .shouldBe(visible, Duration.ofSeconds(15))
            .shouldHave(text("Имя и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы."));
    }

    @Test
    void shouldShowValidationErrorForEmptyDate() {
        // Заполняем поля
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.click();
        cityField.setValue(city);
        cityField.sendKeys(Keys.ENTER);
        $("body").click();
        
        // Пустая дата
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.click();
        dateField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        dateField.setValue("");
        $("body").click();
        
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.click();
        nameField.setValue(name);
        
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.click();
        phoneField.setValue(phone);
        
        SelenideElement agreementLabel = $("[data-test-id='agreement']");
        agreementLabel.click();
        
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        button.click();
        
        // Проверяем ошибку
        $("[data-test-id='date'] .input__sub")
            .shouldBe(visible, Duration.ofSeconds(15))
            .shouldHave(text("Неверно введена дата"));
    }

    @Test
    void shouldShowValidationErrorForPastDate() {
        // Дата в прошлом
        LocalDate pastDate = LocalDate.now().minusDays(1);
        String pastDateString = pastDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.click();
        cityField.setValue(city);
        cityField.sendKeys(Keys.ENTER);
        $("body").click();
        
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.click();
        dateField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        dateField.setValue(pastDateString);
        dateField.sendKeys(Keys.ENTER);
        $("body").click();
        
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.click();
        nameField.setValue(name);
        
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.click();
        phoneField.setValue(phone);
        
        SelenideElement agreementLabel = $("[data-test-id='agreement']");
        agreementLabel.click();
        
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        button.click();
        
        // Проверяем ошибку
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
