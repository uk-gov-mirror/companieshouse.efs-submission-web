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
    public static final String IP_ALLOW_LIST = "ip-allow-list";
    public static final String SUBMISSION_BY_ID = "submission-by-id";

    // although this method is unused it prevents sonar considering this a utility class
    // with a public constructor
    @Bean
    public List<String> cacheNames() {
        return Arrays.asList(ALL_CATEGORIES, CATEGORY_BY_ID, CATEGORY_BY_PARENT, TOP_LEVEL_CATEGORY, IP_ALLOW_LIST, SUBMISSION_BY_ID);
    }
}
