package uk.gov.companieshouse.efs.web.payment.service;

import org.springframework.beans.factory.FactoryBean;

/**
 * Produces a NonceServiceImpl bean
 */
public class NonceServiceFactoryImpl implements FactoryBean<NonceService> {

    @Override
    public NonceService getObject() throws Exception {
        return new NonceServiceImpl();
    }

    @Override
    public Class<?> getObjectType() {
        return NonceServiceImpl.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
