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

package org.apache.jena.ext.xerces.util;

import java.util.*;

/**
 * <p>Used to format JAXP 1.3 Datatype API error messages using a specified locale.</p>
 *
 * @author  Neeraj Bajaj, Sun Microsystems
 * @version $Id: DatatypeMessageFormatter.java 813087 2009-09-09 19:35:27Z mrglavas $
 */
public class DatatypeMessageFormatter {

    private static final String BASE_NAME = "org.apache.jena.ext.xerces.impl.msg.DatatypeMessages";

    /**
     * Formats a message with the specified arguments using the given
     * locale information.
     *
     * @param locale    The locale of the message.
     * @param key       The message key.
     * @param arguments The message replacement text arguments. The order
     *                  of the arguments must match that of the placeholders
     *                  in the actual message.
     *
     * @return          the formatted message.
     *
     * @throws MissingResourceException Thrown if the message with the
     *                                  specified key cannot be found.
     */
    public static String formatMessage(Locale locale, String key, Object[] arguments) throws MissingResourceException {

        String fmt = messageFormats.getOrDefault(key, null);
        if ( fmt == null ) {
            String msg = messageFormats.get("BadMessageKey");
            throw new MissingResourceException(key, msg, key);
        }

        if (arguments == null) {
            String msg = messageFormats.get("FormatFailed");
            throw new MissingResourceException(key, msg, key);
        }

        // format message
        try {
            String msg = java.text.MessageFormat.format(fmt, arguments);
            return msg;
        } catch (Exception e) {
            String msg = messageFormats.get("FormatFailed") + " " + fmt;
            throw new MissingResourceException(key, msg, key);
        }
    }

    private static Map<String, String> messageFormats = messagesMap();

    private static Map<String, String> messagesMap() {
        Map<String,String> map = new HashMap<>();
        map.put("BadMessageKey", "The error message corresponding to the message key can not be found.");
        map.put("FormatFailed", "An internal error occurred while formatting the following message:\n");
        map.put("FieldCannotBeNull", "{0} cannot be called with 'null' parameter.");
        map.put("UnknownField", "{0} called with an unknown field:{1}");
        map.put("InvalidXGCValue-milli", "Year = {0}, Month = {1}, Day = {2}, Hour = {3}, Minute = {4}, Second = {5}, fractionalSecond = {6}, Timezone = {7} , is not a valid representation of an XML Gregorian Calendar value.");
        map.put("InvalidXGCValue-fractional", "Year = {0}, Month = {1}, Day = {2}, Hour = {3}, Minute = {4}, Second = {5}, fractionalSecond = {6}, Timezone = {7} , is not a valid representation of an XML Gregorian Calendar value.");
        map.put("InvalidXGCFields", "Invalid set of fields set for XMLGregorianCalendar");
        map.put("InvalidFractional", "Invalid value {0} for fractional second.");
        map.put("InvalidXGCRepresentation", "\"{0}\" is not a valid representation of an XML Gregorian Calendar value.");
        map.put("InvalidFieldValue", "Invalid value {0} for {1} field.");
        map.put("NegativeField", "{0} field is negative");
        map.put("AllFieldsNull", "All the fields (javax.xml.datatype.DatatypeConstants.Field) are null.");
        map.put("TooLarge", "{0} value \"{1}\" too large to be supported by this implementation");
        return Map.copyOf(map);
    }

    // Original DatatypeMessages.properties file
    // @formatter:off
/*
# This file stores localized messages for the Xerces JAXP Datatype API implementation.
#
# The messages are arranged in key and value tuples in a ListResourceBundle.
#
# @version $Id: DatatypeMessages.properties 595211 2007-11-15 05:28:12Z mrglavas $

BadMessageKey = The error message corresponding to the message key can not be found.
FormatFailed = An internal error occurred while formatting the following message:\n

FieldCannotBeNull={0} cannot be called with 'null' parameter.
UnknownField={0} called with an unknown field\:{1}
#There are two similar keys 'InvalidXMLGreogorianCalendarValue' . Suffix (year, month) has been added and are used as per the context.
InvalidXGCValue-milli=Year \= {0}, Month \= {1}, Day \= {2}, Hour \= {3}, Minute \= {4}, Second \= {5}, fractionalSecond \= {6}, Timezone \= {7} , is not a valid representation of an XML Gregorian Calendar value.
#There are two similar keys 'InvalidXMLGreogorianCalendarValue' . Suffix (year, month) has been added and are used as per the context.
InvalidXGCValue-fractional=Year \= {0}, Month \= {1}, Day \= {2}, Hour \= {3}, Minute \= {4}, Second \= {5}, fractionalSecond \= {6}, Timezone \= {7} , is not a valid representation of an XML Gregorian Calendar value.

InvalidXGCFields=Invalid set of fields set for XMLGregorianCalendar

InvalidFractional=Invalid value {0} for fractional second.

#XGC stands for XML Gregorian Calendar
InvalidXGCRepresentation="{0}" is not a valid representation of an XML Gregorian Calendar value.

InvalidFieldValue=Invalid value {0} for {1} field.

NegativeField= {0} field is negative

AllFieldsNull=All the fields (javax.xml.datatype.DatatypeConstants.Field) are null.

TooLarge={0} value "{1}" too large to be supported by this implementation
            """;
*/
    // @formatter:on
}
