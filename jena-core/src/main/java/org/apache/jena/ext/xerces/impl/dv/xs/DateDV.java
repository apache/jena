/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.ext.xerces.impl.dv.xs;

import org.apache.jena.ext.xerces.impl.dv.InvalidDatatypeValueException;

/**
 * Validator for {@literal <date>} datatype (W3C Schema datatypes)
 *
 * {@literal @xerces.internal}
 *
 * @author Elena Litani
 * @author Gopal Sharma, SUN Microsystems Inc.
 *
 * @version $Id: DateDV.java 937741 2010-04-25 04:25:46Z mrglavas $
 */
public class DateDV extends DateTimeDV {

    @Override
    public Object getActualValue(String content) throws InvalidDatatypeValueException {
        try{
            return parse(content);
        } catch(Exception ex){
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{content, "date"});
        }
    }

    /**
     * Parses, validates and computes normalized version of dateTime object
     *
     * @param str    The lexical representation of dateTime object CCYY-MM-DD
     *               with possible time zone Z or (-),(+)hh:mm
     * @return normalized dateTime representation
     * @exception SchemaDateTimeException Invalid lexical representation
     */
    @Override
    protected DateTimeData parse(String str) throws SchemaDateTimeException {
        DateTimeData date = new DateTimeData(str, this);
        int len = str.length();

        int end = getDate(str, 0, len, date);
        parseTimeZone (str, end, len, date);

        //validate and normalize
        //REVISIT: do we need SchemaDateTimeException?
        validateDateTime(date);

        //save unnormalized values
        saveUnnormalized(date);

        if (date.utc!=0 && date.utc!='Z') {
            normalize(date);
        }
        return date;
    }

    @Override
    protected String dateToString(DateTimeData date) {
        StringBuilder message = new StringBuilder(25);
        append(message, date.year, 4);
        message.append('-');
        append(message, date.month, 2);
        message.append('-');
        append(message, date.day, 2);
        append(message, (char)date.utc, 0);
        return message.toString();
    }
}
