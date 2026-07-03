package org.example;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.chrome.ChromeOptions;

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
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 15000;
        Configuration.headless = false;

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        Configuration.browserCapabilities = options;

        open("http://localhost:9999");
        $("#city").shouldBe(visible);

        faker = new Faker(new Locale("ru"));
        city = generateCity();
        name = faker.name().firstName() + " " + faker.name().lastName();
        phone = "+7" + faker.number().digits(10);
    }

    @Test
    void shouldSubmitDeliveryForm() {
        SelenideElement cityField = $("#city");
        cityField.click();
        cityField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        cityField.setValue(city);

        String deliveryDate = generateDeliveryDate();
        SelenideElement dateField = $("#date");
        dateField.click();
        dateField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        dateField.setValue(deliveryDate);

        SelenideElement nameField = $("#name");
        nameField.click();
        nameField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        nameField.setValue(name);

        SelenideElement phoneField = $("#phone");
        phoneField.click();
        phoneField.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE);
        phoneField.setValue(phone);

        SelenideElement agreementCheckbox = $("#agreement");
        if (!agreementCheckbox.isSelected()) {
            agreementCheckbox.click();
        }

        $("#submitBtn").click();

        $("#submitBtn.button_loading").shouldBe(visible, Duration.ofSeconds(15));
        $("#submitBtn.button_loading").shouldBe(disappear, Duration.ofSeconds(15));

        $("#successNotification").shouldBe(visible, Duration.ofSeconds(15));
        $("#successMessage").shouldHave(text("Встреча успешно забронирована"), Duration.ofSeconds(15));
    }

    @Test
    void shouldShowValidationErrorForInvalidCity() {
        $("#city").setValue("InvalidCity");
        $("#date").setValue(generateDeliveryDate());
        $("#name").setValue(name);
        $("#phone").setValue(phone);
        if (!$("#agreement").isSelected()) {
            $("#agreement").click();
        }
        $("#submitBtn").click();

        $("#cityError").shouldBe(visible, Duration.ofSeconds(15));
        $("#cityError").shouldHave(text("Доставка в выбранный город недоступна"));
    }

    @Test
    void shouldShowValidationErrorForInvalidName() {
        $("#city").setValue(city);
        $("#date").setValue(generateDeliveryDate());
        $("#name").setValue("John Doe");
        $("#phone").setValue(phone);
        if (!$("#agreement").isSelected()) {
            $("#agreement").click();
        }
        $("#submitBtn").click();

        $("#nameError").shouldBe(visible, Duration.ofSeconds(15));
        $("#nameError").shouldHave(text("Имя и Фамилия должны содержать только русские буквы, дефисы и пробелы"));
    }

    @Test
    void shouldShowValidationErrorForInvalidPhone() {
        $("#city").setValue(city);
        $("#date").setValue(generateDeliveryDate());
        $("#name").setValue(name);
        $("#phone").setValue("8912345678");
        if (!$("#agreement").isSelected()) {
            $("#agreement").click();
        }
        $("#submitBtn").click();

        $("#phoneError").shouldBe(visible, Duration.ofSeconds(15));
        // Проверяем, что текст содержит ключевые слова
        $("#phoneError").shouldHave(text("11 цифр"));
    }

    @Test
    void shouldShowValidationErrorForEmptyDate() {
        $("#city").setValue(city);
        $("#date").setValue("");
        $("#name").setValue(name);
        $("#phone").setValue(phone);
        if (!$("#agreement").isSelected()) {
            $("#agreement").click();
        }
        $("#submitBtn").click();

        $("#dateError").shouldBe(visible, Duration.ofSeconds(15));
        $("#dateError").shouldHave(text("Неверно введена дата"));
    }

    @Test
    void shouldShowValidationErrorForPastDate() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        String pastDateString = pastDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        $("#city").setValue(city);
        $("#date").setValue(pastDateString);
        $("#name").setValue(name);
        $("#phone").setValue(phone);
        if (!$("#agreement").isSelected()) {
            $("#agreement").click();
        }
        $("#submitBtn").click();

        // Ожидаем появления ошибки
        $("#dateError").shouldBe(visible, Duration.ofSeconds(15));
        // Проверяем, что текст содержит ключевые слова
        $("#dateError").shouldHave(text("трёх дней"));
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