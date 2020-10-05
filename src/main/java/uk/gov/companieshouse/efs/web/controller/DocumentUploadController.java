package uk.gov.companieshouse.efs.web.controller;

import static uk.gov.companieshouse.efs.web.controller.DocumentUploadControllerImpl.ATTRIBUTE_NAME;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.efs.web.formtemplates.controller.FormTemplateControllerImpl;
import uk.gov.companieshouse.efs.web.formtemplates.model.FormTemplateModel;
import uk.gov.companieshouse.efs.web.model.DocumentUploadModel;

@RequestMapping(BaseControllerImpl.SERVICE_URI)
public interface DocumentUploadController {

    /**
     * Get request for whether user performs document upload.
     *
     * @param id                      the submission id
     * @param companyNumber           the company number
     * @param documentUploadAttribute the document details see {@link DocumentUploadModel}
     * @param model                   the document upload model
     * @param servletRequest          contains the chs session id
     * @param session                 the HTTP session
     * @return view name
     */
    @GetMapping("{id}/company/{companyNumber}/document-upload")
    String prepare(@PathVariable String id, @PathVariable String companyNumber,
        @ModelAttribute(ATTRIBUTE_NAME) DocumentUploadModel documentUploadAttribute,
       Model model, HttpServletRequest servletRequest, HttpSession session);

    /**
     * Post request submits the document upload details.
     *
     * @param id                      the submission id
     * @param companyNumber           the company number
     * @param documentUploadAttribute the document details see {@link DocumentUploadModel}
     * @param binding                 holds the result of the validation
     * @param model                   the document upload model
     * @param request                 contains the chs session id
     * @param session                 the HTTP session
     * @return view url
     */
    @PostMapping("{id}/company/{companyNumber}/document-upload")
    String process(@PathVariable String id, @PathVariable String companyNumber,
        @ModelAttribute(ATTRIBUTE_NAME) DocumentUploadModel documentUploadAttribute, BindingResult binding, Model model,
        HttpServletRequest request, HttpSession session);

    /**
     * Post request submits the document upload details.
     *
     * @param id                      the submission id
     * @param companyNumber           the company number
     * @param documentUploadAttribute the document details see {@link DocumentUploadModel}
     * @param binding                 holds the result of the validation
     * @param model                   the document upload model
     * @param request                 contains the chs session id
     * @param session                 the HTTP session
     * @return view url
     */
    @PostMapping(value = {"{id}/company/{companyNumber}/document-upload"}, params = {"action=submit"})
    String finish(@PathVariable String id, @PathVariable String companyNumber,
        @ModelAttribute(ATTRIBUTE_NAME) DocumentUploadModel documentUploadAttribute, BindingResult binding, Model model,
        HttpServletRequest request, HttpSession session);
}
