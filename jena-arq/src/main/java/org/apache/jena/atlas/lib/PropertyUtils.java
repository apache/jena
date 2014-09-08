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

package org.apache.jena.atlas.lib;

import java.io.* ;
import java.util.Objects ;
import java.util.Properties ;

import org.apache.jena.atlas.AtlasException ;

import com.hp.hpl.jena.util.FileUtils ;

public class PropertyUtils
{
    /** Load properties from a file if the file exists */ 
    static public Properties loadFromFile(String filename) throws IOException
    {
        Properties properties = new Properties() ;
        loadFromFile(properties, filename) ;
        return properties ;
    }
    
    /** Load properties from a file if the file exists */ 
    static public void loadFromFile(Properties properties, String filename) throws IOException
    {
        Objects.requireNonNull(filename, "File name must not be null") ;
        if ( "-".equals(filename) )
            throw new IllegalArgumentException("Filename is \"-\" (stdin not supported)") ;

        try (InputStream in = new FileInputStream(filename); Reader r = FileUtils.asBufferedUTF8(in);) {
            properties.load(r) ;
        }
    }
    
    static public void storeToFile(Properties properties, String comment, String filename) throws IOException
    {
        String str = comment ;
        if ( str == null )
            str = filename ;
        try(FileOutputStream fos = new FileOutputStream(filename)) {
            Writer w = FileUtils.asUTF8(fos) ;
            w = new BufferedWriter(w) ;
            properties.store(w, "Metadata: "+str) ;
        }
    }
    
    public static int getPropertyAsInteger(Properties properties, String key)
    {
        String x = properties.getProperty(key) ;
        if ( x == null )
            throw new AtlasException("No such property key: "+key) ;
        return Integer.parseInt(x) ;
    }
    
    public static int getPropertyAsInteger(Properties properties, String key, int defaultValue)
    {
        String x = properties.getProperty(key) ;
        if ( x == null )
            return defaultValue ;
        return Integer.parseInt(x) ;
    }

    public static boolean getPropertyAsBoolean(Properties properties, String key, boolean dftValue)
    {
        String x = properties.getProperty(key) ;
        if ( x == null )
            return dftValue ;
        if ( x.equalsIgnoreCase("true") ) return true ;
        if ( x.equalsIgnoreCase("false") ) return true ;
        throw new AtlasException("Value '"+x+"'not recognized for "+key) ;
    }
    
    public static Boolean getPropertyAsBoolean(Properties properties, String key)
    {
        String x = properties.getProperty(key) ;
        if ( x == null )
            throw new AtlasException("No such property key: "+key) ;
        if ( x.equalsIgnoreCase("true") ) return true ;
        if ( x.equalsIgnoreCase("false") ) return true ;
        throw new AtlasException("Value '"+x+"'not recognized for "+key) ;
    }

    
    /** Test whether a property has a value.  Null tests equal to not present. */
    public boolean propertyEquals(Properties properties, String key, String value)
    {
        return Lib.equal(properties.getProperty(key), value) ;
    }

    /** Set property if not already set. */
    public void ensurePropertySet(Properties properties, String key, String expected)
    {
        getOrSetDefault(properties, key, expected) ;
    }

    /** Get property or the default value - also set the default value if not present */
    public String getOrSetDefault(Properties properties, String key, String expected)
    {
        String x = properties.getProperty(key) ;
        if ( x == null )
        {
            properties.setProperty(key, expected) ;
            x = expected ;
        }
        return x ;
    }
    
    /** Check property is an expected value or set if missing */
    public void checkOrSetProperty(Properties properties, String key, String expected)
    {
        String x = properties.getProperty(key) ;
        if ( x == null )
        {
            properties.setProperty(key, expected) ;
            return ; 
        }
        if ( x.equals(expected) )
            return ;
        
        inconsistent(properties, key, x, expected) ; 
    }

    /** Check property has the vakue given - throw exception if not. */
    public void checkMetadata(Properties properties, String key, String expected)
    {
        String value = properties.getProperty(key) ;
        
        if ( ! Lib.equal(value, value) )
            inconsistent(properties, key, value, expected) ;
    }

    private void inconsistent(Properties properties, String key, String actual, String expected)
    {
        String msg = String.format("Inconsistent: key=%s value=%s expected=%s", 
                                   key, 
                                   (actual==null?"<null>":actual),
                                   (expected==null?"<null>":expected) ) ;
        throw new AtlasException(msg) ; 
    }


}
