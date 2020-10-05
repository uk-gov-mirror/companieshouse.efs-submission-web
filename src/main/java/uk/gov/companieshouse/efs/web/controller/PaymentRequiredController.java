package uk.gov.companieshouse.efs.web.controller;

import static uk.gov.companieshouse.efs.web.controller.PaymentRequiredControllerImpl.ATTRIBUTE_NAME;

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
import uk.gov.companieshouse.efs.web.model.PaymentRequiredModel;

@RequestMapping(BaseControllerImpl.SERVICE_URI)
public interface PaymentRequiredController {

    /**
     * Get request for whether user needs to authenticate their company before accessing payment required.
     *
     * @param id                       the submission id
     * @param companyNumber            the company number
     * @param paymentRequiredAttribute the payment details see {@link PaymentRequiredModel}
     * @param formTemplateAttribute    the details of the document selected
     * @param model                    the document upload model
     * @param request                  contains the chs session id
     * @param session                  the HTTP session
     * @return view name
     */
    @GetMapping("{id}/company/{companyNumber}/payment-required")
    String getPaymentReference(@PathVariable String id, @PathVariable String companyNumber,
        @ModelAttribute(ATTRIBUTE_NAME) PaymentRequiredModel paymentRequiredAttribute,
        @ModelAttribute(FormTemplateControllerImpl.ATTRIBUTE_NAME) FormTemplateModel formTemplateAttribute, Model model,
        HttpServletRequest request, HttpSession session);

    /**
     * Post payment details.
     *
     * @param id                     the submission ID
     * @param companyNumber          the company number
     * @param paymentRequiredModel   the payment details see {@link PaymentRequiredModel}
     * @param binding                the MVC binding result
     * @param request                contains the chs session id
     * @param session                the HTTP session
     * @return view name
     */
    @PostMapping("{id}/company/{companyNumber}/payment-required")
    String postPaymentReference(@PathVariable String id, @PathVariable String companyNumber,
        @ModelAttribute(ATTRIBUTE_NAME) PaymentRequiredModel paymentRequiredModel, BindingResult binding,
        Model model, HttpServletRequest request, HttpSession session);
}
