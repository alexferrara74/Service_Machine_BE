package pilot.servizi.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ErpSyncProxyServiceTest {

    private static final String URL = "http://localhost:3000/api/sincronizza";

    private ErpSyncProxyService service(RestClient.Builder builder, String token) {
        ErpSyncProxyService service = new ErpSyncProxyService(builder);
        ReflectionTestUtils.setField(service, "syncUrl", URL);
        ReflectionTestUtils.setField(service, "syncToken", token);
        return service;
    }

    @Test
    void forwardsTheIntegrationResponseAsIs() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(URL)).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"status\":\"success\"}", MediaType.APPLICATION_JSON));

        ResponseEntity<byte[]> response = service(builder, "").synchronize();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"status\":\"success\"}", new String(response.getBody(), StandardCharsets.UTF_8));
        server.verify();
    }

    @Test
    void preservesTheErrorStatusOfTheIntegration() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(URL))
                .andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"status\":\"error\"}"));

        assertEquals(HttpStatus.CONFLICT, service(builder, "").synchronize().getStatusCode());
    }

    @Test
    void sendsTheBearerTokenWhenConfigured() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(URL)).andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer segreto"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        service(builder, "segreto").synchronize();

        server.verify();
    }

    @Test
    void sendsNoAuthorizationHeaderWithoutToken() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(URL))
                .andExpect(request -> assertNull(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        service(builder, "").synchronize();

        server.verify();
    }

    @Test
    void reportsBadGatewayWhenTheIntegrationIsUnreachable() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(URL)).andRespond(withException(new IOException("Connection refused")));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service(builder, "").synchronize());

        assertEquals(HttpStatus.BAD_GATEWAY, error.getStatusCode());
    }

    @Test
    void refusesASecondSynchronizationWhileOneIsRunning() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ErpSyncProxyService service = service(builder, "");
        server.expect(requestTo(URL)).andRespond(request -> {
            ResponseStatusException error = assertThrows(ResponseStatusException.class, service::synchronize);
            assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
            return withSuccess("{}", MediaType.APPLICATION_JSON).createResponse(request);
        });

        assertEquals(HttpStatus.OK, service.synchronize().getStatusCode());
        server.verify();
    }

    @Test
    void allowsANewSynchronizationAfterAFailure() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ErpSyncProxyService service = service(builder, "");
        server.expect(requestTo(URL)).andRespond(withException(new IOException("Connection refused")));
        server.expect(requestTo(URL)).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThrows(ResponseStatusException.class, service::synchronize);

        assertEquals(HttpStatus.OK, service.synchronize().getStatusCode());
    }
}
