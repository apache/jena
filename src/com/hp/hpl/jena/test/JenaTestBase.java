/*
  (c) Copyright 2002, 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: JenaTestBase.java,v 1.6 2003-09-22 12:16:35 chris-dollin Exp $
*/

package com.hp.hpl.jena.test;

import com.hp.hpl.jena.shared.JenaException;

import java.io.*;
import java.lang.reflect.*;

import junit.framework.*;

/**
    A basis for Jena test cases which provides assertFalse and assertDiffer.
    Often the logic of the names is clearer than using a negation (well, Chris
    thinks so anyway).
    
 	@author kers
*/
public class JenaTestBase extends TestCase
    {
    public JenaTestBase( String name )
        { super( name ); }
        
    /**
        assert that the two objects must be unequal according to .equals().
        @param title a labelling string for the assertion failure text
        @param x an object to test; the subject of a .equals()
        @param y the other object; the argument of the .equals()
    */
    public static void assertDiffer( String title, Object x, Object y )
        { 
        if (x.equals( y ))
            fail( (title == null ? "objects should be different, but both were: " : title) + x );
        }
        
    /**
        assert that the two objects must be unequal according to .equals().
        @param x an object to test; the subject of a .equals()
        @param y the other object; the argument of the .equals()
    */
    public static void assertDiffer( Object x, Object y )
        { assertDiffer( null, x, y ); }
        
    /**
        Do nothing; a way of notating that a test has succeeded, useful in the body of a
        catch-block to silence excessively [un]helpful disgnostics. 
    */
    public static void pass()
        {}
        
    /**
        create a temporary file that will be deleted on exit, and do something
        sensible with any IO exceptions - namely, throw them up wrapped in
        a JenaException.
    
        @param prefix the prefix for File.createTempFile
        @param suffix the suffix for File.createTempFile
        @return the temporary File
    */
    public static  File tempFileName( String prefix, String suffix )
        {
        File result = new File( getTempDirectory(), prefix + randomNumber() + suffix );
        if (result.exists()) return tempFileName( prefix, suffix );
        result.deleteOnExit();
        return result;
        }  

    private static int counter = 0;

    private static int randomNumber()
        {
        return ++counter;
        }
 
    /**
        Answer a File naming a freshly-created directory in the temporary directory. This
        directory should be deleted on exit.
        TODO handle threading issues, mkdir failure, and better cleanup
        
        @param prefix the prefix for the directory name
        @return a File naming the new directory
     */
    public static File getScratchDirectory( String prefix )
        {
        File result = new File( getTempDirectory(), prefix + randomNumber() );
        if (result.exists()) return getScratchDirectory( prefix );
        assertTrue( "make temp directory", result.mkdir() );
        result.deleteOnExit();
        return result;   
        } 
        
    public static String getTempDirectory()
        { return temp; }
    
    private static String temp = constructTempDirectory();

    private static String constructTempDirectory()
        {
        try 
            { 
            File x = File.createTempFile( "xxx", ".none" );
            x.delete();
            return x.getParent(); 
            }
        catch (IOException e) 
            { throw new JenaException( e ); }
        }
                 
    /**
        Answer the constructor of the class <code>c</code> which takes arguments of the
        type(s) in <code>args</code>, or <code>null</code> if there isn't one.
     */
    public static Constructor getConstructor( Class c, Class [] args )
        {
        try { return c.getConstructor( args ); }
        catch (NoSuchMethodException e) { return null; }
        }

    /**
        Answer true iff the method <code>m</code> is a public method which fits the
        pattern of being a test method, ie, test*() returning void.
     */
    public static boolean isPublicTestMethod( Method m ) 
        { return Modifier.isPublic( m.getModifiers() ) && isTestMethod( m ); }
     
    /**
        Answer true iff the method <code>m</code> has a name starting "test", takes no
        arguments, and returns void; must catch junit tests, in other words.
    */
    public static boolean isTestMethod( Method m ) 
        { return 
            m.getName().startsWith( "test" ) 
            && m.getParameterTypes().length == 0 
            && m.getReturnType().equals( Void.TYPE ); }                        
    }


/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/