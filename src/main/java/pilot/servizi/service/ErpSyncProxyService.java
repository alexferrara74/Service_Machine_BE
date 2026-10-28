package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class ErpSyncProxyService {

    private final RestClient.Builder restClientBuilder;

    @Value("${integration.sync.url}")
    private String syncUrl;

    public ResponseEntity<byte[]> synchronize() {
        return restClientBuilder.build()
                .post()
                .uri(syncUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange((request, response) -> ResponseEntity
                        .status(response.getStatusCode())
                        .contentType(response.getHeaders().getContentType() != null
                                ? response.getHeaders().getContentType()
                                : MediaType.APPLICATION_JSON)
                        .body(response.getBody().readAllBytes()));
    }
}
