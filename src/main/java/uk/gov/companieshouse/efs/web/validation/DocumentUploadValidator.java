package uk.gov.companieshouse.efs.web.validation;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.model.efs.submissions.FileApi;
import uk.gov.companieshouse.efs.web.configuration.FileUploadConfiguration;
import uk.gov.companieshouse.efs.web.model.DocumentUploadModel;

/**
 * Class representing the customer bean validator for document uploads.
 */
@Component
public class DocumentUploadValidator implements BiFunction<DocumentUploadModel, BindingResult, List<MultipartFile>> {

    private static final String SELECTED_FILES_FIELD    = "selectedFiles";

    private ResourceBundle bundle;
    private FileUploadConfiguration configuration;

    @Autowired
    public DocumentUploadValidator(FileUploadConfiguration configuration, ResourceBundle bundle) {
        this.bundle = bundle;
        this.configuration = configuration;
    }

    @Override
    public List<MultipartFile> apply(final DocumentUploadModel model, final BindingResult binding) {

        // Define our collection for the valid file uploads.
        final List<MultipartFile> validFiles = new ArrayList<>();

        // Check all the currently supplied files pass the criteria.
        for (MultipartFile file : model.getSelectedFiles()) {

            final String filename = file.getOriginalFilename();
            final String mimeType = file.getContentType();

            // Determine how many files have already been uploaded
            final int filesAlreadyUploaded = (model.getDetails() == null) ? 0 : model.getDetails().getFiles().size();

            // Check that each file doesn't exceed the maximum file upload size.
            final long maximumFileUploadSize = toByteCount(configuration.getMaximumFilesize());

            if ((filesAlreadyUploaded + validFiles.size()) >= configuration.getMaximumFilesAllowed()) {
                // Check the maximum file uploads limit has not been exceeded.
                String pattern = bundle.getString("max_files_exceeded.documentUpload");
                MessageFormat formatter = new MessageFormat(pattern, Locale.UK);
                String errorText = formatter.format(new Object[]{filename, configuration.getMaximumFilesAllowed()});
                binding.rejectValue(SELECTED_FILES_FIELD, "error.file-upload-limit", errorText);

            } else if (!configuration.getDistinctMimeTypes().contains(mimeType)) {
                // Check the mime type of the file is currently allowed.
                String pattern = bundle.getString("invalid_file_type.documentUpload");
                MessageFormat formatter = new MessageFormat(pattern, Locale.UK);
                String errorText = formatter.format(new Object[]{filename, getAllowedFileExtensions()});
                binding.rejectValue(SELECTED_FILES_FIELD, "error.invalid-mime-type", errorText);

            } else if (file.getSize() == 0L) {
                // Check that each file isn't empty.
                String pattern = bundle.getString("min_file_size_exceeded.documentUpload");
                MessageFormat formatter = new MessageFormat(pattern, Locale.UK);
                String errorText = formatter.format(new Object[]{filename});
                binding.rejectValue(SELECTED_FILES_FIELD, "error.file-size-exceeded", errorText);

            } else if (file.getSize() > maximumFileUploadSize) {
                // Check that the file content doesn't exceed the maximum allowed.
                String pattern = bundle.getString("max_file_size_exceeded.documentUpload");
                MessageFormat formatter = new MessageFormat(pattern, Locale.UK);
                String errorText = formatter.format(new Object[]{configuration.getMaximumFilesize()});
                binding.rejectValue(SELECTED_FILES_FIELD, "error.file-size-exceeded", errorText);

            } else if (filesAlreadyUploaded > 0 && model.getDetails().getFiles().stream()
                    .map(FileApi::getFileName).anyMatch(name -> name.equalsIgnoreCase(filename))) {

                // Ensure that each file upload has a unique filename.
                String pattern = bundle.getString("duplicate_file.documentUpload");
                MessageFormat formatter = new MessageFormat(pattern, Locale.UK);
                String errorText = formatter.format(new Object[]{filename});
                binding.rejectValue(SELECTED_FILES_FIELD, "error.duplicate-file-found", errorText);
            }

            // All validation checks are passed, add the file to the results.
            if (!binding.hasErrors()) {
                validFiles.add(file);
            }
        }

        return validFiles;
    }

    private long toByteCount(final String sizeHuman) {
        final int KILOBYTE = 1000;

        long returnValue = Long.MAX_VALUE;

        if (!"-1".equals(sizeHuman)) {
            Map<String, Integer> powerMap = new HashMap<>();
            powerMap.put("GB", 3);
            powerMap.put("MB", 2);
            powerMap.put("KB", 1);

            Pattern pattern = Pattern.compile("([\\d.]+)([GMK]B)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(sizeHuman);

            if (matcher.find()) {
                String number = matcher.group(1);
                int pow = powerMap.get(matcher.group(2).toUpperCase());
                BigDecimal bytes = new BigDecimal(number);
                bytes = bytes.multiply(BigDecimal.valueOf(KILOBYTE).pow(pow));
                returnValue = bytes.longValue();
            }
        }

        return returnValue;
    }

    private String getAllowedFileExtensions() {
        final List<String> extensions = configuration.getDistinctExtensions().stream().sorted().collect(Collectors.toList());
        if (extensions.size() == 1) {
            return extensions.get(0);
        } else {
            return String.join(" or ", String.join(", ", extensions.subList(0, extensions.size() - 1)), extensions.get(extensions.size() - 1));
        }
    }

}
