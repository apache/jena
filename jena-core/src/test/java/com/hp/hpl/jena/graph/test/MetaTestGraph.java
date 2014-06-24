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
import com.hp.hpl.jena.shared.*;

/**
	MetaTestGraph
*/
public class MetaTestGraph extends AbstractTestGraph 
    {
    protected final Class<? extends Graph> graphClass;
    
	public MetaTestGraph( Class<? extends Graph> graphClass, String name) 
        {
		super( name );
        this.graphClass = graphClass;
        }
        
    public MetaTestGraph( String name )
        { super( name ); graphClass = null; }
     
    /**
        Construct a suite of tests from the test class <code>testClass</code>
        by instantiating it three times, once each for the three reification styles,
        and applying it to the graph <code>graphClass</code>.
    */
    public static TestSuite suite( Class<? extends Test> testClass, Class<? extends Graph> graphClass )
        {
        TestSuite result = new TestSuite();
        result.addTest( suiteX( testClass, graphClass)); 
        result.setName("Meta "+testClass.getName());
        return result;    
        }
        
    public static TestSuite suiteX( Class<? extends Test> testClass, Class<? extends Graph> graphClass)
        {
        TestSuite result = new TestSuite();
        for (Class<?> c = testClass; Test.class.isAssignableFrom( c ); c = c.getSuperclass())
            {
            Method [] methods = c.getDeclaredMethods();
            addTestMethods( result, testClass, methods, graphClass );  
            }
        result.setName(testClass.getName());
        return result;    
        }
        
    public static void addTestMethods
        ( TestSuite result, Class<? extends Test> testClass, Method [] methods, Class<? extends Graph> graphClass)
        {
            for ( Method method : methods )
            {
                if ( isPublicTestMethod( method ) )
                {
                    result.addTest( makeTest( testClass, graphClass, method.getName() ) );
                }
            }
        }
        
    public static TestCase makeTest( Class<? extends Test> testClass, Class<? extends Graph> graphClass, String name)
        {
        Constructor<?> cons = getConstructor( testClass, new Class[] {Class.class, String.class} );
        if (cons == null) throw new JenaException( "cannot find MetaTestGraph constructor" );
        try { return (TestCase) cons.newInstance( new Object [] {graphClass, name} ); }
        catch (Exception e) { throw new JenaException( e ); }
        }

	@Override public Graph getGraph() 
        { return getGraph( this, graphClass); }
        
    }
