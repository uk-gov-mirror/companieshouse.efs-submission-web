package uk.gov.companieshouse.efs.web.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.CodeSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
class JoinPointHelperTest {
    @Mock
    JoinPoint joinPoint;

    @Mock
    CodeSignature codeSignature;

    @Mock
    Signature signature;

    JoinPointHelper helper;

    private static final String[] PARAM_NAMES = new String[] {
            "id", "companyNumber", "superSecretParameter" };

    private static final List<String> PARAMS_TO_GET = Arrays.asList(
            "id", "companyNumber");

    private static final Object[] ARGS = new Object[] {
            "TestID", "00006400", "Much secret" };

    @BeforeEach
    public void setUp() {
        helper = new JoinPointHelper(joinPoint);
    }

    @Test
    @DisplayName("Get arguments from a JoinPoint")
    void getArguments() {
        when(codeSignature.getParameterNames()).thenReturn(PARAM_NAMES);
        when(joinPoint.getSignature()).thenReturn(codeSignature);
        when(joinPoint.getArgs()).thenReturn(ARGS);

        Map<String, Object> arguments = helper.getArguments(PARAMS_TO_GET::contains);

        assertThat(arguments, aMapWithSize(2));
        assertThat(arguments, hasEntry(PARAM_NAMES[0], ARGS[0]));
        assertThat(arguments, hasEntry(PARAM_NAMES[1], ARGS[1]));
        assertThat(arguments, not(hasEntry(PARAM_NAMES[2], ARGS[2])));
    }

    @Test
    @DisplayName("Get arguments when unable to get parameter names")
    void getArgumentsNoNames() {
        when(codeSignature.getParameterNames()).thenReturn(new String[]{});
        when(joinPoint.getSignature()).thenReturn(codeSignature);
        when(joinPoint.getArgs()).thenReturn(ARGS);

        Map<String, Object> args = helper.getArguments(name -> true);
        assertThat(args, aMapWithSize(0));
        assertThat(args, equalTo(new HashMap<>()));
    }

    @Test
    @DisplayName("Get the names of the parameters passed into the joinpoint method")
    void getArgumentNames() {
        when(codeSignature.getParameterNames()).thenReturn(PARAM_NAMES);
        when(joinPoint.getSignature()).thenReturn(codeSignature);

        List<String> argumentNames = helper.getArgumentNames();

        assertThat(argumentNames, hasSize(PARAM_NAMES.length));
        assertThat(argumentNames, hasItems(PARAM_NAMES));
    }

    @Test
    @DisplayName("JoinPoint signature is not a CodeSignature")
    void getArgumentNamesWrongSignatureType() {
        when(joinPoint.getSignature()).thenReturn(signature);

        List<String> argumentNames = helper.getArgumentNames();

        assertThat(argumentNames, hasSize(0));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void getArgumentThatExists(int argIndex) {
        when(codeSignature.getParameterNames()).thenReturn(PARAM_NAMES);
        when(joinPoint.getSignature()).thenReturn(codeSignature);
        when(joinPoint.getArgs()).thenReturn(ARGS);

        Optional<String> arg = helper.getArgument(PARAM_NAMES[argIndex], String.class);
        assertThat(arg.isPresent(), is(true));
        assertThat(arg.get(), equalTo(ARGS[argIndex]));
    }

    @Test
    void getArgumentThatDoesntExist() {
        when(codeSignature.getParameterNames()).thenReturn(PARAM_NAMES);
        when(joinPoint.getSignature()).thenReturn(codeSignature);

        Optional<String> arg = helper.getArgument("Doesn't exist", String.class);
        assertThat(arg.isPresent(), is(false));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void getArgumentWrongType(int argIndex) {
        when(codeSignature.getParameterNames()).thenReturn(PARAM_NAMES);
        when(joinPoint.getSignature()).thenReturn(codeSignature);
        when(joinPoint.getArgs()).thenReturn(ARGS);

        Optional<Double> arg = helper.getArgument(PARAM_NAMES[argIndex], Double.class);
        assertThat(arg.isPresent(), is(false));
    }
}