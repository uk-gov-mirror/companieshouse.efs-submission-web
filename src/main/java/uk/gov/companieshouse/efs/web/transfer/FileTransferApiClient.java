package uk.gov.companieshouse.efs.web.transfer;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * Client for using the File-Transfer-Api for upload / download / delete of files.
 */
@Component
public class FileTransferApiClient {

    private static final String HEADER_API_KEY = "x-api-key";
    private static final String DELETE_URI = "%s/%s";
    private static final String UPLOAD = "upload";
    private static final String CONTENT_DISPOSITION_VALUE = "form-data; name=%s; filename=%s";

    private RestTemplate restTemplate;

    @Value("${file.transfer.api.key}")
    private String fileTransferApiKey;

    @Value("${file.transfer.api.url}")
    private String fileTransferApiUrl;

    @Autowired
    public FileTransferApiClient(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private <T> FileTransferApiClientResponse makeApiCall(FileTransferOperation<T> operation, FileTransferResponseBuilder<T> responseBuilder) {
        FileTransferApiClientResponse response = new FileTransferApiClientResponse();

        try {
            T operationResponse = operation.execute();

            response = responseBuilder.createResponse(operationResponse);

        } catch (IOException ex) {
            response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    /**
     * Uploads a file to the file-transfer-api
     * Creates a multipart form request containing the file and sends to
     * the file-transfer-api. The response from the file-transfer-api contains
     * the new unique id for the file. This is captured and returned in the FileTransferApiClientResponse.
     * @param fileToUpload The file to upload
     * @return FileTransferApiClientResponse containing the file id if successful, and http status
     */
    public FileTransferApiClientResponse upload(final MultipartFile fileToUpload) {
        return makeApiCall(
                // FileTransferOperation
                () -> {
                    HttpHeaders headers = createFileTransferApiHttpHeaders();
                    LinkedMultiValueMap<String, String> fileHeaderMap = createUploadFileHeader(fileToUpload);
                    HttpEntity<byte[]> fileHttpEntity = new HttpEntity<>(fileToUpload.getBytes(), fileHeaderMap);
                    LinkedMultiValueMap<String, Object> body = createUploadBody(fileHttpEntity);
                    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
                    return restTemplate.postForEntity(fileTransferApiUrl, requestEntity, FileTransferApiResponse.class);
                },

                // FileTransferResponseBuilder - the output from FileTransferOperation is the
                // input into this FileTransferResponseBuilder
                responseEntity -> {
                    FileTransferApiClientResponse fileTransferApiClientResponse = new FileTransferApiClientResponse();
                    if (responseEntity != null) {
                        fileTransferApiClientResponse.setHttpStatus(responseEntity.getStatusCode());
                        FileTransferApiResponse apiResponse = responseEntity.getBody();
                        if (apiResponse != null) {
                            fileTransferApiClientResponse.setFileId(apiResponse.getId());
                        } else {
                            fileTransferApiClientResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                    } else {
                        fileTransferApiClientResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                    return fileTransferApiClientResponse;
                }
        );
    }

    /**
     * Delete a file from S3 via the file-transfer-api.
     *
     * @param fileId of the file to delete
     * @return FileTransferApiClientResponse containing the http status
     */
    public FileTransferApiClientResponse delete(final String fileId) {
        String deleteUrl = String.format(DELETE_URI, fileTransferApiUrl, fileId);

        return makeApiCall(
                // FileTransferOperation
                () -> {
                    HttpEntity<Void> request = new HttpEntity<>(createApiKeyHeader());
                    return restTemplate.exchange(deleteUrl, HttpMethod.DELETE, request, String.class);
                },

                // FileTransferResponseBuilder - the output from FileTransferOperation
                // is the input into this FileTransferResponseBuilder
                responseEntity -> {
                    FileTransferApiClientResponse fileTransferApiClientResponse = new FileTransferApiClientResponse();
                    if (responseEntity != null) {
                        fileTransferApiClientResponse.setHttpStatus(responseEntity.getStatusCode());
                    } else {
                        fileTransferApiClientResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                    return fileTransferApiClientResponse;
                }
        );
    }

    private HttpHeaders createFileTransferApiHttpHeaders() {
        HttpHeaders headers = createApiKeyHeader();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }

    private HttpHeaders createApiKeyHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_API_KEY, fileTransferApiKey);
        return headers;
    }

    private LinkedMultiValueMap<String, String> createUploadFileHeader(final MultipartFile fileToUpload) {
        LinkedMultiValueMap<String, String> fileHeaderMap = new LinkedMultiValueMap<>();
        fileHeaderMap.add(HttpHeaders.CONTENT_DISPOSITION, String.format(CONTENT_DISPOSITION_VALUE, UPLOAD, fileToUpload.getOriginalFilename()));
        return fileHeaderMap;
    }

    private LinkedMultiValueMap<String, Object> createUploadBody(final HttpEntity<byte[]> fileHttpEntity) {
        LinkedMultiValueMap<String, Object> multipartReqMap = new LinkedMultiValueMap<>();
        multipartReqMap.add(UPLOAD, fileHttpEntity);
        return multipartReqMap;
    }

}
