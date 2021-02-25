package uk.gov.companieshouse.efs.web.security;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.client.utils.URIBuilder;
import uk.gov.companieshouse.auth.filter.UserAuthFilter;
import uk.gov.companieshouse.efs.web.exception.ServiceException;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.session.Session;

/**
 * Manages user authentication.
 */
public final class LoggingAuthFilter extends UserAuthFilter {
    private static final String CONFIRMATION_FRAGMENT = "confirmation";
    protected final String signoutRestartPath;

    /**
     * constructor sets the signout redirect path
     *
     * @param signoutRedirectPath the redirect path to use after sign in instead of the original path
     *                            (only if the user is signing out or has signed out)
     */
    public LoggingAuthFilter(final String signoutRedirectPath) {
        this.signoutRestartPath = signoutRedirectPath;
    }

    public LoggingAuthFilter(EnvironmentReader environmentReader, String signoutRestartPath) {
        super(environmentReader);
        this.signoutRestartPath = signoutRestartPath;
    }

    @Override
    protected void redirectForAuth(final Session session, final HttpServletRequest request,
        final HttpServletResponse response, final String companyNumber, final boolean force) throws IOException {
        log.debug("originalRequestUrl=" + request.getRequestURL());
        super.redirectForAuth(session, request, response, companyNumber, force);
    }

    @Override
    protected String createAuthoriseURI(final String originalRequestUri, final String scope, final String nonce) {
        final URIBuilder uriBuilder;
        final Map<String, Object> map = new HashMap<>();

        map.put("originalRequestUrl", originalRequestUri);
        try {
            uriBuilder = new URIBuilder(originalRequestUri);
        }
        catch (URISyntaxException ex) {
            log.error("Invalid originalRequestUri", map);
            throw new ServiceException("Invalid originalRequestUri", ex);
        }

        final String redirectUri = uriBuilder.getPathSegments().contains(CONFIRMATION_FRAGMENT)
            ? signoutRestartPath
            : originalRequestUri;
        final String authoriseUri = super.createAuthoriseURI(redirectUri, scope, nonce);

        map.put("redirectUri", redirectUri);
        map.put("authoriseUri", authoriseUri);
        log.debug("Building Authorise URI...", map);

        return authoriseUri;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final LoggingAuthFilter that = (LoggingAuthFilter) o;
        return Objects.equals(signoutRestartPath, that.signoutRestartPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signoutRestartPath);
    }
}
