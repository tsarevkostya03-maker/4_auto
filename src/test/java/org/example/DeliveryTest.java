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
    private LocalDate deliveryLocalDate;

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
        deliveryLocalDate = generateDeliveryDate();
        deliveryDate = deliveryLocalDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
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
        
        // Закрываем всплывающее окно
        $("body").click();
        sleep(300);
    }

    private void fillDateViaCalendar(LocalDate date) {
        SelenideElement dateField = $("[data-test-id='date'] input");
        dateField.click();
        
        // Получаем месяц и год для выбора
        int targetMonth = date.getMonthValue();
        int targetYear = date.getYear();
        int targetDay = date.getDayOfMonth();
        
        // Кликаем по полю даты чтобы открыть календарь
        dateField.click();
        
        // Ждём пока календарь откроется
        $(".calendar").shouldBe(visible, Duration.ofSeconds(5));
        
        // Получаем текущий месяц и год в календаре
        String currentMonthYear = $(".calendar__title").getText();
        
        // Навигация к нужному месяцу (если нужно)
        while (true) {
            String[] parts = currentMonthYear.split(" ");
            String monthName = parts[0];
            int year = Integer.parseInt(parts[1]);
            
            // Проверяем, совпадает ли текущий месяц и год с целевыми
            if (year == targetYear && getMonthNumber(monthName) == targetMonth) {
                break;
            }
            
            // Если целевой месяц позже текущего - листаем вперёд
            if (year < targetYear || (year == targetYear && getMonthNumber(monthName) < targetMonth)) {
                $("[data-test-id='date'] .calendar__next").click();
            } else {
                $("[data-test-id='date'] .calendar__prev").click();
            }
            sleep(200);
            currentMonthYear = $(".calendar__title").getText();
        }
        
        // Выбираем нужный день
        $$(".calendar__day").findBy(text(String.valueOf(targetDay))).click();
        sleep(300);
    }
    
    private int getMonthNumber(String monthName) {
        switch (monthName) {
            case "Январь": return 1;
            case "Февраль": return 2;
            case "Март": return 3;
            case "Апрель": return 4;
            case "Май": return 5;
            case "Июнь": return 6;
            case "Июль": return 7;
            case "Август": return 8;
            case "Сентябрь": return 9;
            case "Октябрь": return 10;
            case "Ноябрь": return 11;
            case "Декабрь": return 12;
            default: return 0;
        }
    }

    @Test
    void shouldSubmitDeliveryForm() {
        // Город
        fillCity(city);
        
        // Дата через календарь
        fillDateViaCalendar(deliveryLocalDate);
        
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
        sleep(300);
        
        // Дата через календарь
        fillDateViaCalendar(deliveryLocalDate);
        
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
        
        // Дата через календарь
        fillDateViaCalendar(deliveryLocalDate);
        
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
        sleep(300);
        
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
        
        // Город
        fillCity(city);
        
        // Дата в прошлом через календарь
        fillDateViaCalendar(pastDate);
        
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

    private LocalDate generateDeliveryDate() {
        LocalDate currentDate = LocalDate.now();
        return currentDate.plusDays(faker.number().numberBetween(3, 30));
    }
}
