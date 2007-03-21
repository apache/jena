/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.test;

import java.lang.reflect.Method;

import junit.framework.TestSuite;

/** This class provides an indiret way to get the ARQ test suite.
 *   This is done to remove a Jena compile-time dependency on ARQ. 
 * @author Andy Seaborne
 * @version $Id: TestPackageARQ.java,v 1.3 2007-03-21 21:27:01 andy_seaborne Exp $
 */ 

public class TestPackageARQ extends TestSuite
{
    public static TestSuite suite()
    {
        return suiteByReflection( "com.hp.hpl.jena.sparql.test.ARQTestSuite" );
    }

    private static TestSuite suiteByReflection(String className)
    {
        // Reflection to invoke <class>.suite() and return a TestSuite.
        Class cmd = null ;
        try { cmd = Class.forName(className) ; }
        catch (ClassNotFoundException ex)
        {
            return null ; 
        }
        
        Method method = null ;
        try { method = cmd.getMethod("suite", new Class[]{}) ; }
        catch (NoSuchMethodException ex)
        {
            System.err.println("'suite' not found but the class '"+className+"' was") ;
            return null ;
        }
        
        try 
        {
            return (TestSuite)method.invoke(null, new Object[]{}) ;
        } catch (Exception ex)
        {
            System.err.println("Failed to invoke static method 'suite'"+ex.getMessage()) ;
            ex.printStackTrace(System.err) ;
        }
        return null ;
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