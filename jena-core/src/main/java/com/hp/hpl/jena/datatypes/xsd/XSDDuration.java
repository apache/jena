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

package com.hp.hpl.jena.datatypes.xsd;

import java.math.BigDecimal;

import com.hp.hpl.jena.datatypes.xsd.impl.XSDAbstractDateTimeType;

/**
 * Represent an XSD duration value. We use a seven dimensional space
 * with years, months, days, hours, minutes, seconds and fractional seconds.
 * This deviates from the spec which allows arbitrary position 
 * decimals for seconds.
 */

public class XSDDuration extends AbstractDateTime {

    /**
     * Constructor - should only be used by the internals but public scope because
     * the internals spread across multiple packages.
     * 
     * @param value the date/time value returned by the parsing
     */
    public XSDDuration(Object value) {
        super(value);
    }
    
    /**
     * Return the number of years in the duration
     */
    public int getYears() {
        return data[CY];
    }
    
    /**
     * Return the number of months in the duration
     */
    public int getMonths() {
        return data[M];
    }
    
    /**
     * Return the number of years in the duration
     */
    public int getDays() {
        return data[D];
    }
    
    /**
     * Return the number of hours in the duration
     */
    public int getHours() {
        return data[h];
    }
    
    /**
     * Return the number of minutes in the duration
     */
    public int getMinutes() {
        return data[m];
    }
    
    /**
     * Return the number of full seconds in the duration
     */
    public int getFullSeconds() {
        return data[s];
    }
    
    /**
     * Return the number of seconds in the duration, including fractional part
     */
    public double getSeconds() {
        return data[s] + fractionalSeconds;
    }
    
    /**
     * Return the number of seconds in the duration, including the fractional part,
     * in a lossless but expensive notation - i.e. a BigDecimal.
     */
    public BigDecimal getBigSeconds() {
        return BigDecimal.valueOf( data[ms], data[msscale])
                     .add( BigDecimal.valueOf(data[s]));
    }
    
    /**
     * Return the time component of the duration - i.e. just the hours/mins/seconds,
     * and returns the values in seconds.
     */
    public double getTimePart() {
        return ((data[h]) * 60l + data[m]) * 60l + getSeconds();
    }
    
    /**
     * Serializer
     */
    @Override
    public String toString() {
        // All zeros -> return canonical zero duration.
        if ( data[CY]==0 && data[M]==0 && data[D]==0 && data[h]==0 && data[m]==0 && data[s]==0 && data[ms]==0 )
            return "PT0S" ;

        StringBuffer message = new StringBuffer(30);
        int negate = 1;
        if ( data[CY]<0 || data[M]<0 || data[D]<0 || data[h]<0 || data[m]<0 || data[s]<0 || data[ms]<0 ) {
            message.append('-');
            negate=-1;
        }
        // All zeros -> return canonical zero duration.
        if ( data[CY]==0 && data[M]==0 && data[D]==0 && data[h]==0 && data[m]==0 && data[s]==0 && data[ms]==0 )
            return "PT0S" ;
        message.append('P');
        if (data[CY] != 0) {
            message.append(negate * data[CY]);
            message.append('Y');
        }
        if (data[M] != 0) {
            message.append(negate * data[M]);
            message.append('M');
        }
        if (data[D] != 0) {
            message.append(negate * data[D]);
            message.append('D');
        }
        if (data[h] != 0 || data[m] != 0 || data[s] != 0 || data[ms] != 0) {
            message.append('T');
            if (data[h] != 0) {
                message.append(negate * data[h]);
                message.append('H');
            }
            if (data[m] != 0) {
                message.append(negate * data[m]);
                message.append('M');
            }
            if (data[s] != 0 || data[ms] != 0) {
                message.append(negate * data[s]);
                if ( data[ms] != 0 )
                {
                    message.append('.');
                    XSDAbstractDateTimeType.appendFractionalTime(message, negate * data[ms], data[msscale]);
                }
                message.append('S');
            }
        }

        return message.toString();
    }

    // The following duration comparison code is based on Xerces DurationDV, Apache Software Foundation
    
    // order-relation on duration is a partial order. The dates below are used to
    // for comparison of 2 durations, based on the fact that
    // duration x and y is x<=y iff s+x<=s+y
    // see 3.2.6 duration W3C schema datatype specs
    //
    // the dates are in format: {CCYY,MM,DD, H, S, M, MS, timezone}
    private final static int[][] DATETIMES= {
        {1696, 9, 1, 0, 0, 0, 0, 'Z'},
        {1697, 2, 1, 0, 0, 0, 0, 'Z'},
        {1903, 3, 1, 0, 0, 0, 0, 'Z'},
        {1903, 7, 1, 0, 0, 0, 0, 'Z'}};

    private int[][] fDuration = null;

