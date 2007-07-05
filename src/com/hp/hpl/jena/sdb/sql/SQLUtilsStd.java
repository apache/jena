/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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