/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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