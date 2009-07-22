/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lib;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;

import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.util.ALog;

/** Simple wrapper for reading metadata, once, from a system resource.
 *  Note that in some environments, it's tricky to get a class loader.  
 */

public class Metadata
{
    List<String> resources = new ArrayList<String>() ;
    Properties properties = new Properties() ;
    
    public Metadata() { }
    
    public Metadata(String resourceName)
    {
        this() ;
        addMetadata(resourceName) ;
    }
    
    public void addMetadata(String resourceName)
    {
        resources.add(resourceName) ;
        read(properties, resourceName) ;
    }
    
    // Protect all classloader choosing -- sometimes systems mess with even the system class loader.
    private static void read(Properties properties, String resourceName)
    {
        // Armour-plate this - classloaders and using them can be blocked by some environments.
        try { 
            ClassLoader classLoader = null ;

            try { classLoader = SystemUtils.chooseClassLoader() ; } catch (ARQException ex) {}

            if (classLoader == null)
            {
                try { classLoader = Metadata.class.getClassLoader(); } catch (ARQException ex) {}
            }

            if ( classLoader == null )
            {
                ALog.fatal(Metadata.class, "No classloader") ;
                return ;
            }

            InputStream in = classLoader.getResourceAsStream(resourceName) ;
            if ( in == null )
                //throw new ARQException("Failed to find the properties file") ;
                // In development, there is no properties file.
                return ;

            try { properties.loadFromXML(in) ; } 
            catch (InvalidPropertiesFormatException ex)
            { throw new ARQException("Invalid properties file", ex) ; }
            catch (IOException ex)
            { throw new ARQException("Metadata ==> IOException", ex) ; }
        }
        catch (Throwable ex)
        {
            ALog.fatal(Metadata.class, "Unexpected Thorwable", ex) ;
            return ;
        }
    }

    public String get(String name) { return get(name, null) ; }
    
    public String get(String name, String defaultValue)
    {
        if ( properties == null ) return defaultValue ;
        return properties.getProperty(name, defaultValue) ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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