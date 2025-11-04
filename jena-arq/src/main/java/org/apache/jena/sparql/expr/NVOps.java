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

package org.apache.jena.sparql.expr;

import static org.apache.jena.sparql.expr.NVDatatypes.*;
import static java.util.Map.entry;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.jena.datatypes.RDFDatatype;

/** Operations related to data/times */
class NVOps {

    // ---- Data/time with XMLGregorianCalendar

    private static final Map<RDFDatatype, Predicate<XMLGregorianCalendar>> TEMPORAL_VALIDATE = Map.ofEntries(
             // date/time
             entry(XSDdateTime,
                       gCal -> checkDateFields(gCal, true) && checkTimeFields(gCal, true)),
             entry(XSDdateTimeStamp,
                       gCal -> checkDateFields(gCal, true) && checkTimeFields(gCal, true) && checkTimezoneField(gCal, true)),
             entry(XSDdate,
                       gCal -> checkDateFields(gCal, true) && checkTimeFields(gCal, false)),
             entry(XSDtime,
                       gCal -> checkDateFields(gCal, false) && checkTimeFields(gCal, true)),
             // g*
             entry(XSDgYear,
                       gCal -> checkDateFields(gCal, true, false, false) && checkTimeFields(gCal, false)),
             entry(XSDgYearMonth,
                       gCal -> checkDateFields(gCal, true, true, false) && checkTimeFields(gCal, false)),
             entry(XSDgMonth,
                       gCal -> checkDateFields(gCal, false, true, false) && checkTimeFields(gCal, false)),
             entry(XSDgMonthDay,
                       gCal -> checkDateFields(gCal, false, true, true) && checkTimeFields(gCal, false)),
             entry(XSDgDay,
                       gCal -> checkDateFields(gCal, false, false, true) && checkTimeFields(gCal, false))
            );

    static boolean checkCalendarInstance(XMLGregorianCalendar gCal, RDFDatatype xsdDatatype) {
        Objects.requireNonNull(xsdDatatype);
        var predicate = TEMPORAL_VALIDATE.get(xsdDatatype);
        return predicate.test(gCal);
    }

    // No need to check getEonAndYear or getFractionalSecond.
    // These can not be set unless their companion field is set.

    /** Check the set/undefined status of the 6 fields (not timezone, which is optional except in xsd:dateTimeStamp) */
    private static boolean checkFields(XMLGregorianCalendar gCal, boolean yearField, boolean monthField, boolean dayField, boolean hourField, boolean minuteField, boolean secondField) {
        return checkDateFields(gCal, yearField, monthField, dayField) && checkTimeFields(gCal, hourField, minuteField, secondField);
    }

    /** Check the set/undefined status of the date fields. */
    private static boolean checkDateFields(XMLGregorianCalendar gCal, boolean present) {
        return checkDateFields(gCal, present, present, present);
    }

    /** Check the set/undefined status of the 3 date fields. */
    private static boolean checkDateFields(XMLGregorianCalendar gCal, boolean yearField, boolean monthField, boolean dayField) {
        if ( ! checkTemporalField(gCal.getYear(), yearField) )
            return false;
        // No need to check getEonAndYear -- getYear will be set.
        //gCal.getEonAndYear()
        if ( ! checkTemporalField(gCal.getMonth(),monthField ) )
            return false;
        if ( ! checkTemporalField(gCal.getDay() , dayField ) )
            return false;
        return true;
    }

    /** Check the set/undefined status of the time fields. */
    private static boolean checkTimeFields(XMLGregorianCalendar gCal, boolean present) {
        return checkTimeFields(gCal, present, present, present);
    }

    /** Check the set/undefined status of the 3 time fields. */
    private static boolean checkTimeFields(XMLGregorianCalendar gCal, boolean hourField, boolean minuteField, boolean secondField) {
        if ( ! checkTemporalField(gCal.getHour(), hourField ) )
            return false;
        if ( ! checkTemporalField(gCal.getMinute(), minuteField ) )
            return false;
        if ( ! checkTemporalField(gCal.getSecond(), secondField ) )
            return false;
        // No need to check getFractionalSecond -- getSecond will be set.
        //gCal.getFractionalSecond();
        return true;
    }

    /** Check the set/undefined status of the timezone field. */
    private static boolean checkTimezoneField(XMLGregorianCalendar gCal, boolean timezonePresent) {
        return checkTemporalField(gCal.getTimezone(), timezonePresent);
    }

    /** Check the set/undefined status of a field value in an XMLGregorialCalendar. */
    private static boolean checkTemporalField(int fieldValue, boolean isSet) {
        return ( fieldValue != DatatypeConstants.FIELD_UNDEFINED ) == isSet;
    }

    // ---- Duration
    // A Duration of nothing set is illegal so we only need to test of missing

    private static final Map<RDFDatatype, Predicate<Duration>> DURATION_VALIDATE = Map.ofEntries
            (
             entry(XSDduration,          dur -> true),
             entry(XSDdayTimeDuration,   dur ->isDayTimeDuration(dur)),
             entry(XSDyearMonthDuration, dur -> isYearMonthDuration(dur))
            );

    static boolean checkDurationInstance(Duration duration, RDFDatatype xsdDatatype) {
        Objects.requireNonNull(xsdDatatype);
        var predicate = DURATION_VALIDATE.get(xsdDatatype);
        return predicate.test(duration);
    }

    static boolean isDayTimeDuration(Duration duration) {
        return checkDurationField(duration, DatatypeConstants.YEARS, false) &&
               checkDurationField(duration, DatatypeConstants.MONTHS, false) ;
    }

    static boolean isYearMonthDuration(Duration duration) {
        return checkDurationField(duration, DatatypeConstants.DAYS, false) &&
               checkDurationField(duration, DatatypeConstants.HOURS, false) &&
               checkDurationField(duration, DatatypeConstants.MINUTES, false) &&
               checkDurationField(duration, DatatypeConstants.SECONDS, false) ;
    }

    /** Check the set/undefined status of a field value in a Duration. */
    private static boolean checkDurationField(Duration duration, DatatypeConstants.Field field, boolean isSet) {
        return duration.isSet(field) == isSet;
    }
}
