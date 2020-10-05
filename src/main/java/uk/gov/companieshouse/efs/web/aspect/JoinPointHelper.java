package uk.gov.companieshouse.efs.web.aspect;

import static java.lang.Long.min;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.CodeSignature;

/**
 * Some helper utility functions for dealing with join points
 * when doing aspect orientated programming.
 */
public class JoinPointHelper {
    private final JoinPoint jp;

    public JoinPointHelper(JoinPoint jp) {
        this.jp = jp;
    }

    private CodeSignature getSignature() {
        return (CodeSignature) jp.getSignature();
    }

    /**
     * Returns the arguments passed to a method in which the JoinPoint is located.
     * The fields can be filtered by a predicate. If the predicate returns true for a given key
     * then it will be kept i the argument map.
     * This allows sensitive fields to be removed.
     * usage:
     * {@code Map<String, Object> args = getArguments(arg -> !arg.equals("sensitiveField"));}
     *
     * @param shouldKeepParam a predicate that accepts the parameter name and returns true if that
     *                        parameter should be kept.
     * @return A map with names as keys and values as values.
     */
    Map<String, Object> getArguments(Predicate<String> shouldKeepParam) {
        List<String> argNames = getArgumentNames();
        List<Object> args = Arrays.asList(jp.getArgs());

        if (min(argNames.size(), args.size()) < 1) {
            return new HashMap<>();
        }

        Map<String, Object> argumentMap = IntStream.range(0, args.size()).boxed()
                .collect(Collectors.toMap(argNames::get, args::get));

        Optional.ofNullable(shouldKeepParam)
                .map(Predicate::negate)
                .ifPresent(argumentMap.keySet()::removeIf);

        return argumentMap;
    }

    /**
     * Gets the names of parameters passed to the method in which the JoinPoint is located.
     *
     * @return a list of parameter names
     */
    List<String> getArgumentNames() {
        CodeSignature sig;
        try {
            sig = getSignature();
        } catch (ClassCastException invalidCast) {
            return Collections.emptyList();
        }

        return Optional.ofNullable(sig.getParameterNames())
                .map(Arrays::asList)
                .orElseGet(Collections::emptyList);
    }

    /**
     * Gets the value of a specific argument from a JoinPoint.
     * If the is no argument with that name then an empty optional is returned.
     *
     * @param argumentName the name of the argument to get
     * @return The value wrapped in an optional
     */
    <T> Optional<T> getArgument(String argumentName, Class<T> type) {
        List<String> allArgumentNames = getArgumentNames();
        if (!allArgumentNames.contains(argumentName)) {
            return Optional.empty();
        }

        int argumentIndex = allArgumentNames.indexOf(argumentName);
        try {
            return Optional.ofNullable(jp.getArgs()[argumentIndex])
                    .map(type::cast);
        } catch (IndexOutOfBoundsException | ClassCastException exception) {
            return Optional.empty();
        }
    }
}
