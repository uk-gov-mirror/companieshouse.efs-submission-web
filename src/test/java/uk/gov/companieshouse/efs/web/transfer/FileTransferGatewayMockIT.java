package uk.gov.companieshouse.efs.web.transfer;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import com.google.common.net.MediaType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockserver.client.ForwardChainExpectation;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.efs.web.util.IntegrationTestHelper;

/**
 * FileTransferGatewayIntegrationTest with mock server for file-transfer-api
 */
@Tag("integration")
@ActiveProfiles("test")
@SpringBootTest
class FileTransferGatewayMockIT {
    private static Map<String, String> storedEnvironment;
    public static SystemLambda.WithEnvironmentVariables springEnvironment;

    @Autowired
    private FileTransferApiClient gateway;

    private ClientAndServer mockServer;

    @BeforeAll
    static void setUpSpringEnvironment() {
        storedEnvironment = new HashMap<>(System.getenv());
        springEnvironment = IntegrationTestHelper.withSpringEnvironment()
                .and("LOGGING_LEVEL", "DEBUG");

        ReflectionTestUtils.invokeMethod(springEnvironment, "setEnvironmentVariables");
    }

    @AfterAll
    static void tearDownSpringEnvironment() {
        ReflectionTestUtils.invokeMethod(springEnvironment, "restoreOriginalVariables", storedEnvironment);
    }

    @BeforeEach
    public void setUp() {
        mockServer = ClientAndServer.startClientAndServer(8081);
    }

    @AfterEach
    public void stopMockApiServer() {
        mockServer.stop();
    }
    
    @Test
    void willThrowHttpClientExceptionOnUnsupportedMediaType() throws IOException {
        MultipartFile mockFile = new MockMultipartFile("file", "file.txt", "text/plain", "test".getBytes());

        mockServerExpectation("/", "POST").respond(HttpResponse.response().withStatusCode(415));

        assertThrows(HttpClientErrorException.class, () -> gateway.upload(mockFile));
    }

    @Disabled("Needs rework; throws HttpClientErrorException$UnsupportedMediaType like the test above")
    @Test
    void willThrowHttpServerExceptionIf500Returned() throws IOException {
        MultipartFile mockFile =
                new MockMultipartFile("file", "file.txt", "text/plain", "test".getBytes());

        mockServerExpectation("/", "POST").respond(HttpResponse.response()
                .withStatusCode(201).withBody("", MediaType.JSON_UTF_8));

        assertThrows(HttpServerErrorException.class, () -> gateway.upload(mockFile));
    }

    private ForwardChainExpectation mockServerExpectation(String path, String httpMethod) throws IOException {
        return mockServer.when(HttpRequest.request().withMethod(httpMethod).withPath(path).withKeepAlive(true));
    }
}
