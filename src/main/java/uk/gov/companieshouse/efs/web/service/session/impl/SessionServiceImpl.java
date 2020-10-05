package uk.gov.companieshouse.efs.web.service.session.impl;

import java.util.Map;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.efs.web.service.session.SessionService;
import uk.gov.companieshouse.session.handler.SessionHandler;

/**
 * Used to retrieve data from the current session.
 */
@Service
public class SessionServiceImpl implements SessionService {
    private static final String SIGN_IN_KEY = "signin_info";
    private static final String USER_PROFILE_KEY = "user_profile";
    private static final String EMAIL_KEY = "email";

    @Override
    public Map<String, Object> getSessionDataFromContext() {
        return SessionHandler.getSessionDataFromContext();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getUserInfo() {
        Map<String, Object> sessionData = getSessionDataFromContext();
        Map<String, Object> signInInfo = (Map<String, Object>) sessionData.get(SIGN_IN_KEY);

        return signInInfo != null ? (Map<String, Object>) signInInfo.get(USER_PROFILE_KEY) : null;
    }

    @Override
    public String getUserEmail() {
        final Map<String, Object> userInfo = getUserInfo();

        return (String) userInfo.get(EMAIL_KEY);
    }
}
