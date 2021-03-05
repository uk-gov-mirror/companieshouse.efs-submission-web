package uk.gov.companieshouse.efs.web.cache.service;

import static uk.gov.companieshouse.efs.web.configuration.DataCacheConfig.ALL_CATEGORIES;
import static uk.gov.companieshouse.efs.web.configuration.DataCacheConfig.ALL_FORMS;
import static uk.gov.companieshouse.efs.web.configuration.DataCacheConfig.CATEGORY_BY_ID;
import static uk.gov.companieshouse.efs.web.configuration.DataCacheConfig.CATEGORY_BY_PARENT;
import static uk.gov.companieshouse.efs.web.configuration.DataCacheConfig.FORM_BY_CATEGORY;
import static uk.gov.companieshouse.efs.web.configuration.DataCacheConfig.FORM_BY_ID;
import static uk.gov.companieshouse.efs.web.configuration.DataCacheConfig.IP_ALLOW_LIST;
import static uk.gov.companieshouse.efs.web.configuration.DataCacheConfig.SUBMISSION_BY_ID;
import static uk.gov.companieshouse.efs.web.configuration.DataCacheConfig.TOP_LEVEL_CATEGORY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;

@Service
public class DataCacheServiceImpl implements DataCacheService {
    private Logger logger;

    @Autowired
    public DataCacheServiceImpl(final Logger logger) {
        this.logger = logger;
    }

    @Scheduled(fixedDelayString = "${cache.eviction.delay.ms}")
    // Include SUBMISSION_BY_ID cache to ensure to ensure old entries are cleared.
    // Replacing the Boot default with a configurable cache provider would enable
    // TTL configuration.
    @CacheEvict(cacheNames = {SUBMISSION_BY_ID, ALL_CATEGORIES, CATEGORY_BY_ID, CATEGORY_BY_PARENT,
            TOP_LEVEL_CATEGORY, ALL_FORMS, FORM_BY_ID, FORM_BY_CATEGORY, IP_ALLOW_LIST},
            allEntries = true)
    @Override
    public void clearAllCaches() {
        logger.debug("scheduled clearance of submission cache: complete");
        logger.debug("scheduled clearance of reference data caches: complete");
    }

}
