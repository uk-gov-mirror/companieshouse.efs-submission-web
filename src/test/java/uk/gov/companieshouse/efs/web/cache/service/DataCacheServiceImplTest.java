package uk.gov.companieshouse.efs.web.cache.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class DataCacheServiceImplTest {

    private DataCacheService testService;

    @Mock
    private Cache cache;
    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        testService = new DataCacheServiceImpl(logger);
    }


    @Test
    void clearAllCategories() {
        testService.clearAllCaches();

        verify(logger).debug(anyString());
    }

}