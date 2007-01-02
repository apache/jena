/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class sdbscript
{
    private static Log log = LogFactory.getLog(sdbscript.class) ;
    
    public static void main(String[] a)
    {
        // TODO Make engine independent (via sdb.ttl?)
        if ( a.length == 0 )
            a = new String[]{ "script.rb" } ;

        staticByReflection("org.jruby.Main", "main", a) ;
    }
    
    private static void staticByReflection(String className, String methodName, String[] args)
    {
        // Reflection to invoke <class>.suite() and return a TestSuite.
        Class<?> cmd = null ;
        try { cmd = Class.forName(className) ; }
        catch (ClassNotFoundException ex)
        {
            log.fatal(String.format("Class not found: %s", className)) ;
            return  ; 
        }

        Method method = null ;
        try { method = cmd.getMethod(methodName, new Class[]{args.getClass()}) ; }
        catch (NoSuchMethodException ex)
        {
            log.fatal(String.format("Class '%s' found but not the method '%s'",
                                    className, methodName)) ;
            return ;
        }

        try 
        {
            method.invoke(null, (Object)args) ;
        } catch (Exception ex)
        {
            log.fatal(String.format("Exception invoking '%s.%s'",  className, methodName), ex) ;
            return ;
        }
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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