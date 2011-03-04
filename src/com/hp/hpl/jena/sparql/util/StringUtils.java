/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.text.DateFormat ;
import java.text.DecimalFormat ;
import java.text.NumberFormat ;
import java.text.SimpleDateFormat ;
import java.util.Date ;
import java.util.List ;
import java.util.regex.Pattern ;

import org.openjena.atlas.iterator.Iter ;

/** @see org.openjena.atlas.lib.StrUtils */
public class StringUtils
{
    static NumberFormat integerFormat = NumberFormat.getNumberInstance() ;
    public static String str(long v)
    {
        return integerFormat.format(v) ;
    }
    
    static DateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss") ;
    public static String str(Date date)
    {
        return dateTimeFormat.format(date) ;
    }

    static DecimalFormat decimalFormat = new DecimalFormat("#,##0.0") ;
    public static String str(float value)
    {
        return decimalFormat.format(value) ;
    }
    
    public static String str(double value)
    {
        return decimalFormat.format(value) ;
    }
    
    private static Pattern p = Pattern.compile("http:[^ \n]*[#/]([^/ \n]*)") ;
    /** Abbreviate, crudely, URI in strings, leaving only their last component. */ 
    public static String printAbbrev(Object obj)
    {
        if ( obj==null )
            return "<null>" ;
        String x = obj.toString() ;
        return p.matcher(x).replaceAll("::$1") ;
    }
    
    /** Abbreviate, crudely, URI in strings, leaving only their last component. */ 
    public static <T> String printAbbrevList(List<T> objs)
    {
        String x = Iter.asString(objs, "\n") ;
        return printAbbrev(x) ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
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