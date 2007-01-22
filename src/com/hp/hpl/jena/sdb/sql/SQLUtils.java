/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.sql;

import static com.hp.hpl.jena.sdb.util.StrUtils.strjoinNL;

import java.sql.Timestamp;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.logging.LogFactory;

public class SQLUtils
{
    static public String sqlStr(String ... str)
    {
        return strjoinNL(str) ;
    }
    
    // TODO Check SQL-92/2003 
    // for what the quoting characters and escape mechanisms 
    // are for:
    //   Strings
    //   Identifiers with strange chars
    // Per-store-iation?
    
    // Standard:
    //   String quote is single quote
    //   Identifier quote is " 
    //     but MySQL uses `
    // MySQL has "ANSI quotes" mode => "
    //   Variables: _ (not $ strictly)
    
    // Standard SQL quoting is to double up '
    static private String strQuoteChar = "'" ;
    static private String strQuoteCharEsc = strQuoteChar+strQuoteChar ;
    
    static private String[] strChar =       { strQuoteChar } ;
    static private String[] strCharEsc =    { strQuoteCharEsc } ;
    
    static public String quoteStr(String s)
    {
        s = map(s, strChar, strCharEsc) ;
        return strQuoteChar+s+strQuoteChar ;
    }

    static public String unquoteStr(String s)
    {
        if ( s.startsWith(strQuoteChar) )
            s = s.substring(1,s.length()-1 ) ;
        s = map(s, strCharEsc, strChar) ;
        return s ;
    }
    
    static private String map(String str, String[] fromArray, String[] toArray)
    {
        for ( int i = 0 ; i < fromArray.length ; i++ )
            str = str.replace(fromArray[i], toArray[i]) ;
        return str ;
    }
    
    static private String identifierQuoteChar = "\"" ;
    static private String identifierQuoteChar2 = "\"\"" ;
    
    static public String quoteIdentifier(String name)
    {
        if ( sqlSafeChar(name) )
            return name ;
        // Check SQL-92
        name = name.replace(identifierQuoteChar, identifierQuoteChar2) ;
        return identifierQuoteChar+name+identifierQuoteChar ;
    }
    
    
    
    private static final String SQLmark = "_" ;
    
    // TODO Need per store gen(first, last) =>
    
    /** Separator used in SQL name generation.
     *  Not used as a leading character. 
     */ 
    public static String getSQLmark() { return SQLmark ; }
    
    public static String gen(String first, String last)
    { return first+SQLmark+last ; }
    
    public static String gen(String first)
    { return first+SQLmark ; }
    
    // Not needed - we don't use the SPARQL name to generate the SQL name
    // anymore but instead allocate a plain name and remember the mapping (Dec 2006)
//    /** Map a SPARQL variable to an SQL identifier.
//     * @param var 
//     * @return String   The SQL identifier
//     */
//    static public String varToSqlId(Var var)
//    { 
//        return quoteIdentifier(var.getName()) ;
//    }
//
//    /** Map an SQL identifier to a SPARQL variable
//     * @param sqlName   The SQL identifier 
//     * @return Var 
//     */
//    static public Var sqlIdToVar(String sqlName)
//    {
//        if ( sqlName.startsWith("'") )
//            return null ;
//        return Var.alloc(sqlName) ;
//    }
    
    private static boolean sqlSafeChar(String str)
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

    private static boolean isLowerCaseSqlChar(char ch)
    {
        return ch >= 'a' && ch <= 'z' ;
    }
    
    private static boolean isSqlDigit(char ch)
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
            LogFactory.getLog(SQLUtils.class).warn("Failed to convert "+lex, e) ;
            return "0000-00-00 00:00:00" ;
        }
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */