package uk.gov.companieshouse.efs.web.controller;

import static uk.gov.companieshouse.efs.web.controller.RemoveDocumentControllerImpl.ATTRIBUTE_NAME;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.efs.web.model.RemoveDocumentModel;

@RequestMapping(BaseControllerImpl.SERVICE_URI)
public interface RemoveDocumentController {

    /**
     * Get request for whether user removes a document.
     *
     * @param id                  the submission id
     * @param companyNumber       the company number
     * @param fileId              the file identifier for the document to be removed.
     * @param removeDocumentAttribute the remove document details see {@link RemoveDocumentModel}
     * @param model               the remove document model
     * @param request             contains the chs session id
     * @return view name
     */
    @GetMapping("{id}/company/{companyNumber}/remove-document/{fileId}")
    String prepare(@PathVariable String id, @PathVariable String companyNumber, @PathVariable String fileId,
        @ModelAttribute(ATTRIBUTE_NAME) RemoveDocumentModel removeDocumentAttribute, Model model, HttpServletRequest request);

    /**
     * Post request for whether user removes a document.
     *
     * @param id                     the submission id
     * @param companyNumber          the company number
     * @param fileId                 the file identifier for the document to be removed.
     * @param removeDocumentAttribute    the remove document details see {@link RemoveDocumentModel}
     * @param binding                the MVC binding result
     * @param model                  the remove document model
     * @param request                contains the chs session id
     * @param session                the HTTP session
     * @return view name
     */
    @PostMapping("{id}/company/{companyNumber}/remove-document/{fileId}")
    String process(@PathVariable String id, @PathVariable String companyNumber, @PathVariable String fileId,
        @ModelAttribute(ATTRIBUTE_NAME) RemoveDocumentModel removeDocumentAttribute, BindingResult binding, Model model,
        HttpServletRequest request, HttpSession session);
}
