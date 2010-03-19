/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.util.FileUtils;

public class PropertyUtils
{
    /** Java5 does not have read/write from readers/writers - needed for UTF-8 */ 
    
    static public Properties loadFromFile(String filename) throws IOException
    {
        Properties properties = new Properties() ;
        loadFromFile(properties, filename) ;
        return properties ;
    }
    
    static public void loadFromFile(Properties properties, String filename) throws IOException
    {
        String x = FileUtils.readWholeFileAsUTF8(filename) ;
        byte b[] = x.getBytes(FileUtils.encodingUTF8) ;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(b);
        properties.load(inputStream) ;
    }
    
    static public void storeToFile(Properties properties, String comment, String filename) throws IOException
    {
        String str = comment ;
        if ( str == null )
            str = filename ;
        FileOutputStream fos = new FileOutputStream(filename) ;
//        Writer w = FileUtils.asUTF8(fos) ;
//        w = new BufferedWriter(w) ;
//        //properties.store(w, "Metadata: "+str) ;   // Java6.
        // Warning - not UTF-8 safe.
        properties.store(fos, str) ;
        fos.close() ;
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
        return Utils.equal(properties.getProperty(key), value) ;
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

    /** Check property has teh vakue given - throw exception if not. */
    public void checkMetadata(Properties properties, String key, String expected)
    {
        String value = properties.getProperty(key) ;
        
        if ( ! Utils.equal(value, value) )
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