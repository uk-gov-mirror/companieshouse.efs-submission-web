package uk.gov.companieshouse.efs.web.controller;

import static uk.gov.companieshouse.efs.web.controller.RegisteredCompanyControllerImpl.ATTRIBUTE_NAME;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.companieshouse.efs.web.model.RegisteredCompanyModel;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.efs.web.service.session.SessionService;
import uk.gov.companieshouse.logging.Logger;

@Controller
@SessionAttributes(ATTRIBUTE_NAME)
@SuppressWarnings("squid:S3753")
/* S3753: "@Controller" classes that use "@SessionAttributes" must call "setComplete" on their "SessionStatus" objects
 *
 * The nature of the web journey across several controllers means it's not appropriate to do this. However,
 * setComplete() is properly called in ConfirmationControllerImpl at the end of the submission journey.
 */
public class RegisteredCompanyControllerImpl extends BaseControllerImpl implements RegisteredCompanyController {

    /**
     * Define the model name for this action.
     */
    public static final String ATTRIBUTE_NAME = "registeredCompany";

    private RegisteredCompanyModel registeredCompanyAttribute;

    @Autowired
    public RegisteredCompanyControllerImpl(final Logger logger, final SessionService sessionService,
        final ApiClientService apiClientService, final RegisteredCompanyModel registeredCompanyAttribute) {
        super(logger, sessionService, apiClientService);

        this.registeredCompanyAttribute = registeredCompanyAttribute;
    }

    @ModelAttribute(ATTRIBUTE_NAME)
    public RegisteredCompanyModel getModel() {
        return registeredCompanyAttribute;
    }

    @Override
    public String getViewName() {
        return ViewConstants.REGISTERED_COMPANY.asView();
    }

    @Override
    public String prepare(@ModelAttribute(ATTRIBUTE_NAME)
        RegisteredCompanyModel registeredCompanyModel, Model model,
        HttpServletRequest request) {

        // Assign our previously saved response to our model.
        registeredCompanyModel.setRegistered(registeredCompanyAttribute.getRegistered());

        return getViewName();
    }

    @Override
    public String process(@Valid @ModelAttribute(ATTRIBUTE_NAME) RegisteredCompanyModel registeredCompanyModel,
        BindingResult binding, Model model, HttpServletRequest request, HttpSession session) {

        if (binding.hasErrors()) {
            return getViewName();
        }

        // Update our persistent model with the latest response.
        registeredCompanyAttribute.setRegistered(registeredCompanyModel.getRegistered());

        String targetURL = ViewConstants.PROPOSED_COMPANY.asRedirectUri(chsUrl);

        if(registeredCompanyAttribute.getRegistered().equals("Y")) {
            targetURL = "redirect:/efs-submission/new-company-submission";
        }

        return targetURL;
    }
}
