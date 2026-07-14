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
    private String deliveryDate; // Сохраняем дату для всех тестов

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
        deliveryDate = generateDeliveryDate(); // Генерируем дату один раз
    }

    @Test
    void shouldSubmitDeliveryForm() {
        // === ГОРОД ===
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.click();
        // ВНИМАНИЕ: на Linux используем Keys.CONTROL, на Mac - Keys.COMMAND
        cityField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        cityField.setValue(city);
        cityField.sendKeys(Keys.ENTER);
        
        // Ждём появления выпадающего списка и выбираем первый вариант
        $(".menu-item").shouldBe(visible, Duration.ofSeconds(5));
        $(".menu-item").click(); // Обязательно кликаем по варианту из списка!
        
        // === ДАТА ===
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.click();
        dateField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        dateField.setValue(deliveryDate);
        dateField.sendKeys(Keys.ENTER);
        
        // Небольшая пауза, чтобы дата успела примениться
        sleep(500);
        
        // === ИМЯ ===
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.click();
        nameField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        nameField.setValue(name);
        
        // === ТЕЛЕФОН ===
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.click();
        phoneField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        phoneField.setValue(phone);
        
        // === СОГЛАСИЕ ===
        SelenideElement agreementLabel = $("[data-test-id='agreement']");
        agreementLabel.click();
        
        // === КНОПКА "ЗАПЛАНИРОВАТЬ" ===
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        button.click();
        
        // === ПРОВЕРКА УВЕДОМЛЕНИЯ ===
        SelenideElement notification = $("[data-test-id='success-notification']");
        notification.shouldBe(visible, Duration.ofSeconds(30));
        
        // Проверяем текст уведомления
        String expectedMessage = "Встреча успешно забронирована на " + deliveryDate;
        notification.$(".notification__content").shouldHave(text(expectedMessage), Duration.ofSeconds(30));
    }

    @Test
    void shouldShowValidationErrorForInvalidCity() {
        // === НЕВАЛИДНЫЙ ГОРОД ===
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.click();
        cityField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        cityField.setValue("InvalidCity");
        cityField.sendKeys(Keys.ENTER);
        sleep(500);
        
        // === ДАТА ===
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.click();
        dateField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        dateField.setValue(deliveryDate);
        dateField.sendKeys(Keys.ENTER);
        sleep(500);
        
        // === ИМЯ ===
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.click();
        nameField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        nameField.setValue(name);
        
        // === ТЕЛЕФОН ===
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.click();
        phoneField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        phoneField.setValue(phone);
        
        // === СОГЛАСИЕ ===
        SelenideElement agreementLabel = $("[data-test-id='agreement']");
        agreementLabel.click();
        
        // === КНОПКА ===
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        button.click();
        
        // === ПРОВЕРКА ОШИБКИ ===
        $("[data-test-id='city'] .input__sub")
            .shouldBe(visible, Duration.ofSeconds(15))
            .shouldHave(text("Доставка в выбранный город недоступна"));
    }

    @Test
    void shouldShowValidationErrorForInvalidName() {
        // === ГОРОД ===
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.click();
        cityField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        cityField.setValue(city);
        cityField.sendKeys(Keys.ENTER);
        $(".menu-item").shouldBe(visible, Duration.ofSeconds(5));
        $(".menu-item").click();
        
        // === ДАТА ===
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.click();
        dateField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        dateField.setValue(deliveryDate);
        dateField.sendKeys(Keys.ENTER);
        sleep(500);
        
        // === НЕВАЛИДНОЕ ИМЯ (латиница) ===
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.click();
        nameField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        nameField.setValue("John Doe");
        
        // === ТЕЛЕФОН ===
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.click();
        phoneField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        phoneField.setValue(phone);
        
        // === СОГЛАСИЕ ===
        SelenideElement agreementLabel = $("[data-test-id='agreement']");
        agreementLabel.click();
        
        // === КНОПКА ===
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        button.click();
        
        // === ПРОВЕРКА ОШИБКИ ===
        $("[data-test-id='name'] .input__sub")
            .shouldBe(visible, Duration.ofSeconds(15))
            .shouldHave(text("Имя и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы."));
    }

    @Test
    void shouldShowValidationErrorForEmptyDate() {
        // === ГОРОД ===
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.click();
        cityField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        cityField.setValue(city);
        cityField.sendKeys(Keys.ENTER);
        $(".menu-item").shouldBe(visible, Duration.ofSeconds(5));
        $(".menu-item").click();
        
        // === ПУСТАЯ ДАТА ===
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.click();
        dateField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        dateField.setValue("");
        $("body").click();
        
        // === ИМЯ ===
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.click();
        nameField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        nameField.setValue(name);
        
        // === ТЕЛЕФОН ===
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.click();
        phoneField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        phoneField.setValue(phone);
        
        // === СОГЛАСИЕ ===
        SelenideElement agreementLabel = $("[data-test-id='agreement']");
        agreementLabel.click();
        
        // === КНОПКА ===
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        button.click();
        
        // === ПРОВЕРКА ОШИБКИ ===
        $("[data-test-id='date'] .input__sub")
            .shouldBe(visible, Duration.ofSeconds(15))
            .shouldHave(text("Неверно введена дата"));
    }

    @Test
    void shouldShowValidationErrorForPastDate() {
        // === ДАТА В ПРОШЛОМ ===
        LocalDate pastDate = LocalDate.now().minusDays(1);
        String pastDateString = pastDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        
        // === ГОРОД ===
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.click();
        cityField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        cityField.setValue(city);
        cityField.sendKeys(Keys.ENTER);
        $(".menu-item").shouldBe(visible, Duration.ofSeconds(5));
        $(".menu-item").click();
        
        // === ДАТА В ПРОШЛОМ ===
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.click();
        dateField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        dateField.setValue(pastDateString);
        dateField.sendKeys(Keys.ENTER);
        sleep(500);
        
        // === ИМЯ ===
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.click();
        nameField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        nameField.setValue(name);
        
        // === ТЕЛЕФОН ===
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.click();
        phoneField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        phoneField.setValue(phone);
        
        // === СОГЛАСИЕ ===
        SelenideElement agreementLabel = $("[data-test-id='agreement']");
        agreementLabel.click();
        
        // === КНОПКА ===
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        button.click();
        
        // === ПРОВЕРКА ОШИБКИ ===
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
