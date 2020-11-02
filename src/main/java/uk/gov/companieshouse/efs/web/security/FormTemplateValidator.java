package uk.gov.companieshouse.efs.web.security;


import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants;
import uk.gov.companieshouse.efs.web.categorytemplates.service.api.CategoryTemplateService;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.session.model.SignInInfo;
import uk.gov.companieshouse.session.model.UserProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants.INSOLVENCY;

public class FormTemplateValidator extends ValidatorImpl
        implements Validator {

    private static final List<CategoryTypeConstants> categoriesWithAllowLists = Collections
            .singletonList(INSOLVENCY);
    private static final String COMPANY_NUMBER_GROUP = "companyNumber";
    private static final Pattern AUTH_COMPANY_SCOPE = Pattern.compile("/company/(?<" +
            COMPANY_NUMBER_GROUP + ">[0-9a-zA-Z]*)$");

    private final CategoryTemplateService categoryTemplateService;

    public FormTemplateValidator(ValidatorResourceProvider resourceProvider,
                                 CategoryTemplateService categoryTemplateService) {
        super(resourceProvider);
        this.categoryTemplateService = categoryTemplateService;
    }

    @Override
    public boolean isValid() {
        return isAuthRequired() && (
                isAuthorisedForCompany() ||
                        isOnAllowList());
    }

    private boolean isAuthRequired() {
        return resourceProvider.getForm()
                .map(FormTemplateApi::isAuthenticationRequired)
                .orElse(false);    // If the user hasn't progressed far enough to have a form then
                                        // this returns false
    }

    private boolean isOnAllowList() {
        Optional<CategoryTypeConstants> maybeTopLevelCategory = resourceProvider.getForm()
                .map(FormTemplateApi::getFormCategory)
                .map(categoryTemplateService::getTopLevelCategory);

        if (!maybeTopLevelCategory.isPresent()) {
            // Don't know what form. Can't be on allow list
            return false;
        }

        CategoryTypeConstants topLevelCategory = maybeTopLevelCategory.get();

        if (!categoriesWithAllowLists.contains(topLevelCategory)) {
            // If category doesn't have an allow list then the user can't be on it
            return false;
        }

        ApiClientService apiClientService = resourceProvider.getApiClientService();
        return resourceProvider.getSignInInfo()
                .map(SignInInfo::getUserProfile)
                .map(UserProfile::getEmail)
                .map(apiClientService::isOnAllowList)
                .map(ApiResponse::getData)
                .orElse(false);
    }

    private boolean isAuthorisedForCompany() {
        Optional<String> maybeCompanyNumber = resourceProvider.getCompanyNumber();
        if (!maybeCompanyNumber.isPresent()) {
            return false;
        }

        String companyNumber = maybeCompanyNumber.get();

        return getUserScopes().stream()
                .map(AUTH_COMPANY_SCOPE::matcher)
                .filter(Matcher::find)
                .map(m -> m.group(COMPANY_NUMBER_GROUP))
                .anyMatch(companyNumber::equalsIgnoreCase);
    }

    private List<String> getUserScopes() {
        return resourceProvider.getSignInInfo()
                .map(SignInInfo::getUserProfile)
                .map(UserProfile::getScope)
                .map(scopesStr -> scopesStr.split(" "))
                .map(Arrays::asList)
                .orElseGet(ArrayList::new);
    }
}
