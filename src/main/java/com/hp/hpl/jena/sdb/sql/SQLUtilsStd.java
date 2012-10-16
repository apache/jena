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

public class SQLUtilsStd
{
    // In progress: making this per store
    // Status: a class with working ops
    // Next: strict SQL versiosn here then subclass for DB specifics 
    
    static private final String strQuoteChar = "'" ;
    static private final String strQuoteCharEsc = strQuoteChar+strQuoteChar ;

    static private final String strQuoteChar2 = "\\" ;
    static private final String strQuoteCharEsc2 = strQuoteChar2+strQuoteChar2 ;

    
    static private final String[] strChar =       { strQuoteChar, strQuoteChar2 } ;
    static private final String[] strCharEsc =    { strQuoteCharEsc, strQuoteCharEsc2 } ;
    
    public String escapeStr(String s)
    { return FunctionsSQL.replace(s, strChar, strCharEsc) ; }
    
    public String unescapeStr(String s)
    { return FunctionsSQL.replace(s, strCharEsc, strChar) ; }
    
    public String quoteStr(String s)
    {
        s = escapeStr(s) ;
        return strQuoteChar+s+strQuoteChar ;
    }

    public String unquoteStr(String s)
    {
        if ( s.startsWith(strQuoteChar) )
            s = s.substring(1,s.length()-1 ) ;
        s = unescapeStr(s) ;
        return s ;
    }
    
    static private String identifierQuoteChar = "\"" ;
    static private String identifierQuoteChar2 = "\"\"" ;
    
    public String quoteIdentifier(String name)
    {
        if ( FunctionsSQL.sqlSafeChar(name) )
            return name ;
        // Check SQL-92
        name = name.replace(identifierQuoteChar, identifierQuoteChar2) ;
        return identifierQuoteChar+name+identifierQuoteChar ;
    }
    
    
    
    private static final String SQLmark = "_" ;
    
    /** Separator used in SQL name generation.
     *  Not used as a leading character. 
     */ 
    public String getSQLmark() { return SQLmark ; }
    
    public String gen(String first, String last)
    { return first+SQLmark+last ; }
    
    public String gen(String first)
    { return first+SQLmark ; }
}
