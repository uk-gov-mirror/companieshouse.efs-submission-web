package uk.gov.companieshouse.efs.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.submissions.PaymentReferenceApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.web.categorytemplates.service.api.CategoryTemplateService;
import uk.gov.companieshouse.efs.web.formtemplates.controller.FormTemplateControllerImpl;
import uk.gov.companieshouse.efs.web.formtemplates.model.FormTemplateModel;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;
import uk.gov.companieshouse.efs.web.model.PaymentRequiredModel;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.efs.web.service.session.SessionService;
import uk.gov.companieshouse.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Objects;

import static uk.gov.companieshouse.efs.web.controller.PaymentRequiredControllerImpl.ATTRIBUTE_NAME;

@Controller
@SessionAttributes(ATTRIBUTE_NAME)
@SuppressWarnings("squid:S3753")
/* S3753: "@Controller" classes that use "@SessionAttributes" must call "setComplete" on their "SessionStatus" objects
 *
 * The nature of the web journey across several controllers means it's not appropriate to do this. However,
 * setComplete() is properly called in ConfirmationControllerImpl at the end of the submission journey.
 */
public class PaymentRequiredControllerImpl extends BaseControllerImpl implements PaymentRequiredController {

    /**
     * Define the model name for this action.
     */
    public static final String ATTRIBUTE_NAME = "paymentRequired";

    private PaymentRequiredModel paymentRequiredAttribute;

    /**
     * Constructor used by child controllers.
     *
     * @param logger the CH logger
     */
    @Autowired
    public PaymentRequiredControllerImpl(final Logger logger, SessionService sessionService,
                                         ApiClientService apiClientService, FormTemplateService formTemplateService,
                                         CategoryTemplateService categoryTemplateService, PaymentRequiredModel paymentRequiredAttribute) {
        super(logger, sessionService, apiClientService, formTemplateService, categoryTemplateService);
        this.paymentRequiredAttribute = paymentRequiredAttribute;
    }

    @ModelAttribute(ATTRIBUTE_NAME)
    public PaymentRequiredModel getPaymentRequiredAttribute() {
        return paymentRequiredAttribute;
    }

    @Override
    public String getViewName() {
        return ViewConstants.PAYMENT_REQUIRED.asView();
    }

    @Override
    public String getPaymentReference(final String id, @PathVariable String companyNumber,
                                      @ModelAttribute(ATTRIBUTE_NAME) final PaymentRequiredModel paymentRequiredAttribute,
                                      @ModelAttribute(FormTemplateControllerImpl.ATTRIBUTE_NAME) final FormTemplateModel formTemplateAttribute,
                                      final Model model, final HttpServletRequest request, final HttpSession session) {

        final SubmissionApi submissionApi = Objects.requireNonNull(getSubmission(id));

        String result;

        if (!verifySubmission(submissionApi)) {
            result = ViewConstants.ERROR.asView();
        } else if (submissionApi.getStatus() != SubmissionStatus.OPEN) {
            result = ViewConstants.GONE.asView();
        } else {
            paymentRequiredAttribute.setSubmissionId(id);
            paymentRequiredAttribute.setPaymentReference(submissionApi.getPaymentReference());

            final String fee = formTemplateAttribute.getFee();
            final BigDecimal feeAmount = StringUtils.isNotBlank(fee)
                    ? new BigDecimal(fee)
                    : BigDecimal.ZERO;

            if (BigDecimal.ZERO.compareTo(feeAmount) == 0) {
                result = ViewConstants.CHECK_DETAILS.asRedirectUri(chsUrl, id, companyNumber);
            } else {
                paymentRequiredAttribute.setFeeAmount(feeAmount);
                result = ViewConstants.PAYMENT_REQUIRED.asView();
            }
        }

        addTrackingAttributeToModel(model);

        return result;
    }

    @Override
    public String postPaymentReference(@PathVariable String id, @PathVariable String companyNumber,
                                       @Valid @ModelAttribute(ATTRIBUTE_NAME) final PaymentRequiredModel paymentRequiredModel,
                                       final BindingResult binding, final Model model, final HttpServletRequest request, final HttpSession session) {

        if (binding.hasErrors()) {
            addTrackingAttributeToModel(model);
            return ViewConstants.PAYMENT_REQUIRED.asView();
        }
        final ApiResponse<SubmissionResponseApi> response = apiClientService.putPayment(id,
                new PaymentReferenceApi(paymentRequiredModel.getPaymentReference()));

        logApiResponse(response, "", "PUT /efs-submission-api/submission/" + id + "/payment");


        return ViewConstants.CHECK_DETAILS.asRedirectUri(chsUrl, id, companyNumber);
    }
}
