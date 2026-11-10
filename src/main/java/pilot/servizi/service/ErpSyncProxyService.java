package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ErpSyncProxyService {

    private final RestClient.Builder restClientBuilder;

    @Value("${integration.sync.url}")
    private String syncUrl;

    @Value("${integration.sync.token:}")
    private String syncToken;

    public ResponseEntity<byte[]> synchronize() {
        try {
            return restClientBuilder.build()
                    .post()
                    .uri(syncUrl)
                    .headers(headers -> {
                        if (!syncToken.isBlank()) {
                            headers.setBearerAuth(syncToken);
                        }
                    })
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange((request, response) -> ResponseEntity
                            .status(response.getStatusCode())
                            .contentType(response.getHeaders().getContentType() != null
                                    ? response.getHeaders().getContentType()
                                    : MediaType.APPLICATION_JSON)
                            .body(response.getBody().readAllBytes()));
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Servizio di sincronizzazione ERP non raggiungibile");
        }
    }
}
