package uk.gov.companieshouse.efs.web.security.validator;

import org.springframework.beans.factory.annotation.Value;
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
    private static final Pattern LEGACY_AUTH_COMPANY_SCOPE = Pattern.compile("/company/(?<companyNumber>[0-9a-zA-Z]{8}+)$");
    private static final Pattern FINE_GRAINED_AUTH_COMPANY_SCOPE = Pattern.compile("/company/(?<companyNumber>[0-9a-zA-Z]{8}+)/admin.write-full$");

    private final CategoryTemplateService categoryTemplateService;

    @Value("${auth.use.fine.grained.scope}")
    private String useFineGrainedScope;

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

        boolean previouslyAuthorised = resourceProvider.getSignInInfo()
                .map(SignInInfo::getCompanyNumber)
                .map(companyNumber::equalsIgnoreCase)
                .orElse(false);

        if (previouslyAuthorised) {
            return true;
        }

        return getUserScopes().stream()
                .map(getAuthCompanyScopePattern()::matcher)
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

    private Pattern getAuthCompanyScopePattern() {
        if (isUseFineGrainedScope()) {
            return FINE_GRAINED_AUTH_COMPANY_SCOPE;
        } else {
            return LEGACY_AUTH_COMPANY_SCOPE;
        }
    }

    private boolean isUseFineGrainedScope() {
        return "1".equalsIgnoreCase(useFineGrainedScope);
    }
}
