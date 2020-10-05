package uk.gov.companieshouse.efs.web.controller;

import java.text.MessageFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Contains the redirect urls for the service.
 */
public enum ViewConstants {
    START("start", "start"),
    GUIDANCE("guidance", "guidance"),
    NEW_SUBMISSION("new-submission", null),
    COMPANY_LOOKUP("companyLookup", null),
    COMPANY_DETAIL("details", "companyDetail"),
    CATEGORY_SELECTION("category-selection", "categorySelection"),
    DOCUMENT_SELECTION("document-selection", "documentSelection"),
    RESOLUTIONS_INFO("resolutions-info", "resolutionsInfo"),
    ACCESSIBILITY("accessibilityStatement", "accessibilityStatement"),
    DOCUMENT_UPLOAD("document-upload", "documentUpload"),
    REMOVE_DOCUMENT("removeDocument", "removeDocument"),
    PAYMENT_REQUIRED("payment-required", "paymentRequired"),
    CHECK_DETAILS("check-your-details", "checkDetails"),
    CONFIRMATION("confirmation", "confirmation"),
    ERROR("error", "error"),
    MISSING("error/404", "error/404"),
    GONE("/error/410", "error/410");

    private static final String SUBMISSION = "/efs-submission/";
    private static final String COMPANY = "company/{0}";
    private static final String PAGE = "{0}" + SUBMISSION + "{1}";
    private static final String ID_PAGE = PAGE + "/{2}";
    private static final String ID_PAGE_AUTH = ID_PAGE + "/{3}";
    private static final String REDIRECT = "redirect:{0}";

    private static final String CATEGORY_TEMPLATE = "category={category}";

    private final String template;
    private final String uri;

    @Value("${chs.url}")
    private String chsUrl;

    ViewConstants(String uri, String template) {
        this.uri = uri;
        this.template = template;
    }

    /**
     * Get name of view.
     *
     * @return the url of the view to display
     */
    public String asView() {
        return this.template;
    }

    /**
     * Get redirect url of the view.
     *
     * @param chsUrl the CHS URL prefix host[:port]
     * @param id the submission ID
     * @return the redirect url of the view to display (with ID id)
     */
    public String asRedirectUri(final String chsUrl, final String id) {
        return MessageFormat.format(REDIRECT, asUri(id, chsUrl), "redirect:");
    }

    /**
     * Get redirect url of the view.
     *
     * @param chsUrl the CHS URL prefix host[:port]
     * @return the redirect url of the view to display (without ID)
     */
    public String asRedirectUri(final String chsUrl) {
        return MessageFormat.format(REDIRECT, asUri(chsUrl), "redirect:");
    }

    public String asRedirectUri(final String chsUrl, final String id, final String companyNumber) {
        return MessageFormat.format(REDIRECT, asUri(chsUrl, id, companyNumber));
    }

    public String asRedirectUri(final String chsUrl, final String id, final String companyNumber, final String category) {
        return MessageFormat.format(REDIRECT, asUri(chsUrl, id, companyNumber, category));
    }

    public String asUri(final String id, final String chsUrl) {
        return MessageFormat.format(ID_PAGE, chsUrl, id, this.uri);
    }

    public String asUri(final String chsUrl, final String id, final String companyNumber) {
        return MessageFormat.format(ID_PAGE_AUTH, chsUrl, id,
            MessageFormat.format(COMPANY, companyNumber), this.uri);
    }

    /**
     * Build form category view as a URI with query parameter encoding to support special characters like "&amp;".
     *
     * @param chsUrl        the chs host url
     * @param id            the submission ID
     * @param companyNumber the company number
     * @param formCategory  the form category ID
     * @return the encoded URI
     */
    public String asUri(final String chsUrl, final String id, final String companyNumber, final String formCategory) {

        final String url = MessageFormat.format(ID_PAGE_AUTH, chsUrl, id,
            MessageFormat.format(COMPANY, companyNumber), this.uri);
        final UriComponents components = UriComponentsBuilder.fromHttpUrl(url).query(CATEGORY_TEMPLATE).buildAndExpand(
            formCategory).encode();

        return components.toUriString();
    }

    public String asUri(final String chsUrl) {
        return MessageFormat.format(PAGE, chsUrl, this.uri);
    }

    public String asUriForCompany(final String chsUrl, final String companyNumber) {

        return MessageFormat.format(ID_PAGE, chsUrl, MessageFormat.format(COMPANY, companyNumber), this.uri);
    }

}