package uk.gov.companieshouse.efs.web.payment.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NonceServiceFactoryImplTest {
    private NonceServiceFactoryImpl testFactory;

    @BeforeEach
    void setUp() {
        testFactory = new NonceServiceFactoryImpl();
    }

    @Test
    void getObject() throws Exception {
        assertThat(testFactory.getObject(), isA(NonceService.class));
    }

    @Test
    void getObjectType() {
        assertThat(testFactory.getObjectType(), is(NonceServiceImpl.class));
    }

    @Test
    void isSingleton() {
        assertThat(testFactory.isSingleton(), is(true));
    }
}