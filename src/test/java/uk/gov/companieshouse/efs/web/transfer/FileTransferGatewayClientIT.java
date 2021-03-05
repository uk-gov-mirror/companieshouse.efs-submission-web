package uk.gov.companieshouse.efs.web.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpResponse.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stefanbirkner.systemlambda.SystemLambda;
import com.google.common.net.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockserver.client.ForwardChainExpectation;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import uk.gov.companieshouse.efs.web.util.IntegrationTestHelper;

/**
 * FileTransferGatewayClientIT
 */
@Tag("integration")
//@TestPropertySource
@ActiveProfiles("test")
@SpringBootTest
class FileTransferGatewayClientIT {
    private static Map<String, String> storedEnvironment;
    public static SystemLambda.WithEnvironmentVariables springEnvironment;

    @Autowired
    private FileTransferApiClient fileTransferApiClient;

    @Value("${http_proxy:}")
    private String envHttpProxy;

    @Value("${https_proxy:}")
    private String envHttpsProxy;

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
    void startServer() {
        mockServer = startClientAndServer(1080);
    }

    @AfterEach
    public void stopServer() {
        mockServer.stop();
    }

    @PostConstruct
    public void setProxy() {
        setProxySystemProperties("http", envHttpProxy);
        setProxySystemProperties("https", envHttpsProxy);
    }

    private void setProxySystemProperties(String protocol, String envProxyValue) {
        if (StringUtils.isNotBlank(envProxyValue)) {
            final String portSeparator = ":";
            String proxyHost;
            String proxyPort;

            envProxyValue = StringUtils.remove(envProxyValue, "http://");

            if (envProxyValue.contains(portSeparator)) {
                proxyHost = StringUtils.substringBefore(envProxyValue, portSeparator);
                proxyPort = StringUtils.substringAfter(envProxyValue, portSeparator);
            } else {
                proxyHost = envProxyValue;
                proxyPort = "8080";
            }

            System.setProperty(protocol + ".proxyHost", proxyHost);
            System.setProperty(protocol + ".proxyPort", proxyPort);
        }
    }

    @Test
    void willUploadFile() throws Exception {
        final String filename = "test.png";
        final String fileFolder = "./src/test/resources/file-upload/";
        final String uploadFilePath = fileFolder + filename;
        final String downloadFilePath = fileFolder + "download-" + filename;

        // Prepare upload
        FileTransferApiResponse mockResponse = new FileTransferApiResponse();
        mockResponse.setId(UUID.randomUUID()
                .toString());

        final String responseBody = new ObjectMapper().writeValueAsString(mockResponse);

        mockServerExpectation("/", "POST").respond(
                response().withBody(responseBody, MediaType.JSON_UTF_8)
                        .withStatusCode(201));

        // Upload
        File uploadFile = new File(uploadFilePath);
        FileTransferApiClientResponse uploadResponse = uploadFile(uploadFile);

        assertEquals(HttpStatus.CREATED, uploadResponse.getHttpStatus());
        assertTrue(StringUtils.isNotBlank(uploadResponse.getFileId()));
    }

    private FileTransferApiClientResponse uploadFile(final File uploadFile) throws Exception {
        FileItem fileItem =
                new DiskFileItem("file", Files.probeContentType(uploadFile.toPath()), false,
                        uploadFile.getName(), (int) uploadFile.length(),
                        uploadFile.getParentFile());
        try {
            IOUtils.copy(new FileInputStream(uploadFile), fileItem.getOutputStream());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        MultipartFile multipartFile = new CommonsMultipartFile(fileItem);

        System.out.println("Calling upload...");

        return fileTransferApiClient.upload(multipartFile);
    }

    private ForwardChainExpectation mockServerExpectation(String path, String httpMethod) throws IOException {
        return mockServer.when(HttpRequest.request().withMethod(httpMethod).withPath(path).withKeepAlive(true));
    }
}
