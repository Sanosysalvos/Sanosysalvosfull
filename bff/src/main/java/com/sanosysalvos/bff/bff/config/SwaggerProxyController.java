package com.sanosysalvos.bff.bff.config;

import java.net.URI;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class SwaggerProxyController {

    private final RestTemplate restTemplate;
    private final Map<String, String> serviceUrls;

    public SwaggerProxyController(
            RestTemplate restTemplate,
            @Value("${ms.users.url}") String msUsersUrl,
            @Value("${ms.pets.url}") String msPetsUrl,
            @Value("${ms.notification.url}") String msNotificationUrl,
            @Value("${ms.sightings.url}") String msSightingsUrl,
            @Value("${ms.matching.url}") String msMatchingUrl) {
        this.restTemplate = restTemplate;
        this.serviceUrls = Map.of(
                "ms-users", normalizeUrl(msUsersUrl),
                "ms-pets", normalizeUrl(msPetsUrl),
                "ms-notification", normalizeUrl(msNotificationUrl),
                "ms-sightings", normalizeUrl(msSightingsUrl),
                "ms-matching", normalizeUrl(msMatchingUrl));
    }

    @Bean
    public RouterFunction<ServerResponse> swaggerProxyRouter() {
        return RouterFunctions.route(
                RequestPredicates.GET("/api-docs/{service}"),
                this::proxyApiDocs);
    }

    private ServerResponse proxyApiDocs(ServerRequest request) {
        String service = request.pathVariable("service")
                .trim()
                .toLowerCase(Locale.ROOT);

        String baseUrl = serviceUrls.get(service);
        if (baseUrl == null) {
            log("Servicio no reconocido: %s", service);
            return ServerResponse.badRequest().body("Servicio no reconocido: " + service);
        }

        // All microservices use /v3/api-docs endpoint
        String targetUrl = baseUrl + "/v3/api-docs";
        if (request.uri().getRawQuery() != null && !request.uri().getRawQuery().isBlank()) {
            targetUrl += "?" + request.uri().getRawQuery();
        }
        log("Proxying /api-docs request for service '%s' to URL: %s", service, targetUrl);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    URI.create(targetUrl),
                    HttpMethod.GET,
                    null,
                    String.class);

            log("Received %s from %s", response.getStatusCode(), targetUrl);
            ServerResponse.BodyBuilder builder = ServerResponse.status(response.getStatusCode());
            response.getHeaders().forEach((name, values) -> builder.header(name, values.toArray(String[]::new)));
            return builder.body(response.getBody());

        } catch (HttpStatusCodeException ex) {
            log("HttpStatusCodeException calling %s: %s %s", targetUrl, ex.getStatusCode(), ex.getResponseBodyAsString());
            ServerResponse.BodyBuilder builder = ServerResponse.status(ex.getStatusCode());
            if (ex.getResponseHeaders() != null) {
                ex.getResponseHeaders().forEach((name, values) -> builder.header(name, values.toArray(String[]::new)));
            }
            return builder.body(ex.getResponseBodyAsString());
        } catch (ResourceAccessException ex) {
            log("ResourceAccessException calling %s: %s", targetUrl, ex.getMessage());
            return ServerResponse.status(502).body("Error al conectar con el microservicio: " + ex.getMessage());
        } catch (Exception ex) {
            log("Unexpected exception calling %s: %s", targetUrl, ex.toString());
            return ServerResponse.status(500).body("Error interno del proxy: " + ex.getMessage());
        }
    }

    private static String buildTargetUrl(String baseUrl, String requestPath, String rawQuery) {
        String cleanBaseUrl = baseUrl.replaceAll("/+$", "");
        String cleanPath = requestPath.startsWith("/") ? requestPath : "/" + requestPath;
        String url = cleanBaseUrl + cleanPath;
        if (rawQuery != null && !rawQuery.isBlank()) {
            url += "?" + rawQuery;
        }
        return url;
    }

    private static String normalizeUrl(String url) {
        return url == null ? null : url.trim().replaceAll("/+$", "");
    }

    private static void log(String message, Object... args) {
        System.out.println("[SwaggerProxy] " + String.format(message, args));
    }
}