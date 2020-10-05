package uk.gov.companieshouse.efs.web.controller;

import javax.servlet.ServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.companieshouse.efs.web.categorytemplates.model.CategoryTemplateModel;

/**
 * Handles the HTTP requests for the web application, static pages (general).
 */
public interface StaticPageController {

    /**
     * Directs user to the start page.
     *
     * @param model the start page model
     * @param servletRequest contains the chs session id
     * @param sessionStatus the Spring Web session status
     * @return page to be displayed
     */
    @GetMapping("/start")
    String start(@ModelAttribute CategoryTemplateModel categoryTemplateAttribute, Model model, ServletRequest servletRequest, SessionStatus sessionStatus);

    /**
     * Directs user to the guidance page.
     *
     * @param model the guidance page model
     * @param request contains the chs session id
     * @return page to be displayed
     */
    @GetMapping("/guidance")
    String guidance(Model model, ServletRequest request);

    /**
     * Directs user to the accessibility statement page.
     *
     * @param model   the accessibility statement model
     * @param request contains the chs session id
     * @return page to be displayed
     */
    @GetMapping("/accessibility-statement")
    String accessibilityStatement(Model model, ServletRequest request);

    /**
     * Directs user to company lookup service and sets the return URL to COMPANY_DETAIL view.
     */
    @GetMapping("{id}/companyLookup")
    String companyLookup(@PathVariable String id, Model model, ServletRequest servletRequest,
        RedirectAttributes attributes);
}