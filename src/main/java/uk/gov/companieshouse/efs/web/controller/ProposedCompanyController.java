package uk.gov.companieshouse.efs.web.controller;

import static uk.gov.companieshouse.efs.web.controller.ProposedCompanyControllerImpl.ATTRIBUTE_NAME;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.efs.web.model.ProposedCompanyModel;

@RequestMapping(BaseControllerImpl.SERVICE_URI)
public interface ProposedCompanyController {

    @GetMapping("{id}/company/noCompany/proposed-company")
    String prepare(@PathVariable final String id,
        @ModelAttribute(ATTRIBUTE_NAME) ProposedCompanyModel proposedCompanyModel, Model model,
        HttpServletRequest request);

    @PostMapping("{id}/company/noCompany/proposed-company")
    String process(@PathVariable final String id,
        @ModelAttribute(ATTRIBUTE_NAME) ProposedCompanyModel proposedCompanyModel,
        BindingResult binding, Model model, HttpServletRequest request, HttpSession session);

}