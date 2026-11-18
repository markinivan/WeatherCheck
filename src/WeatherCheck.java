import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class WeatherCheck {
        public static void main(String[] args) throws Exception {

            String apiKey = "e529b942-1c0c-463b-9254-9c795a5e2b0d";
            double lat = 72.15;
            double lon = 14.632;
            int limit = 5;

            String url = String.format(
                    Locale.US,
                    "https://api.weather.yandex.ru/v2/forecast?lat=%f&lon=%f&limit=%d&lang=ru_RU",
                    lat, lon, limit
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("X-Yandex-Weather-Key", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                System.err.println("Ошибка " + response.statusCode() + ": " + response.body());
                return;
            }

            System.out.println("Ответ (JSON): " + response.body());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            int currentTemp = root.path("fact").path("temp").asInt();
            System.out.println("Текущая температура: " + currentTemp + "°C");

            JsonNode forecasts = root.path("forecasts");
            int sum = 0;
            int count = 0;

            int days = Math.min(limit, forecasts.size());

            for (int i = 0; i < days; i++) {
                JsonNode dayNode = forecasts.get(i).path("parts").path("day");
                if (dayNode.isMissingNode()) {
                    System.out.println("  (день " + i + ": нет блока parts.day)");
                    continue;
                }
                int tempAvg = dayNode.path("temp_avg").asInt();
                System.out.println("  День " + (i + 1)
                        + " (" + forecasts.get(i).path("date").asText() + "): "
                        + tempAvg + "°C");
                sum += tempAvg;
                count++;
            }

            if (count == 0) {
                System.out.println("Нет данных для расчёта средней температуры.");
            } else {
                double average = (double) sum / count;
                System.out.printf(
                        Locale.US,
                        "Средняя температура за %d дня(дней): %.2f°C%n",
                        count, average
                );
            }
        }
    }