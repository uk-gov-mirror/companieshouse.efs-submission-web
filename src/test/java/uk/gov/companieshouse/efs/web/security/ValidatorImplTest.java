package uk.gov.companieshouse.efs.web.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ValidatorImplTest {
    private TestValidatorImpl testValidator;

    @Mock
    private Validator nextValidator;

    @BeforeEach
    void setUp() {
        testValidator = new TestValidatorImpl();
    }

    @Test
    void validateWhenHasNextValidator() {
        testValidator.setNext(nextValidator);
        testValidator.validate();

        verify(nextValidator).validate();
    }

    @Test
    void validateWhenNoNextValidator() {
        testValidator.setNext(null);
        testValidator.validate();

        assertTrue(testValidator.validate());
    }

    static class TestValidatorImpl extends ValidatorImpl {
        public TestValidatorImpl() {
            super(null);
        }

        @Override
        public boolean isValid() {
            return true;
        }
    }
}