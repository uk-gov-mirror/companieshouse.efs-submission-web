package uk.gov.companieshouse.efs.web.controller;

import static uk.gov.companieshouse.efs.web.controller.RegisteredCompanyControllerImpl.ATTRIBUTE_NAME;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.efs.web.model.RegisteredCompanyModel;

@RequestMapping(BaseControllerImpl.SERVICE_URI)
public interface RegisteredCompanyController {

    @GetMapping("registered-company")
    String prepare(@ModelAttribute(ATTRIBUTE_NAME) RegisteredCompanyModel registeredCompanyModel,
        Model model, HttpServletRequest request);

    @PostMapping("registered-company")
    String process(@ModelAttribute(ATTRIBUTE_NAME) RegisteredCompanyModel registeredCompanyModel,
        BindingResult binding, Model model, HttpServletRequest request, HttpSession session);

}
