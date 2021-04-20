package uk.gov.companieshouse.efs.web.controller;

import static uk.gov.companieshouse.efs.web.controller.ProposedCompanyControllerImpl.ATTRIBUTE_NAME;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.companieshouse.efs.web.model.ProposedCompanyModel;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.efs.web.service.session.SessionService;
import uk.gov.companieshouse.logging.Logger;

@Controller
@SessionAttributes(ATTRIBUTE_NAME)
public class ProposedCompanyControllerImpl extends BaseControllerImpl implements ProposedCompanyController {

    /**
     * Define the model name for this action.
     */
    public static final String ATTRIBUTE_NAME = "proposedCompany";
    public static final String TEMP_COMPANY_NUMBER = "99999999";
    
    private ProposedCompanyModel proposedCompanyAttribute;

    @Autowired
    public ProposedCompanyControllerImpl(final Logger logger, final SessionService sessionService,
        final ApiClientService apiClientService, final ProposedCompanyModel proposedCompanyAttribute) {
        super(logger, sessionService, apiClientService);

        this.proposedCompanyAttribute = proposedCompanyAttribute;
    }

    @ModelAttribute(ATTRIBUTE_NAME)
    public ProposedCompanyModel getModel() {
        return proposedCompanyAttribute;
    }

    @Override
    public String getViewName() {
        return ViewConstants.PROPOSED_COMPANY.asView();
    }

    @Override
    public String prepare(@PathVariable final String id,
        @ModelAttribute(ATTRIBUTE_NAME) ProposedCompanyModel proposedCompanyModel, Model model,
        HttpServletRequest request) {

        // Assign our previously saved response to our model.
        proposedCompanyModel.setSubmissionId(id);
        proposedCompanyModel.setName(proposedCompanyAttribute.getName());

        return getViewName();
    }

    @Override
    public String process(@PathVariable final String id,
        @Valid @ModelAttribute(ATTRIBUTE_NAME) ProposedCompanyModel proposedCompanyModel,
        BindingResult binding, Model model, HttpServletRequest request, HttpSession session) {

        if (binding.hasErrors()) {
            return getViewName();
        }

        // Update our persistent model with the latest response.
        proposedCompanyAttribute.setName(proposedCompanyModel.getName());

        return ViewConstants.CATEGORY_SELECTION.asRedirectUri(chsUrl, id, TEMP_COMPANY_NUMBER);
    }
}