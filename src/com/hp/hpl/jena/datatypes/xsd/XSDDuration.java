/******************************************************************
 * File:        Duration.java
 * Created by:  Dave Reynolds
 * Created on:  16-Dec-02
 * 
 * (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: XSDDuration.java,v 1.7 2005-02-21 12:02:16 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.datatypes.xsd;

/**
 * Represent an XSD duration value. We use a seven dimensional space
 * with years, months, days, hours, minutes, seconds and fractional seconds.
 * This deviates from the spec which allows arbitrary position 
 * decimals for seconds.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.7 $ on $Date: 2005-02-21 12:02:16 $
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
     * Return the time component of the duration - i.e. just the hours/mins/seconds,
     * and returns the values in seconds.
     */
    public double getTimePart() {
        return ((data[h]) * 60l + data[m]) * 60l + getSeconds();
    }
    
    /**
     * Serializer
     */
    public String toString() {
         StringBuffer message = new StringBuffer(30);
         int negate = 1;
         if ( data[CY]<0 ) {
             message.append('-');
             negate=-1;
         }
         message.append('P');
         message.append(negate * data[CY]);
         message.append('Y');
         message.append(negate * data[M]);
         message.append('M');
         message.append(negate * data[D]);
         message.append('D');
         message.append('T');
         message.append(negate * data[h]);
         message.append('H');
         message.append(negate * data[m]);
         message.append('M');
         message.append(negate * data[s]);
         message.append('.');
         message.append(negate * data[ms]);
         message.append('S');

         return message.toString();
     }

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
    protected short compareDates(int[] date1, int[] date2, boolean strict) {

        //REVISIT: this is unoptimazed vs of comparing 2 durations
        //         Algorithm is described in 3.2.6.2 W3C Schema Datatype specs
        //

        //add constA to both durations
        short resultA, resultB= INDETERMINATE;

        //try and see if the objects are equal
        resultA = compareOrder (date1, date2);
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

/*
    (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
