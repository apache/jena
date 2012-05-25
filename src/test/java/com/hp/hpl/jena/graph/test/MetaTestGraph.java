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
@SuppressWarnings("deprecation")
public class MetaTestGraph extends AbstractTestGraph 
    {
    protected final Class<? extends Graph> graphClass;
    protected final ReificationStyle style;
    
	public MetaTestGraph( Class<? extends Graph> graphClass, String name, ReificationStyle style ) 
        {
		super( name );
        this.graphClass = graphClass;
        this.style = style;
        }
        
    public MetaTestGraph( String name )
        { super( name ); graphClass = null; style = null; }
     
    public static TestSuite suite()
        { return suite( MetaTestGraph.class, GraphMem.class, ReificationStyle.Minimal ); }
            
    /**
        Construct a suite of tests from the test class <code>testClass</code>
        by instantiating it three times, once each for the three reification styles,
        and applying it to the graph <code>graphClass</code>.
    */
    public static TestSuite suite( Class<? extends Test> testClass, Class<? extends Graph> graphClass )
        {
        TestSuite result = new TestSuite();
        result.addTest( suite( testClass, graphClass, ReificationStyle.Minimal ) ); 
        result.addTest( suite( testClass, graphClass, ReificationStyle.Standard ) ); 
        result.addTest( suite( testClass, graphClass, ReificationStyle.Convenient ) ); 
        result.setName("Meta "+testClass.getName());
        return result;    
        }
        
    public static TestSuite suite( Class<? extends Test> testClass, Class<? extends Graph> graphClass, ReificationStyle style )
        {
        TestSuite result = new TestSuite();
        for (Class<?> c = testClass; Test.class.isAssignableFrom( c ); c = c.getSuperclass())
            {
            Method [] methods = c.getDeclaredMethods();
            addTestMethods( result, testClass, methods, graphClass, style );  
            }
        result.setName(testClass.getName()+" "+style.toString());
        return result;    
        }
        
    public static void addTestMethods
        ( TestSuite result, Class<? extends Test> testClass, Method [] methods, Class<? extends Graph> graphClass, ReificationStyle style  )
        {
        for (int i = 0; i < methods.length; i += 1)
            if (isPublicTestMethod( methods[i] )) 
                result.addTest( makeTest( testClass, graphClass, methods[i].getName(), style ) );  
        }
        
    public static TestCase makeTest( Class<? extends Test> testClass, Class<? extends Graph> graphClass, String name, ReificationStyle style )
        {
        Constructor<?> cons = getConstructor( testClass, new Class[] {Class.class, String.class, ReificationStyle.class} );
        if (cons == null) throw new JenaException( "cannot find MetaTestGraph constructor" );
        try { return (TestCase) cons.newInstance( new Object [] {graphClass, name, style} ); }
        catch (Exception e) { throw new JenaException( e ); }
        }

	@Override public Graph getGraph() 
        { return getGraph( this, graphClass, style ); }
        
    }
