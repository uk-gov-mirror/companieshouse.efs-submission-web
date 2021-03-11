package uk.gov.companieshouse.efs.web.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.UrlBasedViewResolver;


@ExtendWith(MockitoExtension.class)
class StaticPageControllerImplTest extends BaseControllerImplTest {

    protected static final String COMPANY_SEARCH_REDIRECT =
        UrlBasedViewResolver.REDIRECT_URL_PREFIX + CHS_URL + "/company-lookup/search";
    private StaticPageController testController;

    @Mock
    private RedirectAttributes attributes;

    @BeforeEach
    protected void setUp() {
        setUpHeaders();
        testController = new StaticPageControllerImpl(logger);
        ((StaticPageControllerImpl) testController).setChsUrl(CHS_URL);
    }

    @Test
    void startPage() {
        assertThat(testController.start(categoryTemplateAttribute, model, servletRequest, sessionStatus), is(ViewConstants.START.asView()));
    }

    @Test
    void guidancePage() {
        assertThat(testController.guidance(model, servletRequest), is(ViewConstants.GUIDANCE.asView()));
    }

    @Test
    void insolvencyGuidancePage() {
        assertThat(testController.insolvencyGuidance(model, servletRequest), is(ViewConstants.INSOLVENCY_GUIDANCE.asView()));
    }

    @Test
    void accessibilityStatementPage() {
        assertThat(testController.accessibilityStatement(model, servletRequest),
            is(ViewConstants.ACCESSIBILITY.asView()));
    }

    @Test
    void companyLookup() {
        final String view = testController.companyLookup(SUBMISSION_ID, model, servletRequest, attributes);

        verify(attributes)
            .addAttribute("forward", "/efs-submission/" + SUBMISSION_ID + "/company/{companyNumber}/details");
        assertThat(view, is(COMPANY_SEARCH_REDIRECT));
    }

}
