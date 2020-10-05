package uk.gov.companieshouse.efs.web.transfer;

import java.io.IOException;

/**
 * Functional interface for the file transfer operation.
 */
@FunctionalInterface
public interface FileTransferOperation<T> {

    T execute() throws IOException;

}
