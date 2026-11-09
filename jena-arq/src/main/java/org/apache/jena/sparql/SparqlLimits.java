/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql;

import org.apache.jena.sparql.expr.ExprEvalException;

import java.math.BigInteger;
import java.text.CharacterIterator;

/**
 * Static limits that control what's permitted for some SPARQL compilation, optimisation and expression evaluation
 * <p>
 * These are protective controls that allow a system operator to set limits on some potentially problematic aspects of
 * SPARQL behaviour.  The following limits are currently exposed:
 * </p>
 * <ul>
 *     <li>{@link #MAX_EXPONENT_DIGITS}</li>
 *     <li>{@link #MAX_FACTORIAL_DIGITS}</li>
 *     <li>{@link #MAX_LENGTH_PATH_FOR_REDUCTION}</li>
 *     <li>{@link #MAX_REGEX_EVALUATION_TIME}</li>
 * </ul>
 */
public class SparqlLimits {
    private SparqlLimits() {

    }

    // Mathematical computation related limits
    // These are specifically around exponentiation and factorials

    /**
     * Default maximum digits used for {@link #MAX_EXPONENT_DIGITS} and {@link #MAX_FACTORIAL_DIGITS}
     */
    public static final int DEFAULT_MAX_DIGITS = 64;
    /**
     * Configurable maximum number of digits for an exponent calculation
     * <p>
     * Any mathematical function that involves exponentiation can require large amounts of computation, and potentially
     * produce an output vastly larger than its inputs.  By restricting the maximum number of digits such computations
     * may produce we avoid excessive computation.
     * </p>
     */
    public static int MAX_EXPONENT_DIGITS = DEFAULT_MAX_DIGITS;
    /**
     * Configurable maximum number of digits for a factorial calculation
     * <p>
     * A factorial computation may involve many calculations and depending on input produce a vastly larger output than
     * the input.  By restricting the maximum number of digits such computations may produce we avoid excessive
     * computation.
     * </p>
     */
    public static int MAX_FACTORIAL_DIGITS = DEFAULT_MAX_DIGITS;

    /**
     * Calculates how many digits raising {@code a} to the power of {@code b} would require.  If the number of digits is
     * very large then throw an expression evaluation error to prevent execution.
     *
     * @param a A, base number to raise by the exponent
     * @param b B, exponent to raise by
     * @see #preValidateExponentCalculation(double, double)
     */
    public static void preValidateExponentCalculation(BigInteger a, BigInteger b) {
        double aDbl = a.doubleValue();
        if (infiniteOrNaN(aDbl)) {
            throw baseOutOfRange();
        }
        double bDbl = b.doubleValue();
        if (infiniteOrNaN(bDbl)) {
            throw exponentOutOfRange();
        }
        preValidateExponentCalculation(aDbl, bDbl);
    }

    private static ExprEvalException tooManyExponentDigits() {
        return new ExprEvalException("Calculating this exponent would produce too many digits");
    }

    private static ExprEvalException baseOutOfRange() {
        return new ExprEvalException("Cannot calculate exponent when base is out of double range");
    }

    private static ExprEvalException exponentOutOfRange() {
        return new ExprEvalException("Cannot calculate exponent when exponent is outside double range");
    }

    private static boolean infiniteOrNaN(double aDbl) {
        return Double.isInfinite(aDbl) || Double.isNaN(aDbl);
    }

    /**
     * Calculates how many digits raising {@code a} to the power of {@code b} would require.  If the number of digits is
     * very large then throw an expression evaluation error to prevent execution.
     * <p>
     * This is based upon the following mathematical formula, for {@code a^b} then number of digits is
     * {@code floor(b * log10(a)) + 1}.
     * </p>
     *
     * @param a A, base number to raise by exponent
     * @param b B, exponent to raise by
     * @throws ExprEvalException Thrown if the calculation would require too many digits
     */
    public static void preValidateExponentCalculation(double a, double b) {
        // Raising zero to the power anything will always be zero so is always safe
        if (a == 0) {
            return;
        }

        double logA = Math.log10(Math.abs(a));
        if (infiniteOrNaN(logA)) {
            throw exponentOutOfRange();
        }
        double numDigits = Math.floor(b * logA) + 1.0d;
        if (numDigits > MAX_EXPONENT_DIGITS) {
            throw tooManyExponentDigits();
        }
    }

