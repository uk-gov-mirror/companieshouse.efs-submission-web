package uk.gov.companieshouse.efs.web.controller;

import static uk.gov.companieshouse.efs.web.controller.CompanyDetailControllerImpl.ATTRIBUTE_NAME;

import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTemplateControllerImpl;
import uk.gov.companieshouse.efs.web.categorytemplates.model.CategoryTemplateModel;

@RequestMapping(BaseControllerImpl.SERVICE_URI)
@SessionAttributes(ATTRIBUTE_NAME)
@SuppressWarnings("squid:S3753")
/* S3753: "@Controller" classes that use "@SessionAttributes" must call "setComplete" on their "SessionStatus" objects
 *
 * The nature of the web journey across several controllers means it's not appropriate to do this. However,
 * setComplete() is properly called in ConfirmationControllerImpl at the end of the submission journey.
 */
public interface RegistrationsInfoController {

    /**
     * Get request for the category template.
     *
     * @param categoryTemplateAttribute the category template details see {@link CategoryTemplateModel}
     * @param model                     the category model
     * @param servletRequest            contains the chs session id
     * @return the view name
     */
    @GetMapping(value = {"{id}/company/{companyNumber}/registrations-info"})
    String registrationsInfo(@PathVariable String id, @PathVariable String companyNumber,
        @ModelAttribute(CategoryTemplateControllerImpl.ATTRIBUTE_NAME)
            CategoryTemplateModel categoryTemplateAttribute, Model model,
        HttpServletRequest servletRequest);

}