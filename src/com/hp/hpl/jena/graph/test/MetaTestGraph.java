/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: MetaTestGraph.java,v 1.1 2003-09-12 15:05:17 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

import java.lang.reflect.*;

import junit.framework.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.shared.*;

/**
	MetaTestGraph

	@author kers
*/
public class MetaTestGraph extends AbstractTestGraph 
    {
    protected final Class graphClass;
    protected final ReificationStyle style;
    
	public MetaTestGraph( Class graphClass, String name, ReificationStyle style ) 
        {
		super( name );
        this.graphClass = graphClass;
        this.style = style;
	   }

    static final Method [] methods = AbstractTestGraph.class.getDeclaredMethods();
    
    public static TestSuite suite( Class graphClass )
        {
        TestSuite result = new TestSuite();
        result.addTest( suite( graphClass, ReificationStyle.Minimal ) ); 
        result.addTest( suite( graphClass, ReificationStyle.Standard ) ); 
        result.addTest( suite( graphClass, ReificationStyle.Convenient ) ); 
        return result;    
        }
        
    public static TestSuite suite( Class graphClass, ReificationStyle style )
        {
        TestSuite result = new TestSuite();
		addTestMethods( result, methods, graphClass, style );   
        return result;    
        }
        
    public static void addTestMethods
        ( TestSuite result, Method [] methods, Class graphClass, ReificationStyle style  )
        {
        for (int i = 0; i < methods.length; i += 1)
            if (isPublicTestMethod( methods[i] )) 
                result.addTest( new MetaTestGraph( graphClass, methods[i].getName(), style ) );  
        }

    public static boolean isPublicTestMethod(Method m) {
        return isTestMethod(m) && Modifier.isPublic(m.getModifiers());
     }
     
    public static boolean isTestMethod(Method m) {
        String name= m.getName();
        Class[] parameters= m.getParameterTypes();
        Class returnType= m.getReturnType();
        return parameters.length == 0 && name.startsWith("test") && returnType.equals(Void.TYPE);
     }
             
    public static TestSuite suite()
        { return suite( GraphMem.class, ReificationStyle.Minimal ); }
        
	public Graph getGraph() 
        {
        try
            {
            Constructor cons = graphClass.getConstructor( new Class[] { ReificationStyle.class } );
            return (Graph) cons.newInstance( new Object[] { style } );
            }
        catch (Exception e)
            { throw new JenaException( e ); }
        }

    }

/*
    (c) Copyright 2003, Hewlett-Packard Development Company, LP
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