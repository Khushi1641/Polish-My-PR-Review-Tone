import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class Main {

    private static final int PORT = 3000;
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/polish", new PolishHandler());

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("✅ Polish server running at http://localhost:" + PORT);
    }

    static class PolishHandler implements HttpHandler {
        private final Client client;

        public PolishHandler() {
            this.client = new Client();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            try {
                JsonObject requestBody;
                try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
                    requestBody = JsonParser.parseReader(reader).getAsJsonObject();
                } catch (JsonSyntaxException | IllegalStateException e) {
                    sendError(exchange, 400, "Invalid JSON format");
                    return;
                }

                if (!requestBody.has("comment")) {
                    sendError(exchange, 400, "Missing 'comment' field in request body");
                    return;
                }

                String comment = requestBody.get("comment").getAsString();

                GenerateContentConfig config = GenerateContentConfig.builder()
                        .systemInstruction(Content.fromParts(Part.fromText(
                                "You are an emotionally intelligent code reviewer. " +
                                        "Polish PR comments to sound friendly and constructive, " +
                                        "while keeping the technical feedback intact."
                        )))
                        .temperature(0.7f)
                        .build();

                String promptText = "Original: \"" + comment + "\"\nPolished:";

                GenerateContentResponse response = client.models.generateContent(
                        "gemini-2.5-flash",
                        promptText,
                        config
                );

                String suggestion = response.text();
                String cleanSuggestion = suggestion != null ? suggestion.trim() : "";

                cleanSuggestion = cleanSuggestion.replaceAll("^[\"']+|[\"']+$", "").trim();

                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("suggestion", cleanSuggestion);
                String responseString = gson.toJson(jsonResponse);

                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(200, responseString.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseString.getBytes(StandardCharsets.UTF_8));
                }

            } catch (Exception e) {
                System.err.println("🔥 API Error: " + e.getMessage());
                e.printStackTrace();
                sendError(exchange, 500, "Polish failed");
            }
        }

        private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
            JsonObject errorObj = new JsonObject();
            errorObj.addProperty("error", message);
            String responseString = gson.toJson(errorObj);

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, responseString.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseString.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}