package uk.gov.companieshouse.efs.web.configuration;

import org.springframework.cache.annotation.EnableCaching;
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
}
