/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.sparql.expr.nodevalue;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import org.apache.jena.sparql.expr.Expr;

import javax.xml.datatype.DatatypeConstants.Field;

/**
 * Functions relating to XSD durations (F&O 3.1), using
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
            if ( vs1.equals(vs2) ) {
                int x = duration1.compare(duration2);
                if ( x == -1 || x == 0 || x == 1 )
                    return x;
                return Expr.CMP_INDETERMINATE;
            }

            // Otherwise, only zeros can be equals.
            if ( durationIsZero$(duration1) && durationIsZero$(duration2) )
                return Expr.CMP_EQUAL;
            return Expr.CMP_UNEQUAL;
        } catch (UnsupportedOperationException ex) {
            return Expr.CMP_INDETERMINATE;
        }
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
        return ! duration.isSet(field) || duration.getField(field).intValue() == 0 ;
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
     */
    private static DurationVS durationClassify(Duration duration) {
        boolean isSetYear     = duration.isSet(DatatypeConstants.YEARS);
        boolean isSetMonth    = duration.isSet(DatatypeConstants.MONTHS);
        boolean isSetDay      = duration.isSet(DatatypeConstants.DAYS);
        boolean isSetHour     = duration.isSet(DatatypeConstants.HOURS);
        boolean isSetMinute   = duration.isSet(DatatypeConstants.MINUTES);
        boolean isSetSeconds  = duration.isSet(DatatypeConstants.SECONDS);

        if ( ! isSetYear && ! isSetMonth && ! isSetDay && ! isSetHour && ! isSetMinute && ! isSetSeconds )
            // Unset.
            return DurationVS.DURATION;

        // Something must be set.
        if ( ! isSetYear && ! isSetMonth )
            return DurationVS.DAYTIME;

        if ( ! isSetDay && ! isSetHour && ! isSetMinute && ! isSetSeconds )
            return DurationVS.YEARMONTH;

        // Mixture
        return DurationVS.DURATION;
    }
}
