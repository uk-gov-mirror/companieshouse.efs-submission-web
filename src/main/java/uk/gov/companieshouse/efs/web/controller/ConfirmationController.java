package uk.gov.companieshouse.efs.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.SessionStatus;
import uk.gov.companieshouse.efs.web.formtemplates.model.FormTemplateModel;

@RequestMapping(BaseControllerImpl.SERVICE_URI)
public interface ConfirmationController {

    /**
     * Get request for the submission confirmation when company auth required.
     *
     * @param id            the submission id
     * @param companyNumber the company number
     * @param formTemplateAttribute the form template model
     * @param model         the confirmation page model
     * @param request       contains the chs session id
     * @param session       the HTTP session
     * @param sessionStatus the session status; to be closed to finish the user's session journey
     * @return view name
     */
    @GetMapping("{id}/company/{companyNumber}/confirmation")
    String getConfirmation(@PathVariable String id, @PathVariable String companyNumber,
        final FormTemplateModel formTemplateAttribute, Model model, HttpServletRequest request, HttpSession session,
        SessionStatus sessionStatus);
}