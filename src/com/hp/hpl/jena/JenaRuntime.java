/*
 * (c) Copyright 2004, Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena;

import java.util.* ;
import java.security.AccessController;

/** Methods and constants that define features of the curren the environment.
 *  Primarily for other parts of the Jena framework. 
 * 
 * @author Andy Seaborne
 * @version $Id: JenaRuntime.java,v 1.1 2004-07-06 13:36:58 andy_seaborne Exp $
 */

public class JenaRuntime
{
    /** The JVM does not implement java.security (correctly) */
    public static final String featureNoSecurity = "http://jena.hpl.hp.com/2004/07/feature/noSecurity" ;
    
    /** The JVM does not implement java.nio.charset.Charset operations (correctly) */
    public static final String featureNoCharset = "http://jena.hpl.hp.com/2004/07/feature/noCharset" ; 
        
    static Map features = new HashMap() ;
    static {
        if ( System.getProperty(featureNoSecurity) != null )
            features.put(featureNoSecurity, "true") ;
        if ( System.getProperty(featureNoCharset) != null )
            features.put(featureNoCharset, "true") ;
    }
    
    static boolean runUnder(String featureName) { return true ; }
    static boolean runNotUnder(String featureName) { return true ; }
    
    
    static final String lineSeparator = getSystemProperty("line.separator", "\n") ; 
    public static String getLineSeparator()
    {
        return lineSeparator ;
    }
    
    public static String getSystemProperty(String propName)
    {
        return getSystemProperty(propName, null) ;
    }

    public static String getSystemProperty(String propName, String defaultValue)
    {
        try {
            return System.getProperty(propName, defaultValue) ;
        } catch (SecurityException ex)
        {
            if ( runUnder(featureNoSecurity))
                return defaultValue ;
            try {
                Object x = AccessController.doPrivileged(
                    new sun.security.action.GetPropertyAction(propName));
                return (String)x ;
            } catch (Exception ex2)
            {
                // Give up
                return defaultValue ;
            }
        }
    }
    
}

/*
 * (c) Copyright 2004 Hewlett-Packard Development Company, LP
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