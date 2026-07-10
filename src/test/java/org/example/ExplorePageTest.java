package org.example;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.*;

public class ExplorePageTest {

    @Test
    void explorePage() {
        Configuration.browser = "firefox";
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 15000;
        Configuration.headless = true;

        open("http://localhost:9999");

        // Ждём загрузки React-приложения
        sleep(3000);

        System.out.println("Page title: " + title());

        System.out.println("\n=== ALL INPUTS ===");
        $$("input").forEach(el -> {
            System.out.println("  ID: " + el.getAttribute("id") +
                    ", data-test-id: " + el.getAttribute("data-test-id") +
                    ", placeholder: " + el.getAttribute("placeholder") +
                    ", class: " + el.getAttribute("class"));
        });

        System.out.println("\n=== ALL BUTTONS ===");
        $$("button").forEach(el -> {
            System.out.println("  ID: " + el.getAttribute("id") +
                    ", data-test-id: " + el.getAttribute("data-test-id") +
                    ", text: " + el.getText());
        });

        System.out.println("\n=== ALL ELEMENTS WITH data-test-id ===");
        $$("[data-test-id]").forEach(el -> {
            System.out.println("  data-test-id: " + el.getAttribute("data-test-id") +
                    ", tag: " + el.getTagName() +
                    ", text: " + el.getText());
        });
    }
}