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

package com.hp.hpl.jena.sparql.util;

import java.lang.reflect.Field ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.IndentedWriter ;

/** Manage version information for subsystems */
public class Version
{
    private List<Class< ? >> classes = new ArrayList<>() ;
    
    /**
     * Add a class to the version information
     * @param c Class
     */
    public void addClass(Class< ? > c)
    {
        if ( ! classes.contains(c) ) 
            classes.add(c) ;
    }
    
    private static String FIELD_VERSION = "VERSION";
    
    private static String FIELD_BUILD_DATE = "BUILD_DATE";
    
    private static String[] fields = { /*"NAME",*/ FIELD_VERSION, FIELD_BUILD_DATE } ;

    /**
     * Prints version information for all registered classes to Standard Out
     */
    public void print() {
        print(IndentedWriter.stdout);
    }
    
    /**
     * Prints version information for all registered classes to the given writer
     * @param writer Writer to print version information to
     */
    public void print(IndentedWriter writer)
    {
        for ( Class<?> c : classes )
        {
            String x = Utils.classShortName( c );
            fields( writer, x, c );
        }
    }
    
    /**
     * Gets user friendly version information for all registered classes as a string
     * @param singleLine Whether to print to a single line
     * @return Version information
     */
    public String toString(boolean singleLine) {
        try ( IndentedLineBuffer buffer = new IndentedLineBuffer(false) ) {
            Iterator<Class<?>> iter = classes.iterator();
            while (iter.hasNext())
            {
                Class<?> c = iter.next();
                String component = Utils.classShortName(c) ;
                String version = field(FIELD_VERSION, c);
                String timestamp = field(FIELD_BUILD_DATE, c);
                buffer.append("%s Version %s (Built %s)", component, version, timestamp);
                if (iter.hasNext()) {
                    if (!singleLine) {
                        buffer.println();
                    } else {
                        buffer.print(", ");
                    }
                }
            }

            return buffer.asString();
        }
    }
    
    /**
     * Gets user friendly version information for all registered classes as a string
     */
    @Override
    public String toString() {
        return this.toString(false);
    }
    
    private static void fields(IndentedWriter writer, String prefix, Class< ? > cls)
    {
        for ( String field : fields )
        {
            printField( writer, prefix, field, cls );
        }
    }
    
    private static String field(String fieldName, Class< ? > cls)
    {
        try
        {
            Field f = cls.getDeclaredField(fieldName) ;
            return f.get(null).toString() ;
        } catch (IllegalArgumentException ex)
        {
            ex.printStackTrace();
        } catch (IllegalAccessException ex)
        {
            ex.printStackTrace();
        } catch (SecurityException ex)
        {
            ex.printStackTrace();
        } catch (NoSuchFieldException ex)
        {
            ex.printStackTrace();
        }
        return "<error>" ;
    }
        
    private static void printField(IndentedWriter out, String prefix, String fieldName, Class< ? > cls)
    {
        out.print(prefix) ;
        out.print(": ") ;
        out.pad(12) ;
        out.print(fieldName) ;
        out.print(": ") ;
        out.print(field(fieldName, cls)) ;
        out.println() ;
        out.flush();
    }
}
