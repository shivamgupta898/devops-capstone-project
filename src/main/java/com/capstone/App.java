package com.capstone;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;

public class App {
    private static final String DB_HOST = System.getenv().getOrDefault("DB_HOST", "mysql-service");
    private static final String DB_PORT = System.getenv().getOrDefault("DB_PORT", "3306");
    private static final String DB_NAME = System.getenv().getOrDefault("DB_NAME", "capstonedb");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "capstoneuser");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "userpassword123");

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/health", new HealthHandler());
        server.createContext("/db-status", new DbStatusHandler());
        server.setExecutor(null);
        System.out.println("2-Tier Web Server started on port 8080...");
        server.start();
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "<h1>DevOps Capstone Project: 2-Tier Architecture Live on Kubernetes!</h1>"
                            + "<p>Application Tier is running and connected to Kubernetes cluster.</p>";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"status\":\"UP\",\"tier\":\"web-app\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class DbStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?allowPublicKeyRetrieval=true&useSSL=false";
            String response;
            int statusCode = 200;

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection conn = DriverManager.getConnection(url, DB_USER, DB_PASSWORD)) {
                    response = "{\"database\":\"CONNECTED\",\"host\":\"" + DB_HOST + "\",\"db_name\":\"" + DB_NAME + "\"}";
                }
            } catch (Exception e) {
                statusCode = 503;
                response = "{\"database\":\"DISCONNECTED\",\"error\":\"" + e.getMessage() + "\"}";
            }

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}