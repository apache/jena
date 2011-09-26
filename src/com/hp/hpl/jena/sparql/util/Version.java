/**
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

import org.openjena.atlas.io.IndentedWriter ;

/** Manage version information for subsystems */
public class Version
{
    private List<Class< ? >> classes = new ArrayList<Class< ? >>() ; 
    public void addClass(Class< ? > c)
    {
        if ( ! classes.contains(c) ) 
            classes.add(c) ;
    }
    
    private static String[] fields = { /*"NAME",*/ "VERSION", "BUILD_DATE" } ;

    public void print()
    {    
        for ( Iterator<Class<?>> iter = classes.iterator() ; iter.hasNext() ; )
        {
            Class<?> c = iter.next();
            String x = Utils.classShortName(c) ;
            fields(x, c) ;    
        }
    }
    
    private static void fields(String prefix, Class< ? > cls)
    {
        for (int i=0; i < fields.length; i++)
            printField(IndentedWriter.stdout, prefix, fields[i], cls) ;
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
