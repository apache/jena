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

package com.hp.hpl.jena.sparql.lib;

import java.io.IOException ;
import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.InvalidPropertiesFormatException ;
import java.util.List ;
import java.util.Properties ;

import org.apache.jena.atlas.lib.SystemUtils ;
import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.sparql.ARQException ;

/** Simple wrapper for reading metadata, once, from a system resource.
 *  Note that in some environments, it's tricky to get a class loader.  
 */

public class Metadata
{
    private List<String> resources = new ArrayList<>() ;
    private Properties properties = new Properties() ;
    
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
                Log.fatal(Metadata.class, "No classloader") ;
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
            Log.fatal(Metadata.class, "Unexpected Thorwable", ex) ;
            return ;
        }
    }

    public String get(String name) { return get(name, null) ; }
    
    public String get(String name, String defaultValue)
    {
        if ( properties == null ) return defaultValue ;
        return properties.getProperty(name, defaultValue) ;
    }

    public List<String> getResources()
    {
        return resources ;
    }

    public Properties getProperties()
    {
        return properties ;
    }
}