    /**
     * Compares 2 given durations. (refer to W3C Schema Datatypes "3.2.6 duration")
     *
     * @param date1  Unnormalized duration
     * @param date2  Unnormalized duration
     * @param strict (min/max)Exclusive strict == true ( LESS_THAN ) or ( GREATER_THAN )
     *               (min/max)Inclusive strict == false (LESS_EQUAL) or (GREATER_EQUAL)
     * @return INDETERMINATE if the order relationship between date1 and date2 is indeterminate. 
     * EQUAL if the order relation between date1 and date2 is EQUAL.  
     * If the strict parameter is true, return LESS_THAN if date1 is less than date2 and
     * return GREATER_THAN if date1 is greater than date2. 
     * If the strict parameter is false, return LESS_THAN if date1 is less than OR equal to date2 and
     * return GREATER_THAN if date1 is greater than OR equal to date2 
     */
    @Override
    protected short compareDates(int[] date1, int[] date2, boolean strict) {

        //REVISIT: this is unoptimazed vs of comparing 2 durations
        //         Algorithm is described in 3.2.6.2 W3C Schema Datatype specs
        //

        //add constA to both durations
        short resultA, resultB= INDETERMINATE;

        //try and see if the objects are equal
        resultA = compareOrder (date1, date2);
        short baseResult = resultA;   // Full comparison including time fractions
        if ( resultA == 0 ) {
            return 0;
        }
        if ( fDuration == null ) {
            fDuration = new int[2][TOTAL_SIZE];
        }
        //long comparison algorithm is required
        int[] tempA = addDuration (date1, 0, fDuration[0]);
        int[] tempB = addDuration (date2, 0, fDuration[1]);
        resultA =  compareOrder(tempA, tempB);
        if ( resultA == INDETERMINATE ) {
            return INDETERMINATE;
        }

        tempA = addDuration(date1, 1, fDuration[0]);
        tempB = addDuration(date2, 1, fDuration[1]);
        resultB = compareOrder(tempA, tempB);
        resultA = compareResults(resultA, resultB, strict);
        if (resultA == INDETERMINATE) {
            return INDETERMINATE;
        }

        tempA = addDuration(date1, 2, fDuration[0]);
        tempB = addDuration(date2, 2, fDuration[1]);
        resultB = compareOrder(tempA, tempB);
        resultA = compareResults(resultA, resultB, strict);
        if (resultA == INDETERMINATE) {
            return INDETERMINATE;
        }

        tempA = addDuration(date1, 3, fDuration[0]);
        tempB = addDuration(date2, 3, fDuration[1]);
        resultB = compareOrder(tempA, tempB);
        resultA = compareResults(resultA, resultB, strict);

        if (resultA == 0) {
            // determinate equality for data portion, so base comparison
            // (which includes fractional time) is the correct result
            return baseResult;
        }
        return resultA;
    }

    private short compareResults(short resultA, short resultB, boolean strict){

      if ( resultB == INDETERMINATE ) {
            return INDETERMINATE;
        }
        else if ( resultA!=resultB && strict ) {
            return INDETERMINATE;
        }
        else if ( resultA!=resultB && !strict ) {
            if ( resultA!=0 && resultB!=0 ) {
                return INDETERMINATE;
            }
            else {
                return (resultA!=0)?resultA:resultB;
            }
        }
        return resultA;
    }

    private int[] addDuration(int[] date, int index, int[] duration) {

        //REVISIT: some code could be shared between normalize() and this method,
        //         however is it worth moving it? The structures are different...
        //

        resetDateObj(duration);
        //add months (may be modified additionaly below)
        int temp = DATETIMES[index][M] + date[M];
        duration[M] = modulo (temp, 1, 13);
        int carry = fQuotient (temp, 1, 13);

        //add years (may be modified additionaly below)
        duration[CY]=DATETIMES[index][CY] + date[CY] + carry;

        //add seconds
        temp = DATETIMES[index][s] + date[s];
        carry = fQuotient (temp, 60);
        duration[s] =  mod(temp, 60, carry);

        //add minutes
        temp = DATETIMES[index][m] +date[m] + carry;
        carry = fQuotient (temp, 60);
        duration[m]= mod(temp, 60, carry);

        //add hours
        temp = DATETIMES[index][h] + date[h] + carry;
        carry = fQuotient(temp, 24);
        duration[h] = mod(temp, 24, carry);


        duration[D]=DATETIMES[index][D] + date[D] + carry;

        while ( true ) {

            temp=maxDayInMonthFor(duration[CY], duration[M]);
            if ( duration[D] < 1 ) { //original duration was negative
                duration[D] = duration[D] + maxDayInMonthFor(duration[CY], duration[M]-1);
                carry=-1;
            }
            else if ( duration[D] > temp ) {
                duration[D] = duration[D] - temp;
                carry=1;
            }
            else {
                break;
            }
            temp = duration[M]+carry;
            duration[M] = modulo(temp, 1, 13);
            duration[CY] = duration[CY]+fQuotient(temp, 1, 13);
        }

        duration[utc]='Z';
        return duration;
    }
    
}
