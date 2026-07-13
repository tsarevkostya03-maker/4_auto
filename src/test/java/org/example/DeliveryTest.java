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
        $("input").shouldBe(visible, Duration.ofSeconds(15));
        
        faker = new Faker(new Locale("ru"));
        city = generateCity();
        name = faker.name().firstName() + " " + faker.name().lastName();
        phone = "+7" + faker.number().digits(10);
    }

    @Test
    void shouldSubmitDeliveryForm() {
        // Город — клик через JavaScript
        SelenideElement cityField = $("input[placeholder='Город']");
        executeJavaScript("arguments[0].click();", cityField);
        cityField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        cityField.setValue(city);
        
        // Дата — клик через JavaScript
        String deliveryDate = generateDeliveryDate();
        SelenideElement dateField = $("input[placeholder='Дата встречи']");
        executeJavaScript("arguments[0].click();", dateField);
        dateField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        dateField.setValue(deliveryDate);
        // Закрываем календарь
        dateField.sendKeys(Keys.ENTER);
        
        // Имя — клик через JavaScript
        SelenideElement nameField = $("input[name='name']");
        executeJavaScript("arguments[0].click();", nameField);
        nameField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        nameField.setValue(name);
        
        // Телефон
        SelenideElement phoneField = $("input[placeholder='+7 000 000 00 00']");
        executeJavaScript("arguments[0].click();", phoneField);
        phoneField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        phoneField.setValue(phone);
        
        // Согласие — клик через JavaScript
        SelenideElement agreementCheckbox = $("input[name='agreement']");
        if (!agreementCheckbox.has(checked)) {
            executeJavaScript("arguments[0].click();", agreementCheckbox);
        }
        
        // Кнопка "Запланировать" — клик через JavaScript
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        executeJavaScript("arguments[0].click();", button);
        
        // Ожидаем появления уведомления
        SelenideElement notification = $("[data-test-id='success-notification']");
        notification.shouldBe(visible, Duration.ofSeconds(30));
        notification.shouldHave(text("Успешно"), Duration.ofSeconds(30));
    }

    @Test
    void shouldShowValidationErrorForInvalidCity() {
        // Невалидный город
        SelenideElement cityField = $("input[placeholder='Город']");
        executeJavaScript("arguments[0].click();", cityField);
        cityField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        cityField.setValue("InvalidCity");
        
        // Заполняем остальные поля через JavaScript
        SelenideElement dateField = $("input[placeholder='Дата встречи']");
        executeJavaScript("arguments[0].click();", dateField);
        dateField.setValue(generateDeliveryDate());
        dateField.sendKeys(Keys.ENTER);
        
        SelenideElement nameField = $("input[name='name']");
        executeJavaScript("arguments[0].click();", nameField);
        nameField.setValue(name);
        
        SelenideElement phoneField = $("input[placeholder='+7 000 000 00 00']");
        executeJavaScript("arguments[0].click();", phoneField);
        phoneField.setValue(phone);
        
        SelenideElement agreementCheckbox = $("input[name='agreement']");
        if (!agreementCheckbox.has(checked)) {
            executeJavaScript("arguments[0].click();", agreementCheckbox);
        }
        
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        executeJavaScript("arguments[0].click();", button);
        
        // Проверяем ошибку
        $("[data-test-id='city'] .input__sub")
            .shouldBe(visible, Duration.ofSeconds(15))
            .shouldHave(text("Доставка в выбранный город недоступна"));
    }

    @Test
    void shouldShowValidationErrorForInvalidName() {
        // Заполняем город
        SelenideElement cityField = $("input[placeholder='Город']");
        executeJavaScript("arguments[0].click();", cityField);
        cityField.setValue(city);
        
        SelenideElement dateField = $("input[placeholder='Дата встречи']");
        executeJavaScript("arguments[0].click();", dateField);
        dateField.setValue(generateDeliveryDate());
        dateField.sendKeys(Keys.ENTER);
        
        // Невалидное имя (латиница)
        SelenideElement nameField = $("input[name='name']");
        executeJavaScript("arguments[0].click();", nameField);
        nameField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        nameField.setValue("John Doe");
        
        SelenideElement phoneField = $("input[placeholder='+7 000 000 00 00']");
        executeJavaScript("arguments[0].click();", phoneField);
        phoneField.setValue(phone);
        
        SelenideElement agreementCheckbox = $("input[name='agreement']");
        if (!agreementCheckbox.has(checked)) {
            executeJavaScript("arguments[0].click();", agreementCheckbox);
        }
        
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        executeJavaScript("arguments[0].click();", button);
        
        // Проверяем ошибку
        $("[data-test-id='name'] .input__sub")
            .shouldBe(visible, Duration.ofSeconds(15))
            .shouldHave(text("Имя и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы."));
    }

    @Test
    void shouldShowValidationErrorForEmptyDate() {
        // Заполняем поля
        SelenideElement cityField = $("input[placeholder='Город']");
        executeJavaScript("arguments[0].click();", cityField);
        cityField.setValue(city);
        
        // Пустая дата
        SelenideElement dateField = $("input[placeholder='Дата встречи']");
        executeJavaScript("arguments[0].click();", dateField);
        dateField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        dateField.setValue("");
        
        SelenideElement nameField = $("input[name='name']");
        executeJavaScript("arguments[0].click();", nameField);
        nameField.setValue(name);
        
        SelenideElement phoneField = $("input[placeholder='+7 000 000 00 00']");
        executeJavaScript("arguments[0].click();", phoneField);
        phoneField.setValue(phone);
        
        SelenideElement agreementCheckbox = $("input[name='agreement']");
        if (!agreementCheckbox.has(checked)) {
            executeJavaScript("arguments[0].click();", agreementCheckbox);
        }
        
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        executeJavaScript("arguments[0].click();", button);
        
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
        
        SelenideElement cityField = $("input[placeholder='Город']");
        executeJavaScript("arguments[0].click();", cityField);
        cityField.setValue(city);
        
        SelenideElement dateField = $("input[placeholder='Дата встречи']");
        executeJavaScript("arguments[0].click();", dateField);
        dateField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        dateField.setValue(pastDateString);
        dateField.sendKeys(Keys.ENTER);
        
        SelenideElement nameField = $("input[name='name']");
        executeJavaScript("arguments[0].click();", nameField);
        nameField.setValue(name);
        
        SelenideElement phoneField = $("input[placeholder='+7 000 000 00 00']");
        executeJavaScript("arguments[0].click();", phoneField);
        phoneField.setValue(phone);
        
        SelenideElement agreementCheckbox = $("input[name='agreement']");
        if (!agreementCheckbox.has(checked)) {
            executeJavaScript("arguments[0].click();", agreementCheckbox);
        }
        
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        executeJavaScript("arguments[0].click();", button);
        
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
