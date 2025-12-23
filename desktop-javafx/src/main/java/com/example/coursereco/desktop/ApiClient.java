package com.example.coursereco.desktop;

import java.net.URI;
import java.net.http.*;

public class ApiClient {
    private final String baseUrl;
    private final HttpClient client = HttpClient.newHttpClient();

    public ApiClient(String baseUrl) { this.baseUrl = baseUrl; }

    public String get(String path) throws Exception {
        var req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .build();
        var res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) throw new RuntimeException(res.body());
        return res.body();
    }

    public String postJson(String path, String json) throws Exception {
        var req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        var res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) throw new RuntimeException(res.body());
        return res.body();
    }
}
