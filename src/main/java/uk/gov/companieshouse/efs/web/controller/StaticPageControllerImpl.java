package uk.gov.companieshouse.efs.web.controller;

import javax.servlet.ServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTemplateControllerImpl;
import uk.gov.companieshouse.efs.web.categorytemplates.model.CategoryTemplateModel;
import uk.gov.companieshouse.logging.Logger;

/**
 * Handles the HTTP requests for the web application.
 */
@Controller
@RequestMapping(BaseControllerImpl.SERVICE_URI)
@SessionAttributes(CategoryTemplateControllerImpl.ATTRIBUTE_NAME)
public class StaticPageControllerImpl extends BaseControllerImpl implements StaticPageController {

    /**
     * Constructor.
     *
     * @param logger           the CH logger
     */
    @Autowired
    public StaticPageControllerImpl(final Logger logger) {
        super(logger);
    }

    @Override
    public String start(@ModelAttribute CategoryTemplateModel categoryTemplateAttribute, Model model, ServletRequest servletRequest, SessionStatus sessionStatus) {
        sessionStatus.setComplete(); // invalidate the user's previous session if they have signed out
        model.addAttribute(TEMPLATE_NAME, ViewConstants.START.asView());

        return ViewConstants.START.asView();
    }

    @Override
    public String guidance(Model model, ServletRequest servletRequest) {
        model.addAttribute(TEMPLATE_NAME, ViewConstants.GUIDANCE.asView());

        return ViewConstants.GUIDANCE.asView();
    }

    @Override
    public String insolvencyGuidance(Model model, ServletRequest servletRequest) {
        model.addAttribute(TEMPLATE_NAME, ViewConstants.INSOLVENCY_GUIDANCE.asView());

        return ViewConstants.INSOLVENCY_GUIDANCE.asView();
    }

    @Override
    public String accessibilityStatement(Model model, ServletRequest servletRequest) {

        model.addAttribute(TEMPLATE_NAME, ViewConstants.ACCESSIBILITY.asView());

        return ViewConstants.ACCESSIBILITY.asView();
    }

    @Override
    public String companyLookup(final String id, final Model model, final ServletRequest servletRequest,
        final RedirectAttributes attributes) {
        attributes.addAttribute("forward", String.format("/efs-submission/%s/company/{companyNumber}/details", id));

        return UrlBasedViewResolver.REDIRECT_URL_PREFIX + chsUrl + "/company-lookup/search";
    }

}