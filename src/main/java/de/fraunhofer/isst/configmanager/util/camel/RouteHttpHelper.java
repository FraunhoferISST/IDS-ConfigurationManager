package de.fraunhofer.isst.configmanager.util.camel;

import de.fraunhofer.isst.configmanager.util.OkHttpUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Component for deploying and deleting Camel routes at the Camel application via HTTP.
 */
@Slf4j
@Component
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RouteHttpHelper {
    /**
     * URL of the Camel application.
     */
    @Value("${camel.application.url}")
    String camelApplicationUrl;

    /**
     * Username for the Camel application.
     */
    @Value("${camel.application.username}")
    String camelApplicationUsername;

    /**
     * Password for the Camel application.
     */
    @Value("${camel.application.password}")
    String camelApplicationPassword;

    /**
     * The Camel application's API path for managing routes.
     */
    @Value("${camel.application.path.routes}")
    String camelApplicationRoutesPath;

    /**
     * The OkHttpClient for sending requests to the Camel application.
     */
    final OkHttpClient httpClient = OkHttpUtils.getUnsafeOkHttpClient();

    /**
     * Sends an XML route to the Camel application specified in application.properties as a file.
     *
     * @param xml the XML route
     * @throws IOException if the HTTP request cannot be sent or the response status code is not 2xx
     */
    public void sendRouteFileToCamelApplication(final String xml) throws IOException {
        final var url = camelApplicationUrl + camelApplicationRoutesPath;

        final var body = new MultipartBody.Builder().addFormDataPart("file",
                "route.xml", RequestBody.create(xml.getBytes(StandardCharsets.UTF_8),
                        MediaType.parse("application/xml"))).build();

        final var request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", Credentials.basic(camelApplicationUsername,
                        camelApplicationPassword))
                .build();

        try {
            final var response = httpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                if (log.isErrorEnabled()) {
                    log.error("Error sending file to Camel: {}, {}", response.code(),
                            response.body() != null ? Objects.requireNonNull(response.body()).string() : "No response body.");
                }

                throw new IOException("Request for deploying route was unsuccessful with code "
                        + response.code());
            }

        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("Error sending file to Camel: {}", e.getMessage());
            }

            throw e;
        }
    }

    /**
     * Deletes a route with the given ID at the Camel application specified in
     * application.properties.
     *
     * @param routeId ID of the route to delete
     * @throws IOException if the HTTP request cannot be sent or the response status code is not 2xx
     */
    public void deleteRouteAtCamelApplication(final String routeId) throws IOException {
        final var url = camelApplicationUrl + camelApplicationRoutesPath + "/" + routeId;

        final var request = new Request.Builder()
                .url(url)
                .delete()
                .header("Authorization", Credentials.basic(camelApplicationUsername,
                        camelApplicationPassword))
                .build();

        try {
            final var response = httpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                if (log.isErrorEnabled()) {
                    log.error("Error deleting route at Camel: {}, {}", response.code(),
                            response.body() != null ? Objects.requireNonNull(response.body()).string() : "No response body.");
                }

                throw new IOException("Request for deleting route was unsuccessful with code "
                        + response.code());
            }
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("Error deleting route at Camel: {}", e.getMessage());
            }

            throw e;
        }
    }
}
