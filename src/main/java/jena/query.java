/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package jena;

import static jena.cmdline.CmdLineUtils.setLog4jConfiguration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class query
{

    static {
    	setLog4jConfiguration() ;
    }

    // Call-through to arq command line application
    public static void main(String[] args)
    {
        // Do this by reflection so it is not assumed that ARQ is available
        // at compile time.
        
        invokeCmd("arq.query", args) ;
    }
    
    public static void invokeCmd(String className, String[] args)
    {
        
        Class<?> cmd = null ;
        try { cmd = Class.forName(className) ; }
        catch (ClassNotFoundException ex)
        {
            System.err.println("Class '"+className+"' not found") ;
            System.exit(1) ;
        }
        
        Method method = null ;
        try { method = cmd.getMethod("main", new Class[]{String[].class}) ; }
        catch (NoSuchMethodException ex)
        {
            System.err.println("'main' not found but the class '"+className+"' was") ;
            System.exit(1) ;
        }
        
        try 
        {
            method.invoke(null, new Object[]{args}) ;
            return ;
        } catch (IllegalArgumentException ex)
        {
            System.err.println("IllegalArgumentException exception: "+ex.getMessage());
            System.exit(7) ;
        } catch (IllegalAccessException ex)
        {
            System.err.println("IllegalAccessException exception: "+ex.getMessage());
            System.exit(8) ;
        } catch (InvocationTargetException ex)
        {
            System.err.println("InvocationTargetException exception: "+ex.getMessage());
            System.exit(9) ;
        }

        
        //arq.query.main(args) ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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