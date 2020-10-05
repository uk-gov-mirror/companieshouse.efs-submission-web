package uk.gov.companieshouse.efs.web.controller;

import java.util.Objects;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.web.categorytemplates.model.CategoryTemplateModel;
import uk.gov.companieshouse.efs.web.categorytemplates.service.api.CategoryTemplateService;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.efs.web.service.session.SessionService;
import uk.gov.companieshouse.logging.Logger;

@Controller
public class ResolutionsInfoControllerImpl extends BaseControllerImpl implements ResolutionsInfoController {


    /**
     * Constructor used by child controllers.
     *
     */
    public ResolutionsInfoControllerImpl(final Logger logger, final SessionService sessionService,
        final ApiClientService apiClientService, final FormTemplateService formTemplateService,
        final CategoryTemplateService categoryTemplateService) {
        super(logger, sessionService, apiClientService, formTemplateService,
            categoryTemplateService);
    }

    @Override
    public String getViewName() {
        return ViewConstants.RESOLUTIONS_INFO.asView();
    }

    @Override
    public String resolutionsInfo(@PathVariable String id, @PathVariable String companyNumber,
        CategoryTemplateModel categoryTemplateAttribute, Model model, HttpServletRequest servletRequest) {

        final SubmissionApi submissionApi = Objects.requireNonNull(getSubmission(id));
        categoryTemplateAttribute.setSubmissionId(submissionApi.getId());

        if (submissionApi.getStatus() != SubmissionStatus.OPEN) {
            return ViewConstants.GONE.asView();
        }

        addTrackingAttributeToModel(model);

        return ViewConstants.RESOLUTIONS_INFO.asView();
    }

    @Override
    public String postResolutionsInfo(@PathVariable String id, @PathVariable String companyNumber,
        CategoryTemplateModel categoryTemplateAttribute, BindingResult binding, Model model, ServletRequest servletRequest, HttpSession session) {

        return ViewConstants.DOCUMENT_UPLOAD.asRedirectUri(chsUrl, id, companyNumber);
    }
}