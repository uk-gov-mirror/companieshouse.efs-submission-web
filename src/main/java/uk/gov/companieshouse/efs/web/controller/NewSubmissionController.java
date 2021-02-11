package uk.gov.companieshouse.efs.web.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.companieshouse.efs.web.model.ProposedCompanyModel;
import uk.gov.companieshouse.efs.web.model.company.CompanyDetail;

@RequestMapping(BaseControllerImpl.SERVICE_URI)
public interface NewSubmissionController {
    /**
     * Endpoint for new submissions (no company number yet).
     *
     * @param companyDetailAttribute the company selection details see {@link CompanyDetail}
     * @param sessionStatus          the MVC session status
     * @param request                the MVC servlet request
     * @return the view to show
     */
    @GetMapping("/new-company-submission")
    String newCompanySubmission(
        @ModelAttribute(CompanyDetailControllerImpl.ATTRIBUTE_NAME) CompanyDetail companyDetailAttribute,
        SessionStatus sessionStatus, HttpServletRequest request, RedirectAttributes attributes);

    /**
     * @param proposedCompanyModel   the proposed company details see {@link ProposedCompanyModel}
     * @param sessionStatus          the MVC session status
     * @param request                the MVC servlet request
     * @return the view to show
     */
    @GetMapping("/new-blank-submission")
    String newBlankSubmission(@ModelAttribute(ProposedCompanyControllerImpl.ATTRIBUTE_NAME)
        ProposedCompanyModel proposedCompanyModel,
        SessionStatus sessionStatus, HttpServletRequest request);

    /**
     * Endpoint for new submissions (given company number).
     *
     * @param companyNumber the company number to apply
     * @param companyDetailAttribute the company selection details see {@link CompanyDetail}
     * @param sessionStatus          the MVC session status
     * @param request                the MVC servlet request
     * @return the view to show
     */
    @GetMapping("/company/{companyNumber}/new-submission")
    String newSubmissionForCompany(@PathVariable String companyNumber,
        @ModelAttribute(CompanyDetailControllerImpl.ATTRIBUTE_NAME) CompanyDetail companyDetailAttribute,
        SessionStatus sessionStatus, HttpServletRequest request, RedirectAttributes attributes);
}
