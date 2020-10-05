package uk.gov.companieshouse.efs.web.transfer;

import java.io.IOException;

/**
 * Functional interface for the file transfer response builder.
 */
@FunctionalInterface
public interface FileTransferResponseBuilder<T> {

   FileTransferApiClientResponse createResponse(T input) throws IOException;

}
