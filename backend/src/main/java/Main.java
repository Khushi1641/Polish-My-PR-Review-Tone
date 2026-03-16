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
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    private static final int DEFAULT_PORT = 3000;
    private static final String DEFAULT_MODEL = "gemini-2.5-flash";
    private static final int MAX_COMMENT_LENGTH = 5000;

    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        int port = getEnvInt("PORT", DEFAULT_PORT);
        String model = getEnvString("GEMINI_MODEL", DEFAULT_MODEL);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/polish", new PolishHandler(model));
        server.createContext("/api/health", new HealthHandler());

        server.setExecutor(Executors.newFixedThreadPool(10));

        // Drain in-flight requests on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down server…");
            server.stop(5);
        }));

        server.start();
        LOG.info("Polish server running at http://localhost:" + port + " (model=" + model + ")");
    }

    // ---- Handlers ---------------------------------------------------------------

    /**
     * GET /health — simple liveness / readiness probe.
     */
    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }
            JsonObject body = new JsonObject();
            body.addProperty("status", "ok");
            sendJsonResponse(exchange, 200, body);
        }
    }

    /**
     * POST /api/polish — accepts { "comment": "…" }, returns { "suggestion": "…" }.
     */
    static class PolishHandler implements HttpHandler {
        private final Client client;
        private final String model;

        public PolishHandler(String model) {
            this.client = new Client();
            this.model = model;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // --- CORS ---
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
                // --- Parse body ---
                JsonObject requestBody;
                try (InputStreamReader reader =
                             new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
                    requestBody = JsonParser.parseReader(reader).getAsJsonObject();
                } catch (JsonSyntaxException | IllegalStateException e) {
                    sendError(exchange, 400, "Invalid JSON format");
                    return;
                }

                if (!requestBody.has("comment")) {
                    sendError(exchange, 400, "Missing 'comment' field in request body");
                    return;
                }

                String comment = requestBody.get("comment").getAsString().trim();

                // --- Validate input ---
                if (comment.isEmpty()) {
                    sendError(exchange, 400, "Comment must not be empty");
                    return;
                }
                if (comment.length() > MAX_COMMENT_LENGTH) {
                    sendError(exchange, 400,
                            "Comment exceeds maximum length of " + MAX_COMMENT_LENGTH + " characters");
                    return;
                }

                // --- Call Gemini ---
                GenerateContentConfig config = GenerateContentConfig.builder()
                        .systemInstruction(Content.fromParts(Part.fromText(
                                "You are an emotionally intelligent code reviewer. "
                                        + "Polish the PR comment given inside <original_comment> tags "
                                        + "to sound friendly and constructive, while keeping the "
                                        + "technical feedback intact. Return no more than 3 options."
                        )))
                        .temperature(0.7f)
                        .build();

                String promptText =
                        "<original_comment>" + comment + "</original_comment>\nPolished:";

                GenerateContentResponse response = client.models.generateContent(
                        model, promptText, config);

                String suggestion = response.text();
                String cleanSuggestion = suggestion != null ? suggestion.trim() : "";
                cleanSuggestion = cleanSuggestion.replaceAll("^[\"']+|[\"']+$", "").trim();

                // --- Respond ---
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("suggestion", cleanSuggestion);
                sendJsonResponse(exchange, 200, jsonResponse);

            } catch (Exception e) {
                LOG.log(Level.SEVERE, "API Error", e);
                sendError(exchange, 500, "Polish failed");
            }
        }
    }

    // ---- Helpers -----------------------------------------------------------------

    /**
     * Serialises a {@link JsonObject} and writes it as the HTTP response body.
     */
    private static void sendJsonResponse(HttpExchange exchange, int statusCode, JsonObject body)
            throws IOException {
        byte[] bytes = gson.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendError(HttpExchange exchange, int statusCode, String message)
            throws IOException {
        JsonObject errorObj = new JsonObject();
        errorObj.addProperty("error", message);
        sendJsonResponse(exchange, statusCode, errorObj);
    }

    private static int getEnvInt(String name, int defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOG.warning("Invalid integer for env " + name + "=\"" + value + "\", using default " + defaultValue);
            return defaultValue;
        }
    }

    private static String getEnvString(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}