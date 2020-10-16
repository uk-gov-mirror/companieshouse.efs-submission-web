package uk.gov.companieshouse.efs.web.controller;

import static uk.gov.companieshouse.efs.web.controller.CheckDetailsControllerImpl.ATTRIBUTE_NAME;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.SessionStatus;
import uk.gov.companieshouse.efs.web.model.CheckDetailsModel;

@RequestMapping(BaseControllerImpl.SERVICE_URI)
public interface CheckDetailsController {

    /**
     * Get request for the check your details.
     *
     * @param id            the submission id
     * @param companyNumber the company number
     * @param checkDetailsAttribute the checkDetails model
     * @param model         the checkDetails page model
     * @param request       contains the chs session id
     * @param session       the HTTP session
     * @param sessionStatus the session status; to be closed to finish the user's session journey
     * @return view name
     */
    @GetMapping("{id}/company/{companyNumber}/check-your-details")
    String checkDetails(@PathVariable String id, @PathVariable String companyNumber,
        @ModelAttribute(ATTRIBUTE_NAME) final CheckDetailsModel checkDetailsAttribute, Model model, HttpServletRequest request,
        HttpSession session, SessionStatus sessionStatus);

    @PostMapping(value = {"{id}/company/{companyNumber}/check-your-details"}, params = {"action=submit"})
    String postCheckDetails(@PathVariable String id, @PathVariable String companyNumber,
        @ModelAttribute(ATTRIBUTE_NAME) final CheckDetailsModel checkDetailsAttribute, BindingResult binding, Model model,
        HttpServletRequest request);
}