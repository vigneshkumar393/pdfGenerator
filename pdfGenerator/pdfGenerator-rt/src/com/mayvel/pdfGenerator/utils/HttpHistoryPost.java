package com.mayvel.pdfGenerator.utils;

import com.mayvel.pdfGenerator.Const.Consts;
import com.mayvel.pdfGenerator.component.HistoryDBHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.tridium.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpHistoryPost {
    private static HttpServer server;

    /**
     * Starts the HTTP history server
     */
    public static void start() throws IOException {
        if (server != null) {
            System.out.println("History server already started.");
            return;
        }

        server = HttpServer.create(new InetSocketAddress(Consts.HistoryPort), 0);
        server.createContext(Consts.history_sync_names_route, new HistoryHandler());
        server.createContext(Consts.history_generate_pdf_route, new GeneratePdf());
        server.createContext(Consts.get_all_pdfs_name_list, new GetAllPdfNameList());
        server.createContext(Consts.delete_pdf_by_name, new DeletePdfByName());
        server.setExecutor(null);
        server.start();
        System.out.println("HTTP History Server started on port " + Consts.HistoryPort);
    }

    /**
     * Stops the HTTP history server
     */
    public static void stop() {
        if (server != null) {
            server.stop(0);  // Immediate stop
            server = null;
            System.out.println("HTTP History Server stopped.");
        }
    }

    static class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
            t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equalsIgnoreCase(t.getRequestMethod())) {
                t.sendResponseHeaders(204, -1);
                return;
            }

            if (!"GET".equalsIgnoreCase(t.getRequestMethod())) {
                t.sendResponseHeaders(405, -1);
                return;
            }

            String response = HistoryDBHelper.getAllHistoryNames();
            t.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
            t.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }

        private static byte[] readFully(InputStream input) throws IOException {
            byte[] buffer = new byte[8192];
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            return output.toByteArray();
        }

        private static Map<String, String> parseQuery(String query) {
            Map<String, String> result = new HashMap<>();
            if (query == null || query.isEmpty()) {
                return result;
            }

            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    result.put(entry[0], entry[1]);
                } else {
                    result.put(entry[0], "");
                }
            }
            return result;
        }
    }

    static class GeneratePdf implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equalsIgnoreCase(t.getRequestMethod())) {
                t.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(t.getRequestMethod())) {
                t.sendResponseHeaders(405, -1);
                return;
            }

            byte[] requestBody = readFully(t.getRequestBody());
            String body = new String(requestBody, StandardCharsets.UTF_8);

            JSONObject json = new JSONObject(body);
            String data = json.optString("data");

            String[] historyNames = data.split(",");
            for (int i = 0; i < historyNames.length; i++) {
                historyNames[i] = historyNames[i].trim();
            }

            JSONObject result = HistoryDBHelper.generatePdf(historyNames);

            String response = result.toString();
            t.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
            t.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }

        private static byte[] readFully(InputStream input) throws IOException {
            byte[] buffer = new byte[8192];
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            return output.toByteArray();
        }
    }

    static class GetAllPdfNameList implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().add("Access-Control-Allow-Methods", "GET");
            t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if (!"GET".equalsIgnoreCase(t.getRequestMethod())) {
                t.sendResponseHeaders(405, -1);
                return;
            }

            // Parse query parameters
            Map<String, String> queryParams = parseQuery(t.getRequestURI().getQuery());
            int limit = parseIntOrDefault(queryParams.get("limit"), 10);
            int offset = parseIntOrDefault(queryParams.get("offset"), 0);
            String filter = queryParams.get("filter");

            JSONObject result = HistoryDBHelper.getAllPdfFileNames(limit, offset, filter);
            String response = result.toString();

            t.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
            t.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = t.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }

        private static Map<String, String> parseQuery(String query) {
            Map<String, String> result = new HashMap<>();
            if (query == null || query.isEmpty()) {
                return result;
            }

            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    result.put(entry[0], entry[1]);
                } else {
                    result.put(entry[0], "");
                }
            }
            return result;
        }

        private static int parseIntOrDefault(String value, int defaultVal) {
            try {
                return Integer.parseInt(value);
            } catch (Exception e) {
                return defaultVal;
            }
        }
    }
    static class DeletePdfByName implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "DELETE, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            // Handle CORS preflight
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1); // No content
                return;
            }

            // Only allow DELETE
            if (!"DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }

            Map<String, String> queryParams = parseQuery(exchange.getRequestURI().getQuery());
            String fileName = queryParams.get("fileName");

            JSONObject responseJson = new JSONObject();

            if (fileName == null || fileName.trim().isEmpty()) {
                responseJson.put("success", false);
                responseJson.put("message", "Missing 'fileName' parameter.");
            } else {
                boolean deleted = HistoryDBHelper.deletePdfByName(fileName);
                responseJson.put("success", deleted);
                responseJson.put("message", deleted ? "PDF deleted successfully." : "PDF not found or couldn't be deleted.");
            }

            String response = responseJson.toString();
            exchange.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }

        private static Map<String, String> parseQuery(String query) {
            Map<String, String> result = new HashMap<>();
            if (query == null || query.isEmpty()) {
                return result;
            }

            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    result.put(entry[0], entry[1]);
                } else {
                    result.put(entry[0], "");
                }
            }
            return result;
        }

        private static int parseIntOrDefault(String value, int defaultVal) {
            try {
                return Integer.parseInt(value);
            } catch (Exception e) {
                return defaultVal;
            }
        }
    }

}
