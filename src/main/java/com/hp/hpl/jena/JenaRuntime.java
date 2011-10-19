/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena;

import java.util.* ;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.hp.hpl.jena.util.Metadata ;

/** Methods and constants that define features of the current the environment.
 *  Primarily for other parts of the Jena framework. 
 * 
 * @author Andy Seaborne
 * @version $Id: JenaRuntime.java,v 1.2 2009-10-11 16:56:52 andy_seaborne Exp $
 */

public class JenaRuntime
{
    private static String metadataLocation = "com/hp/hpl/jena/jena-properties.xml" ;
    private static Metadata metadata = new Metadata(metadataLocation) ;
    public static String getMetadata(String key, String defaultValue) { return metadata.get(key, defaultValue) ; }
    
    /** The JVM does not implement java.security (correctly) */
    public static final String featureNoSecurity = "http://jena.hpl.hp.com/2004/07/feature/noSecurity" ;
    
    /** The JVM does not implement java.nio.charset.Charset operations (correctly) */
    public static final String featureNoCharset = "http://jena.hpl.hp.com/2004/07/feature/noCharset" ; 
        
    static Map<String, String> features = new HashMap<String, String>() ;
    static {
            // Note getSystemProperty uses featureNoSecurity but works if it
            // has not been initialized
            if ( getSystemProperty(featureNoSecurity) != null )
                setFeature(featureNoSecurity) ;
            
            if ( getSystemProperty(featureNoCharset) != null )
                setFeature(featureNoCharset) ;
    }
    
    public static void setFeature(String featureName) { features.put(featureName, "true") ; }
    public static boolean runUnder(String featureName) { return features.containsKey(featureName) ; }
    public static boolean runNotUnder(String featureName) { return ! features.containsKey(featureName) ; }
    
    
    static final String lineSeparator = getSystemProperty("line.separator", "\n") ; 
    public static String getLineSeparator()
    {
        return lineSeparator ;
    }
    
    public static String getSystemProperty(String propName)
    {
        return getSystemProperty(propName, null) ;
    }

    public static String getSystemProperty(final String propName, String defaultValue)
    {
        try {
            return System.getProperty(propName, defaultValue) ;
        } catch (SecurityException ex)
        {
            if ( runUnder(featureNoSecurity))
                return defaultValue ;
            try {
                PrivilegedAction<String> a = new PrivilegedAction<String>() {
                    @Override
                    public String run() {
                        return System.getProperty(propName);
                    }
                } ;
                return AccessController.doPrivileged(a) ;
            } catch (Exception ex2)
            {
                // Give up
                return defaultValue ;
            }
        }
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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