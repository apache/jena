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
import org.apache.jena.ext.xerces.impl.dv.ValidationContext;

/**
 * Validator for &lt;dateTimeStamp&gt; datatype (W3C Schema Datatypes)
 */
public class DateTimeStampDV extends DateTimeDV {

   private final String invalidDateTimeStampMessage = "%s is an invalid dateTimeStamp data type value. The timezone is missing.";

   @Override
   public Object getActualValue(String content, ValidationContext context) throws InvalidDatatypeValueException {
      try{
         return parse(content);
      } catch(Exception ex){
         throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{content, "dateTimeStamp"});
      }
   }

   /**
    * Parses, validates and computes normalized version of dateTime object
    *
    * @param str    The lexical representation of dateTime object CCYY-MM-DDThh:mm:ss.sss
    *               including the time zone Z or (-),(+)hh:mm
    * @return normalized dateTime representation
    * @exception SchemaDateTimeException Invalid lexical representation
    */
   protected DateTimeData parse(String str) throws SchemaDateTimeException {
      DateTimeData dateTimeData = super.parse( str );
      if (dateTimeData.hasTimeZone()) {
         String errorMessage = String.format( invalidDateTimeStampMessage, str );
         throw new SchemaDateTimeException( errorMessage );
      }
      return dateTimeData;
   }
}
