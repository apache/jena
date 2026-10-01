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

package org.apache.jena.sparql.function;

import org.apache.jena.sparql.expr.ExprEvalException;

import java.math.BigInteger;

/**
 * Static limits that control what's permitted for some math computations accessible via the various SPARQL function
 * libraries
 */
public class MathLimits {

    private MathLimits() {

    }

    /**
     * Default maximum digits used for {@link #MAX_EXPONENT_DIGITS} and {@link #MAX_FACTORIAL_DIGITS}
     */
    public static final int DEFAULT_MAX_DIGITS = 64;
    /**
     * Configurable maximum number of digits for an exponent calculation
     */
    public static int MAX_EXPONENT_DIGITS = DEFAULT_MAX_DIGITS;
    /**
     * Configurable maximum number of digits for a factorial calculation
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
        return new ExprEvalException("Cannot calculate exponent when base is outside double range");
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
        double logA = Math.log10(a);
        if (infiniteOrNaN(logA)) {
            throw exponentOutOfRange();
        }
        double numDigits = Math.floor(b * Math.log10(a)) + 1.0d;
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
}
