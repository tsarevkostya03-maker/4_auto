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
        Configuration.timeout = 15000;
        Configuration.headless = true;
        
        open("http://localhost:9999");
        
        $("[data-test-id='city']").shouldBe(visible, Duration.ofSeconds(15));
        
        faker = new Faker(new Locale("ru"));
        city = generateCity();
        name = faker.name().firstName() + " " + faker.name().lastName();
        phone = "+7" + faker.number().digits(10);
        deliveryDate = generateDeliveryDate();
    }

    private void fillCity(String cityName) {
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.click();
        cityField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        cityField.setValue(cityName);
        cityField.sendKeys(Keys.ENTER);
        
        // Ждём появления выпадающего списка
        $(".menu-item").shouldBe(visible, Duration.ofSeconds(5));
        $(".menu-item").click();
        
        // Проверяем, что город выбрался
        String selectedCity = cityField.getValue();
        System.out.println("Selected city: " + selectedCity);
        
        // Закрываем всплывающее окно
        $("body").click();
        sleep(500);
    }

    private void fillDate(String date) {
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.click();
        sleep(300);
        
        dateField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        sleep(200);
        
        dateField.setValue(date);
        sleep(200);
        
        dateField.sendKeys(Keys.ENTER);
        sleep(500);
        
        $("body").click();
        sleep(300);
        
        String currentValue = dateField.getValue();
        System.out.println("Current date value: " + currentValue);
    }

    @Test
    void shouldSubmitDeliveryForm() {
        System.out.println("Test date: " + deliveryDate);
        
        // Город
        fillCity(city);
        
        // Дата
        fillDate(deliveryDate);
        
        // Имя
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.click();
        nameField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        nameField.setValue(name);
        
        // Телефон
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.click();
        phoneField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        phoneField.setValue(phone);
        
        // Согласие
        SelenideElement agreementLabel = $("[data-test-id='agreement']");
        agreementLabel.click();
        
        // Проверяем, что все поля заполнены
        System.out.println("City: " + $("[data-test-id='city'] input").getValue());
        System.out.println("Date: " + $("[data-test-id='date'] input").getValue());
        System.out.println("Name: " + $("[data-test-id='name'] input").getValue());
        System.out.println("Phone: " + $("[data-test-id='phone'] input").getValue());
        
        // Кнопка "Запланировать"
        SelenideElement button = $$("button").findBy(text("Запланировать"));
        button.shouldBe(visible, Duration.ofSeconds(5));
        button.click();
        
        // Ждём появления уведомления
        SelenideElement notification = $("[data-test-id='success-notification']");
        notification.shouldBe(visible, Duration.ofSeconds(30));
        
        // Проверяем текст уведомления
        String expectedMessage = "Встреча успешно забронирована на " + deliveryDate;
        notification.$(".notification__content").shouldHave(text(expectedMessage), Duration.ofSeconds(30));
    }

    @Test
    void shouldShowValidationErrorForInvalidCity() {
        // Невалидный город
        SelenideElement cityField = $("[data-test-id='city'] input");
        cityField.click();
        cityField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        cityField.setValue("InvalidCity");
        cityField.sendKeys(Keys.ENTER);
        $("body").click();
        sleep(500);
        
        // Дата
        fillDate(deliveryDate);
        
        // Имя
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.click();
        nameField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        nameField.setValue(name);
        
        // Телефон
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.click();
        phoneField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        phoneField.setValue(phone);
        
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
        fillCity(city);
        
        // Дата
        fillDate(deliveryDate);
        
        // Невалидное имя (латиница)
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.click();
        nameField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        nameField.setValue("John Doe");
        
        // Телефон
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.click();
        phoneField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        phoneField.setValue(phone);
        
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
        fillCity(city);
        
        // Пустая дата
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.click();
        dateField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        dateField.setValue("");
        $("body").click();
        sleep(500);
        
        // Имя
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.click();
        nameField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        nameField.setValue(name);
        
        // Телефон
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.click();
        phoneField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        phoneField.setValue(phone);
        
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
        fillCity(city);
        
        // Дата в прошлом
        fillDate(pastDateString);
        
        // Имя
        SelenideElement nameField = $("[data-test-id='name'] input");
        nameField.click();
        nameField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        nameField.setValue(name);
        
        // Телефон
        SelenideElement phoneField = $("[data-test-id='phone'] input");
        phoneField.click();
        phoneField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        phoneField.setValue(phone);
        
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
