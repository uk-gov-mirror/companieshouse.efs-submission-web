package uk.gov.companieshouse.efs.web.categorytemplates.controller;

import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTemplateControllerImpl.ATTRIBUTE_NAME;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.companieshouse.efs.web.categorytemplates.model.CategoryTemplateModel;
import uk.gov.companieshouse.efs.web.controller.BaseControllerImpl;

@RequestMapping(BaseControllerImpl.SERVICE_URI)
public interface CategoryTemplateController {

    @GetMapping(value = {"{id}/company/{companyNumber}/category-selection"})
    String categoryTemplate(@PathVariable String id, @PathVariable String companyNumber,
        @RequestParam(value = "category", required = false) List<String> categoryIdList,
        @ModelAttribute(ATTRIBUTE_NAME) CategoryTemplateModel categoryTemplateAttribute, Model model,
        HttpServletRequest servletRequest);

    @PostMapping(value = {"{id}/company/{companyNumber}/category-selection"}, params = {"action=submit"})
    String postCategoryTemplate(@PathVariable String id, @PathVariable String companyNumber,
        @ModelAttribute(ATTRIBUTE_NAME) CategoryTemplateModel categoryTemplateAttribute, BindingResult binding,
        Model model, HttpServletRequest servletRequest);
}
