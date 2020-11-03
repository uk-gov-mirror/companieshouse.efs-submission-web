package uk.gov.companieshouse.efs.web.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ValidatorImplTest {
    private TestValidatorImpl testValidator;

    @Mock
    private Validator nextValidator;

    @Test
    void validateWhenHasNextValidator() {
        testValidator = new TestValidatorImpl(true);
        testValidator.setNext(nextValidator);
        testValidator.validate();

        verify(nextValidator).validate();
    }

    @Test
    void dontCallNextValidatorWhenValidationFails() {
        testValidator = new TestValidatorImpl(false);
        testValidator.setNext(nextValidator);
        testValidator.validate();

        verify(nextValidator, never()).validate();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void validateWhenNoNextValidator(boolean isValid) {
        testValidator = new TestValidatorImpl(isValid);
        testValidator.setNext(null);
        testValidator.validate();

        assertEquals(isValid, testValidator.validate());
    }

    @Test
    void setNextAppendsToLast() {
        Validator validator1 = spy(new TestValidatorImpl(true));
        Validator validator2 = spy(new TestValidatorImpl(true));

        testValidator = new TestValidatorImpl(true);
        Validator tv = testValidator
                .setNext(validator1)
                .setNext(validator2);


        assertThat(tv, sameInstance(testValidator));

        tv.validate();

        verify(validator1).validate();
        verify(validator1).setNext(validator2);
        verify(validator2).validate();
    }

    static class TestValidatorImpl extends ValidatorImpl {
        private final boolean returns;

        public TestValidatorImpl(boolean returns) {
            super(null);
            this.returns = returns;
        }

        @Override
        public boolean isValid() {
            return returns;
        }
    }
}