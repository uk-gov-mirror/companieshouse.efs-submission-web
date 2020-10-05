package uk.gov.companieshouse.efs.web.controller;

import static uk.gov.companieshouse.efs.web.controller.CompanyDetailControllerImpl.ATTRIBUTE_NAME;

import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.efs.web.model.company.CompanyDetail;

@RequestMapping(BaseControllerImpl.SERVICE_URI)
public interface CompanyDetailController {

    /**
     * Respond to GET request for company details page.
     *
     * @param id the submission id
     * @param companyNumber the company number
     * @param companyDetailAttribute the company detail model
     * @param model the company detail page model
     * @param request the HTTP request
     * @return the next page view name or a URL
     */
    @GetMapping("{id}/company/{companyNumber}/details")
    String getCompanyDetail(@PathVariable String id, @PathVariable String companyNumber,
        @ModelAttribute(ATTRIBUTE_NAME) CompanyDetail companyDetailAttribute, Model model, HttpServletRequest request);

    /**
     * Respond to POST request for company details page.
     *
     * @param id the submission id
     * @param companyNumber the company number
     * @param companyDetailAttribute the company detail model
     * @param model the company detail page model
     * @param request the HTTP request
     * @return the next page view name or a URL
     */
    @PostMapping(value = {"{id}/company/{companyNumber}/details"}, params = {"action=submit"})
    String postCompanyDetail(@PathVariable String id, @PathVariable String companyNumber,
        @ModelAttribute(ATTRIBUTE_NAME) CompanyDetail companyDetailAttribute, Model model, HttpServletRequest request);
}
