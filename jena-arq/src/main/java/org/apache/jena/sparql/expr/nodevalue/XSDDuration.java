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

package org.apache.jena.sparql.expr.nodevalue;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import org.apache.jena.sparql.expr.Expr;

import javax.xml.datatype.DatatypeConstants.Field;

/**
 * Functions relating to XSD durations (F&amp;O 3.1), using
 * {@code javax.xml.datatype.Duration}
 */
public class XSDDuration {

    private enum DurationVS { YEARMONTH, DAYTIME, DURATION };

    /**
     * Compare durations.
     * <p>
     * There are two value spaces: year-month and day-time.
     * <p></p>
     * Value spaces are determined dynamically, not from the datatype.
     * <p></p>
     * Comparison across these
     * two spaces is not possible except in the case of zero duration. This function
     * returns {@link Expr#CMP_UNEQUAL} for durations in different value spaces which
     * are not both zero.
     * <p></p>
     * If the comparison using the JDK throws "UnsupportedOperationException", this function returns {@link Expr#CMP_INDETERMINATE}.
     * (this occurs if field values are too large - larger than {@link Integer#MAX_VALUE}.
     * <p></p>
     * <a href="https://www.w3.org/TR/xpath-functions-3/#comp.duration">Comparison
     * operators on durations</a>
     * <p></p>
     * "With the exception of the zero-length duration, no instance of
     * xs:dayTimeDuration can ever be equal to an instance of xs:yearMonthDuration."
     */
    public static int durationCompare(Duration duration1, Duration duration2) {
        // Dynamic classification of lexical form.
        DurationVS vs1 = durationClassify(duration1);
        DurationVS vs2 = durationClassify(duration2);
        try {
            boolean zero1 = durationIsZero$(duration1);
            boolean zero2 = durationIsZero$(duration2);
            if ( zero1 && zero2 )
                return Expr.CMP_EQUAL;

            // A zero duration belongs to both value spaces, so it can be compared with any duration.
            if ( zero1 )
                vs1 = vs2;
            if ( zero2 )
                vs2 = vs1;

            if ( vs1.equals(vs2) ) {
                // F&O: year-month durations compare by their total months, day-time durations by
                // their total seconds. This is exact; javax.xml.datatype.Duration.compare
                // truncates fractional seconds.
                switch (vs1) {
                    case YEARMONTH :
                        return signum(totalMonths(duration1).compareTo(totalMonths(duration2)));
                    case DAYTIME :
                        return signum(totalSeconds(duration1).compareTo(totalSeconds(duration2)));
                    default :
                        // Both have year-month and day-time parts: only the JDK's calendar-based
                        // comparison can order these, and it may be indeterminate.
                        int x = duration1.compare(duration2);
                        if ( x == -1 || x == 0 || x == 1 )
                            return x;
                        return Expr.CMP_INDETERMINATE;
                }
            }

            // Otherwise, different value spaces: never equal.
            return Expr.CMP_UNEQUAL;
        } catch (UnsupportedOperationException ex) {
            return Expr.CMP_INDETERMINATE;
        }
    }

    private static int signum(int x) {
        return Integer.signum(x);
    }

    /** Signed number of months in the year-month part of a duration. */
    private static BigInteger totalMonths(Duration duration) {
        BigInteger months = field(duration, DatatypeConstants.YEARS).multiply(BigInteger.valueOf(12))
                                .add(field(duration, DatatypeConstants.MONTHS));
        return duration.getSign() < 0 ? months.negate() : months;
    }

    /** Signed number of seconds in the day-time part of a duration, exact. */
    private static BigDecimal totalSeconds(Duration duration) {
        BigDecimal seconds = new BigDecimal(field(duration, DatatypeConstants.DAYS)).multiply(BigDecimal.valueOf(86400))
                                 .add(new BigDecimal(field(duration, DatatypeConstants.HOURS)).multiply(BigDecimal.valueOf(3600)))
                                 .add(new BigDecimal(field(duration, DatatypeConstants.MINUTES)).multiply(BigDecimal.valueOf(60)))
                                 .add(secondsField(duration));
        return duration.getSign() < 0 ? seconds.negate() : seconds;
    }

