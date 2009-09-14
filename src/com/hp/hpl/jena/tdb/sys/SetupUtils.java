/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.sys;

import org.slf4j.Logger ;

import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.file.MetaFile ;

public class SetupUtils
{

    public static String get(MetaFile metafile, String key)
    {
        return metafile.getProperty(key) ;
    }

    public static String[] getSplit(MetaFile metafile, String key)
    {
        return metafile.getProperty(key).split(",") ;
    }

    public static boolean propertyEquals(MetaFile metafile, String key, String value)
    {
        return metafile.getProperty(key).equals(value) ;
    }

    public static void ensurePropertySet(MetaFile metafile, String key, String expected)
    {
        getOrSetDefault(metafile, key, expected) ;
    }

    // Get property - set the defaultvalue if not present.
    public static String getOrSetDefault(MetaFile metafile, String key, String expected)
    {
        String x = metafile.getProperty(key) ;
        if ( x == null )
        {
            metafile.setProperty(key, expected) ;
            x = expected ;
        }
        return x ;
    }

    // Check property is an expected value or set if missing
    public static void checkOrSetMetadata(MetaFile metafile, String key, String expected)
    {
        String x = metafile.getProperty(key) ;
        if ( x == null )
        {
            metafile.setProperty(key, expected) ;
            return ; 
        }
        if ( x.equals(expected) )
            return ;
        
        inconsistent(key, x, expected) ; 
    }

    public static void checkMetadata(MetaFile metafile, String key, String expected)
    {
        //log.debug("checkMetaData["+key+","+expected+"]") ;
        
        String value = metafile.getProperty(key) ;
        if ( value == null && expected == null ) return ;
        if ( value == null && expected != null ) inconsistent(key, value, expected) ; 
        if ( value != null && expected == null ) inconsistent(key, value, expected) ; 
        if ( ! value.equals(expected) )          inconsistent(key, value, expected) ;
        
    }

    public static void inconsistent(String key, String actual, String expected) 
    {
        String msg = String.format("Inconsistent: key=%s value=%s expected=%s", 
                                   key, 
                                   (actual==null?"<null>":actual),
                                   (expected==null?"<null>":expected) ) ;
        throw new TDBException(msg) ; 
    }

    public static void error(Logger log, String msg)
    {
        log.error(msg) ;
        throw new TDBException(msg) ;
    }

    public static int parseInt(String str, String messageBase)
    {
        try { return Integer.parseInt(str) ; }
        catch (NumberFormatException ex) { error(Setup.log, messageBase+": "+str) ; return -1 ; }
    }

}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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