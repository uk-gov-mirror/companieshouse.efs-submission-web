package uk.gov.companieshouse.efs.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.efs.web.configuration.FileUploadConfiguration;
import uk.gov.companieshouse.efs.web.transfer.FileTransferApiClient;
import uk.gov.companieshouse.efs.web.validation.DocumentUploadValidator;

/**
 * Parameter object used to group file transfer related objects.
 * Reduces the number of parameters taken by the constructor and makes sonar happy.
 */
@Component
class FileTransferManager {
    private final FileTransferApiClient fileTransferApiClient;
    private final FileUploadConfiguration fileUploadConfiguration;
    private final DocumentUploadValidator documentUploadValidator;

    @Autowired
    private FileTransferManager(FileTransferApiClient fileTransferApiClient,
                                FileUploadConfiguration fileUploadConfiguration,
                                DocumentUploadValidator documentUploadValidator) {

        this.fileTransferApiClient = fileTransferApiClient;
        this.fileUploadConfiguration = fileUploadConfiguration;
        this.documentUploadValidator = documentUploadValidator;
    }

    public FileTransferApiClient getFileTransferApiClient() {
        return fileTransferApiClient;
    }

    public FileUploadConfiguration getFileUploadConfiguration() {
        return fileUploadConfiguration;
    }

    public DocumentUploadValidator getDocumentUploadValidator() {
        return documentUploadValidator;
    }
}
