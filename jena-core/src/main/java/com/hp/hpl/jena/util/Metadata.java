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

package com.hp.hpl.jena.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList ;
import java.util.InvalidPropertiesFormatException;
import java.util.List ;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.shared.JenaException;

/** 
 * Pluck data out of the ether - or failing that, read it from a properties file.
 * Assumes the properties file is in the "right place" through configuration of the build or compile processes.
 */
public class Metadata 
{
    private static Logger log =  LoggerFactory.getLogger(Metadata.class) ;
    List<String> resources = new ArrayList<>() ;
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
    
    private static void read(Properties properties, String resource)
    {
        ClassLoader classLoader = SystemUtils.chooseClassLoader() ;
        if (classLoader == null)
            classLoader = Metadata.class.getClassLoader();

        if ( classLoader == null )
        {
            log.error( "No classloader") ;
            return ;
        }

        InputStream in = classLoader.getResourceAsStream ( resource ) ;
        if ( in == null )
            // throw new JenaException ( "Failed to find the properties file" ) ;
            // In development, there is no properties file.
            return ;
        try
        {
            properties.loadFromXML ( in ) ;
        } 
        catch ( InvalidPropertiesFormatException ex )
        { 
            throw new JenaException ( "Invalid properties file", ex ) ; 
        }
        catch ( IOException ex )
        { 
            throw new JenaException ( "Metadata ==> IOException", ex ) ; 
        }
    }
    
    public String get(String name) { return get(name, null) ; }
    
    public String get(String name, String defaultValue)
    {
        if ( properties == null ) return defaultValue ;
        return properties.getProperty(name, defaultValue) ;
    }

}
