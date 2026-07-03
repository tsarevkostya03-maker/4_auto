import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MockServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(9999), 0);

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
                String html = "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <title>Доставка карты</title>\n" +
                        "    <style>\n" +
                        "        body { font-family: Arial; padding: 40px; max-width: 600px; margin: 0 auto; }\n" +
                        "        .form-group { margin: 15px 0; }\n" +
                        "        label { display: inline-block; width: 150px; font-weight: bold; }\n" +
                        "        input { padding: 8px; width: 250px; border: 1px solid #ccc; border-radius: 4px; }\n" +
                        "        .input__sub { color: red; font-size: 12px; margin-top: 5px; min-height: 20px; }\n" +
                        "        .button { padding: 10px 30px; background: #4CAF50; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; }\n" +
                        "        .button_loading { background: #ff9800; opacity: 0.7; }\n" +
                        "        .button_loading::after { content: \" ⏳\"; }\n" +
                        "        .notification { display: none; margin-top: 20px; padding: 15px; background: #d4edda; border: 1px solid #c3e6cb; border-radius: 4px; }\n" +
                        "        .notification_visible { display: block; }\n" +
                        "        .notification__content { color: #155724; }\n" +
                        "    </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <h1>Заказ доставки карты</h1>\n" +
                        "    <form id=\"deliveryForm\">\n" +
                        "        <div class=\"form-group\" data-test-id=\"city\">\n" +
                        "            <label>Город</label>\n" +
                        "            <input type=\"text\" id=\"city\" value=\"Москва\" placeholder=\"Введите город\">\n" +
                        "            <div class=\"input__sub\" id=\"cityError\"></div>\n" +
                        "        </div>\n" +
                        "        <div class=\"form-group\" data-test-id=\"date\">\n" +
                        "            <label>Дата</label>\n" +
                        "            <input type=\"text\" id=\"date\" value=\"15.07.2026\" placeholder=\"ДД.ММ.ГГГГ\">\n" +
                        "            <div class=\"input__sub\" id=\"dateError\"></div>\n" +
                        "        </div>\n" +
                        "        <div class=\"form-group\" data-test-id=\"name\">\n" +
                        "            <label>Фамилия и имя</label>\n" +
                        "            <input type=\"text\" id=\"name\" value=\"Иванов Иван\" placeholder=\"Иванов Иван\">\n" +
                        "            <div class=\"input__sub\" id=\"nameError\"></div>\n" +
                        "        </div>\n" +
                        "        <div class=\"form-group\" data-test-id=\"phone\">\n" +
                        "            <label>Телефон</label>\n" +
                        "            <input type=\"text\" id=\"phone\" value=\"+79991234567\" placeholder=\"+79991234567\">\n" +
                        "            <div class=\"input__sub\" id=\"phoneError\"></div>\n" +
                        "        </div>\n" +
                        "        <div class=\"form-group\" data-test-id=\"agreement\">\n" +
                        "            <div style=\"margin-left: 150px;\">\n" +
                        "                <input type=\"checkbox\" id=\"agreement\" checked>\n" +
                        "                <label style=\"width: auto; font-weight: normal;\">Согласие на обработку данных</label>\n" +
                        "            </div>\n" +
                        "        </div>\n" +
                        "        <div style=\"margin-top: 20px; margin-left: 150px;\">\n" +
                        "            <button type=\"button\" class=\"button\" id=\"submitBtn\" data-test-id=\"order\">Забронировать</button>\n" +
                        "        </div>\n" +
                        "    </form>\n" +
                        "    <div class=\"notification\" data-test-id=\"success-notification\" id=\"successNotification\">\n" +
                        "        <div class=\"notification__content\" id=\"successMessage\">Встреча успешно забронирована</div>\n" +
                        "    </div>\n" +
                        "</body>\n" +
                        "<script>\n" +
                        "    document.getElementById('submitBtn').addEventListener('click', function() {\n" +
                        "        document.querySelectorAll('.input__sub').forEach(el => el.textContent = '');\n" +
                        "        const city = document.getElementById('city').value;\n" +
                        "        const cities = ['Москва','Санкт-Петербург','Новосибирск','Екатеринбург','Казань','Нижний Новгород','Челябинск','Омск','Самара','Ростов-на-Дону','Уфа','Красноярск','Воронеж','Пермь','Волгоград','Краснодар'];\n" +
                        "        if (!cities.includes(city)) {\n" +
                        "            document.getElementById('cityError').textContent = 'Доставка в выбранный город недоступна';\n" +
                        "            return;\n" +
                        "        }\n" +
                        "        const date = document.getElementById('date').value;\n" +
                        "        if (!date.match(/^\\d{2}\\.\\d{2}\\.\\d{4}$/)) {\n" +
                        "            document.getElementById('dateError').textContent = 'Неверно введена дата';\n" +
                        "            return;\n" +
                        "        }\n" +
                        "        // Проверка: дата должна быть не ранее чем через 3 дня\n" +
                        "        const parts = date.split('.');\n" +
                        "        const inputDate = new Date(parts[2], parts[1] - 1, parts[0]);\n" +
                        "        const today = new Date();\n" +
                        "        const minDate = new Date(today);\n" +
                        "        minDate.setDate(today.getDate() + 3);\n" +
                        "        if (inputDate < minDate) {\n" +
                        "            document.getElementById('dateError').textContent = 'Дата должна быть не ранее трёх дней с текущей даты';\n" +
                        "            return;\n" +
                        "        }\n" +
                        "        const name = document.getElementById('name').value;\n" +
                        "        if (!/^[А-Яа-яЁё\\s-]+$/.test(name)) {\n" +
                        "            document.getElementById('nameError').textContent = 'Имя и Фамилия должны содержать только русские буквы, дефисы и пробелы';\n" +
                        "            return;\n" +
                        "        }\n" +
                        "        const phone = document.getElementById('phone').value;\n" +
                        "        if (!/^\\+7\\d{10}$/.test(phone)) {\n" +
                        "            document.getElementById('phoneError').textContent = 'Телефон должен содержать 11 цифр и начинаться с +';\n" +
                        "            return;\n" +
                        "        }\n" +
                        "        if (!document.getElementById('agreement').checked) {\n" +
                        "            alert('Необходимо согласие на обработку данных');\n" +
                        "            return;\n" +
                        "        }\n" +
                        "        const btn = document.getElementById('submitBtn');\n" +
                        "        btn.classList.add('button_loading');\n" +
                        "        btn.disabled = true;\n" +
                        "        setTimeout(() => {\n" +
                        "            document.getElementById('successNotification').style.display = 'block';\n" +
                        "            document.getElementById('successMessage').textContent = 'Встреча успешно забронирована на ' + date;\n" +
                        "            btn.classList.remove('button_loading');\n" +
                        "            btn.disabled = false;\n" +
                        "        }, 3000);\n" +
                        "    });\n" +
                        "</script>\n" +
                        "</html>";

                byte[] response = html.getBytes(StandardCharsets.UTF_8);
                Headers headers = exchange.getResponseHeaders();
                headers.set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.length);
                OutputStream os = exchange.getResponseBody();
                os.write(response);
                os.close();
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("✅ Mock server started on http://localhost:9999");
        System.out.println("📝 Press Ctrl+C to stop");
    }
}