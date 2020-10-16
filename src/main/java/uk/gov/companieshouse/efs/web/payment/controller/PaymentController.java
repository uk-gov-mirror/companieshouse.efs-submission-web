package uk.gov.companieshouse.efs.web.payment.controller;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.companieshouse.efs.web.controller.BaseControllerImpl;

@RequestMapping(BaseControllerImpl.SERVICE_URI)
public interface PaymentController {
    /**
     * Get request for the payment
     *
     * @param id            the application id
     * @param companyNumber the company number
     * @param request       the {@link HttpServletRequest}
     * @return the view url
     */
    @GetMapping(value = {"{id}/company/{companyNumber}/payment"})
    String payment(@PathVariable String id, @PathVariable String companyNumber, HttpServletRequest request);

    /**
     * Get request to handle payment complete.
     * When the payment status is paid the user is redirected to the confirmation page
     * otherwise they are returned to the check details page.
     *
     * @param request        the HttpServletRequest
     * @param id             the submission id
     * @param companyNumber  the company number
     * @param status         the payment status
     * @param state          the payment state
     * @param servletRequest contains the chs session id
     * @return view name appropriate for the payment state
     */
    @GetMapping("{id}/company/{companyNumber}/payment-complete-callback")
    String paymentCallback(final HttpServletRequest request, @PathVariable String id,
        @PathVariable String companyNumber, @RequestParam(name = "status") String status,
        @RequestParam(name = "ref") String ref, @RequestParam(name = "state") String state, ServletRequest servletRequest);

}