    /**
     * Calculates the Stirling approximation for the factorial of a number and uses it to validate whether calculating a
     * factorial would produce too many digits throwing an {@link ExprEvalException} if it would.
     * <p>
     * Based on Python example from <a href="https://www.johndcook.com/blog/2015/10/06/number-of-digits-in-n/">Number of
     * Digits in n!</a>
     * <pre>
     * def stirling(n):
     *   return floor( ((n+0.5)*log(n) - n + 0.5*log(2*pi))/log(10) ) + 1
     * </pre>
     * </p>
     * <p>
     * Note this method assumes the caller has already validated that the given input is non-negative.
     * </p>
     *
     * @param n Number to compute the factorial of
     * @throws ExprEvalException Thrown if calculating the factorial would produce too many digits
     */
    public static void preValidateFactorial(BigInteger n) {
        double nDbl = n.doubleValue();
        if (infiniteOrNaN(nDbl)) {
            throw new ExprEvalException("Cannot calculate factorial when n is outside double range");
        }
        double nAdj = nDbl + 0.5;
        double numDigits = Math.floor(((nAdj * Math.log(nDbl)) - (nAdj * Math.log(2 * Math.PI))) / Math.log(10)) + 1d;
        if (numDigits > MAX_FACTORIAL_DIGITS) {
            throw new ExprEvalException("Calculating this factorial would produce too many digits");
        }
    }

    // Path Compilation related limits

    /**
     * Default maximum length value used for the {@link #MAX_LENGTH_PATH_FOR_REDUCTION} control
     */
    public static final int DEFAULT_MAX_PATH_REDUCTION_LENGTH = 5;
    /**
     * Specifies the maximum length (inclusive) of a path that will be reduced by expanding it into individual
     * invocations of the path expression linked by intermediate variables.  Defaults to
     * {@value #DEFAULT_MAX_PATH_REDUCTION_LENGTH}.
     * <p>
     * This control prevents a query using a path like {@code :x :p{N} :y} with a large value of {@code N} being
     * expanded into a massive algebra tree that yields no performance benefits.
     * </p>
     *
     * @see #isReducibleLength(long)
     */
    public static int MAX_LENGTH_PATH_FOR_REDUCTION = DEFAULT_MAX_PATH_REDUCTION_LENGTH;

    /**
     * Checks whether a given length of path is considered reducible
     * <p>
     * Only paths that are non-zero length and less than, or equal to, the configured
     * {@link #MAX_LENGTH_PATH_FOR_REDUCTION} (default {@value #DEFAULT_MAX_PATH_REDUCTION_LENGTH}) are considered
     * reducible.  Anything else is left as-is as either reduction would change semantics (for zero-length paths), or
     * could lead to very large algebra trees which would negatively impact performance.
     * </p>
     *
     * @param N Path length
     * @return True if reducible, false otherwise
     */
    public static boolean isReducibleLength(long N) {
        return N > 0 && N <= MAX_LENGTH_PATH_FOR_REDUCTION;
    }

    // Regular Expression related limits

    /**
     * Default maximum permitted regular expression evaluation time
     *
     * @see #MAX_REGEX_EVALUATION_TIME
     */
    public static final long DEFAULT_MAX_REGEX_EVALUATION_TIME = 250;
    /**
     * Maximum permitted regular expression evaluation time, defaults to {@link #DEFAULT_MAX_REGEX_EVALUATION_TIME}
     * <p>
     * For some regular expression patterns the evaluation time can be excessive, especially if a pattern, or the
     * pattern combined with the data leads to catastrophic backtracking.  Therefore, we bound the evaluation time by
     * using a special {@link CharSequence}/{@link CharacterIterator} wrapper that enforces a time limit.  If the limit
     * is exceeded then an {@link ExprEvalException} is thrown.  This does impose a small performance penalty though
     * this is negligible and considered an acceptable trade-off for the protection it provides.
     * </p>
     * <p>
     * If an application makes extensive use of complex regular expressions, and you are certain that those are not
     * subject to catastrophic backtracking you can either increase the limit appropriately.  Or you can set to a
     * zero/negative value to disable the limit entirely.
     * </p>
     */
    public static long MAX_REGEX_EVALUATION_TIME = DEFAULT_MAX_REGEX_EVALUATION_TIME;

}
