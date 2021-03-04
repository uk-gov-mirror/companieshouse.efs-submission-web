package uk.gov.companieshouse.efs.web.configuration;

import java.util.Arrays;
import java.util.List;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Profile("data-cache")
@EnableCaching
@EnableScheduling
@Configuration
public class DataCacheConfig {
    public static final String ALL_CATEGORIES = "all-categories";
    public static final String CATEGORY_BY_ID = "category-by-id";
    public static final String CATEGORY_BY_PARENT = "category-by-parent";
    public static final String TOP_LEVEL_CATEGORY = "top-level-category";
    public static final String ALL_FORMS = "all-forms";
    public static final String FORM_BY_ID = "form-by-id";
    public static final String FORM_BY_CATEGORY = "form-by-category";
    public static final String IP_ALLOW_LIST = "ip-allow-list";
    public static final String SUBMISSION_BY_ID = "submission-by-id";

    protected static final String[] REF_DATA_CACHE_NAMES =
            {ALL_CATEGORIES, CATEGORY_BY_ID, CATEGORY_BY_PARENT, TOP_LEVEL_CATEGORY, ALL_FORMS,
                    FORM_BY_ID, FORM_BY_CATEGORY, IP_ALLOW_LIST};
    // although this method is unused it prevents sonar considering this a utility class
    // with a public constructor
    @Bean
    public List<String> refDataCacheNames() {
        return Arrays.asList(REF_DATA_CACHE_NAMES);
    }
}
