package uk.gov.companieshouse.efs.web.formtemplates.controller;

import static uk.gov.companieshouse.efs.web.formtemplates.controller.FormTemplateControllerImpl.ATTRIBUTE_NAME;

import javax.servlet.ServletRequest;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTemplateControllerImpl;
import uk.gov.companieshouse.efs.web.categorytemplates.model.CategoryTemplateModel;
import uk.gov.companieshouse.efs.web.controller.BaseControllerImpl;
import uk.gov.companieshouse.efs.web.formtemplates.model.FormTemplateModel;

@RequestMapping(BaseControllerImpl.SERVICE_URI)
public interface FormTemplateController {

    @GetMapping(value = {"{id}/company/{companyNumber}/document-selection"})
    String formTemplate(@PathVariable String id, @PathVariable String companyNumber,
            @RequestParam("category") String category,
            @SessionAttribute(CategoryTemplateControllerImpl.ATTRIBUTE_NAME)
                    CategoryTemplateModel categoryTemplateAttribute,
            @ModelAttribute(ATTRIBUTE_NAME) FormTemplateModel formTemplateAttribute, Model model,
            ServletRequest servletRequest);

    @PostMapping(value = {"{id}/company/{companyNumber}/document-selection"},
            params = {"action=submit"})
    String postFormTemplate(@PathVariable String id, @PathVariable String companyNumber,
            @SessionAttribute(CategoryTemplateControllerImpl.ATTRIBUTE_NAME)
                    CategoryTemplateModel categoryTemplateAttribute,
            @ModelAttribute(ATTRIBUTE_NAME) FormTemplateModel formTemplateAttribute,
            BindingResult binding, Model model, ServletRequest servletRequest);
}
