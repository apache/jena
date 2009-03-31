/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lib;

import java.io.IOException;
import java.io.InputStream;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import com.hp.hpl.jena.sparql.ARQException;

/** Pluck data out of the ether - or failing that, read it from a properties file.
 *  Assumes the properties file is in the "right place" through configuration of
 *  the build or compile processes.
 * @author Andy Seaborne
 */
public class Metadata
{
    static boolean initialized = false ; 
    static Properties properties = null ;
    
    static public void setMetadata(String resourceName)
    {
        resource = resourceName ;
    }
    
    static String resource = null ;
    private static void init()
    {
        if ( ! initialized )
        {
            initialized = true ;
            properties = new Properties() ;
            InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(resource) ;
            if ( in == null )
                //throw new TDBException("Failed to find the properties file") ;
                return ;
            try
            {
                properties.loadFromXML(in) ;
            } 
            catch (InvalidPropertiesFormatException ex)
            { throw new ARQException("Invalid properties file", ex) ; }
            catch (IOException ex)
            { throw new ARQException("Metadata ==> IOException", ex) ; }
        }
    }
    
    public static String get(String name) { return get(name, null) ; }
    
    public static String get(String name, String defaultValue)
    {
        init() ;
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