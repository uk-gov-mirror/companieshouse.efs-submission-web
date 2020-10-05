package uk.gov.companieshouse.efs.web.transfer;

import java.io.IOException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.ExpectedException;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.client.ForwardChainExpectation;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.efs.web.configuration.FileUploadConfiguration;
import uk.gov.companieshouse.efs.web.configuration.IntegrationTestConfig;
import uk.gov.companieshouse.efs.web.configuration.LoggingConfig;
import uk.gov.companieshouse.efs.web.configuration.SpringWebConfig;

/**
 * FileTransferGatewayIntegrationTest with mock server for file-transfer-api
 */
@Tag("integration")
@SpringBootTest
@ContextConfiguration(classes = {SpringWebConfig.class})
@Import({IntegrationTestConfig.class, LoggingConfig.class, FileUploadConfiguration.class})
@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class FileTransferGatewayMockIT {

    @Autowired
    private FileTransferApiClient gateway;

    private static ClientAndServer mockServer;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void startMockApiServer() {
        mockServer = ClientAndServer.startClientAndServer(8081);
    }

    @AfterClass
    public static void stopMockApiServer() {
        mockServer.stop();
    }

    @Before
    public void setup() {
        mockServer.reset();
    }

    @Test
    public void willThrowHttpClientExceptionOnUnsupportedMediaType() throws IOException {
        MultipartFile mockFile = new MockMultipartFile("file", "file.txt", "text/plain", "test".getBytes());

        mockServerExpectation("/", "POST").respond(HttpResponse.response().withStatusCode(415));

        expectedException.expect(HttpClientErrorException.class);

        gateway.upload(mockFile);
    }

    @Test
    public void willThrowHttpServerExceptionIf500Returned() throws IOException {
        MultipartFile mockFile = new MockMultipartFile("file", "file.txt", "text/plain", "test".getBytes());

        mockServerExpectation("/", "POST").respond(HttpResponse.response().withStatusCode(500));

        expectedException.expect(HttpServerErrorException.class);

        gateway.upload(mockFile);
    }

    private ForwardChainExpectation mockServerExpectation(String path, String httpMethod) throws IOException {
        return mockServer.when(HttpRequest.request().withMethod(httpMethod).withPath(path).withKeepAlive(true));
    }
}