    /** Integer field as a BigInteger, zero when unset. */
    private static BigInteger field(Duration duration, Field field) {
        if ( ! duration.isSet(field) )
            return BigInteger.ZERO;
        Number n = duration.getField(field);
        if ( n instanceof BigInteger bi )
            return bi;
        return BigInteger.valueOf(n.longValue());
    }

    /** Seconds field as a BigDecimal, zero when unset. */
    private static BigDecimal secondsField(Duration duration) {
        if ( ! duration.isSet(DatatypeConstants.SECONDS) )
            return BigDecimal.ZERO;
        Number n = duration.getField(DatatypeConstants.SECONDS);
        if ( n instanceof BigDecimal bd )
            return bd;
        return new BigDecimal(n.toString());
    }

    /**
     * Test whether a duration is zero
     */
    public static boolean durationIsZero(Duration duration) {
        if ( ! durationIsSet(duration) )
            return false;
        return durationIsZero$(duration);
    }

    private static boolean durationIsZero$(Duration duration) {
        return zeroField(duration, DatatypeConstants.YEARS) &&
               zeroField(duration, DatatypeConstants.MONTHS) &&
               zeroField(duration, DatatypeConstants.DAYS) &&
               zeroField(duration, DatatypeConstants.HOURS) &&
               zeroField(duration, DatatypeConstants.MINUTES) &&
               zeroField(duration, DatatypeConstants.SECONDS);
    }

    /** Check whether a duration field is unset or is zero */
    private static boolean zeroField(Duration duration, Field field) {
        if ( ! duration.isSet(field) )
            return true;
        Number n = duration.getField(field);
        // Seconds is a BigDecimal: "PT0.5S" is not zero, although its intValue() is.
        if ( n instanceof BigDecimal bd )
            return bd.signum() == 0;
        if ( n instanceof BigInteger bi )
            return bi.signum() == 0;
        return n.doubleValue() == 0;
    }

    /**
     * Check whether a duration is set in some way - the test is whether it is not all "undefined"
     */
    private static boolean durationIsSet(Duration duration) {
        boolean isSetYear     = duration.isSet(DatatypeConstants.YEARS);
        boolean isSetMonth    = duration.isSet(DatatypeConstants.MONTHS);
        boolean isSetDay      = duration.isSet(DatatypeConstants.DAYS);
        boolean isSetHour     = duration.isSet(DatatypeConstants.HOURS);
        boolean isSetMinute   = duration.isSet(DatatypeConstants.MINUTES);
        boolean isSetSeconds  = duration.isSet(DatatypeConstants.SECONDS);
        return ( isSetYear || isSetMonth || isSetDay || isSetHour || isSetMinute || isSetSeconds );
    }

    /**
     * Classify a duration according XSD/F&O.
     * <ul>
     * <li> {@code xsd:dayTimeDuration} (no year or month)
     * <li> {@code xsd:yearMonthDuration} (no day,hour, minute, second)
     * <li> {@code xsd:duration} - otherwise.
     * <ul>
     * The classification is by value: a field that is written but zero, as the
     * year in {@code "P0Y1D"}, does not make the duration a year-month duration,
     * so {@code "P0Y1D"} and {@code "P1D"} are the same day-time duration.
     */
    private static DurationVS durationClassify(Duration duration) {
        boolean hasYearMonth = ! zeroField(duration, DatatypeConstants.YEARS) ||
                               ! zeroField(duration, DatatypeConstants.MONTHS);
        boolean hasDayTime   = ! zeroField(duration, DatatypeConstants.DAYS) ||
                               ! zeroField(duration, DatatypeConstants.HOURS) ||
                               ! zeroField(duration, DatatypeConstants.MINUTES) ||
                               ! zeroField(duration, DatatypeConstants.SECONDS);

        if ( ! hasYearMonth && ! hasDayTime )
            // Zero or unset.
            return DurationVS.DURATION;

        if ( ! hasYearMonth )
            return DurationVS.DAYTIME;

        if ( ! hasDayTime )
            return DurationVS.YEARMONTH;

        // Mixture
        return DurationVS.DURATION;
    }
}
