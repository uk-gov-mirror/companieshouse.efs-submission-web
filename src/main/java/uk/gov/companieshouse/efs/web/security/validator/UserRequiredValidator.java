package uk.gov.companieshouse.efs.web.security.validator;

import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants;
import uk.gov.companieshouse.efs.web.categorytemplates.service.api.CategoryTemplateService;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.session.model.SignInInfo;
import uk.gov.companieshouse.session.model.UserProfile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants.INSOLVENCY;

/**
 * Validates if a user is authorised for the form attached to the submission or if they are on the
 * allow list.
 */
public class UserRequiredValidator extends AuthRequiredValidator implements Validator<HttpServletRequest> {
    private static final List<CategoryTypeConstants> categoriesWithAllowLists = Collections
            .singletonList(INSOLVENCY);
    private static final String COMPANY_NUMBER_GROUP = "companyNumber";

    private static final String AUTH_SCOPE_PATTERN_STRING = String.format("/company/(?<%s>[0-9a-zA-Z]*)$",
            COMPANY_NUMBER_GROUP);
    private static final Pattern AUTH_COMPANY_SCOPE = Pattern.compile(AUTH_SCOPE_PATTERN_STRING);

    private final CategoryTemplateService categoryTemplateService;

    public UserRequiredValidator(ValidatorResourceProvider resourceProvider, CategoryTemplateService categoryTemplateService) {
        super(resourceProvider);
        this.categoryTemplateService = categoryTemplateService;
    }

    /**
     * Authorisation is required if the user isn't authorised for the form or on the allow list.
     *
     * @return true if they are not authorised
     */
    @Override
    protected boolean requiresAuth() {
        return !(isOnAllowList() || isAuthorisedForCompany());
    }

    // topLevelCategory == INSOLVENCY
    // userEmail is on allow list
    private boolean isOnAllowList() {
        boolean categoryHasAllowList = getTopLevelCategory()
                .filter(categoriesWithAllowLists::contains)
                .isPresent();

        // Can't be on the allow list if there isn't one
        if (!categoryHasAllowList) {
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

    private Optional<CategoryTypeConstants> getTopLevelCategory() {
        return resourceProvider.getForm()
                .map(FormTemplateApi::getFormCategory)
                .map(categoryTemplateService::getTopLevelCategory);
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
