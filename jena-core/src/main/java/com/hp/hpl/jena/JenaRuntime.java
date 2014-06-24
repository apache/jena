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

package com.hp.hpl.jena;

import java.util.* ;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.hp.hpl.jena.util.Metadata ;

/** Methods and constants that define features of the current the environment.
 *  Primarily for other parts of the Jena framework. 
 */

public class JenaRuntime
{
    private static String metadataLocation = "org/apache/jena/jena-properties.xml" ;
    private static Metadata metadata = new Metadata(metadataLocation) ;
    public static String getMetadata(String key, String defaultValue) { return metadata.get(key, defaultValue) ; }
    
    /** The JVM does not implement java.security (correctly) */
    public static final String featureNoSecurity = "http://jena.hpl.hp.com/2004/07/feature/noSecurity" ;
    
    /** The JVM does not implement java.nio.charset.Charset operations (correctly) */
    public static final String featureNoCharset = "http://jena.hpl.hp.com/2004/07/feature/noCharset" ; 
        
    static Map<String, String> features = new HashMap<>() ;
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
