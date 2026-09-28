package org.apache.jena.sparql.function.library;

import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.function.MathLimits;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestMathLimits {

    public static Stream<Arguments> bigIntegerExponents() {
        return Stream.of(Arguments.of(BigInteger.valueOf(2), BigInteger.valueOf(16), true),
                         Arguments.of(BigInteger.valueOf(100), BigInteger.valueOf(100), false),
                         Arguments.of(BigInteger.valueOf(2), BigInteger.valueOf(2_000_000_000), false),
                         Arguments.of(BigInteger.valueOf(2_000_000_000), BigInteger.valueOf(16), false));
    }

    @ParameterizedTest
    @MethodSource("bigIntegerExponents")
    public void givenBigIntegers_whenPreValidatingExponent_thenAsExpected(BigInteger a, BigInteger b, boolean valid) {
        if (valid) {
            MathLimits.preValidateExponentCalculation(a, b);
        } else {
            assertThrows(ExprEvalException.class, () -> MathLimits.preValidateExponentCalculation(a, b));
        }
    }

    public static Stream<Arguments> factorials() {
        return Stream.of(Arguments.of(BigInteger.valueOf(10), true),
                         Arguments.of(BigInteger.valueOf(63), true),
                         Arguments.of(BigInteger.valueOf(2_000_000_000), false));
    }

    @ParameterizedTest
    @MethodSource("factorials")
    public void givenFactorialInputs_whenPreValidating_thenAsExpected(BigInteger n, boolean valid) {
        if (valid) {
            MathLimits.preValidateFactorial(n);
        } else {
            assertThrows(ExprEvalException.class, () -> MathLimits.preValidateFactorial(n));
        }
    }
}
