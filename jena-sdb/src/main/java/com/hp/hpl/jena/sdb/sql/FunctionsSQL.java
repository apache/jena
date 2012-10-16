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

package com.hp.hpl.jena.sdb.sql;

import java.sql.Timestamp;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.LoggerFactory;

/** General purpose SQL-related manipulation */

public class FunctionsSQL
{
    public static boolean sqlSafeChar(String str)
    {
        if ( ! isLowerCaseSqlChar(str.charAt(0)) )
            return false ;
        
        for ( int i = 0 ; i < str.length() ; i++ )
        {
            char ch = str.charAt(i) ;
            // Explicitly ASCII
            // if not lowercase letter
            if ( ! isLowerCaseSqlChar(ch) && ! isSqlDigit(ch) )
                return false ;
        }
        return true ;
    }

    public static String replace(String str, String[] fromArray, String[] toArray)
    {
        for ( int i = 0 ; i < fromArray.length ; i++ )
            str = str.replace(fromArray[i], toArray[i]) ;
        return str ;
    }
    
    public static boolean isLowerCaseSqlChar(char ch)
    {
        return ch >= 'a' && ch <= 'z' ;
    }
    
    public static boolean isSqlDigit(char ch)
    {
        return ch >= '0' && ch <= '9' ;
    }

    /** Turn the lexical form of an XSD date into what SQL believes in */
    public static String toSQLdatetimeString(String lex)
    {
        try
        {
            DatatypeFactory f = DatatypeFactory.newInstance() ;
            XMLGregorianCalendar cal = f.newXMLGregorianCalendar(lex) ;
            long millis = cal.toGregorianCalendar().getTimeInMillis() ;
            Timestamp timestamp = new Timestamp(millis) ;
            return timestamp.toString() ;
        } catch (DatatypeConfigurationException e)
        {
            LoggerFactory.getLogger(SQLUtils.class).warn("Failed to convert "+lex, e) ;
            return "0000-00-00 00:00:00" ;
        }
    }
}
