package uk.gov.companieshouse.efs.web.util;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import java.util.Properties;
import java.util.concurrent.Callable;


public class IntegrationTestHelper {

    public static <T> T runWithEnvironment(final Callable<T> callable)
            throws Exception {
        return withSpringEnvironment()
                .execute(callable);
    }

    public static SystemLambda.WithEnvironmentVariables withSpringEnvironment() {
        return withEnvironmentVariable("API_URL", "http://api.chs.local:4001")
                .and("COOKIE_DOMAIN", "chs.local")
                .and("COOKIE_NAME", "__SID")
                .and("COOKIE_SECRET", "cookie_secret")
                .and("DEFAULT_SESSION_EXPIRATION", "3600")
                .and("INTERNAL_API_URL", "http://api.chs.local:4001")
                .and("OAUTH2_CLIENT_ID", "oath2_client_id")
                .and("OAUTH2_CLIENT_SECRET", "oath2_client_secret")
                .and("OAUTH2_AUTH_URI", "http://account.chs.local/oauth2/authorise")
                .and("OAUTH2_TOKEN_URI", "http://account.chs.local/oauth2/token")
                .and("OAUTH2_REDIRECT_URI", "http://chs.local/oauth2/user/callback")
                .and("OAUTH2_REQUEST_KEY", "oauth2_request_key")
                .and("PAYMENTS_API_URL", "http://api-payments.chs.local:4001")
                ;
    }
}
