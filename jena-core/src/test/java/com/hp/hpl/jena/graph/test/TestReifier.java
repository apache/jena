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

import java.lang.reflect.Constructor ;

import junit.framework.TestSuite ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.mem.GraphMem ;
import com.hp.hpl.jena.shared.JenaException ;

/**
    This class tests the reifiers of ordinary GraphMem graphs.
    Old test suite - kept to ensure compatibility for teh one and only Standard mode 
*/

public class TestReifier extends AbstractTestReifier
    {
    public TestReifier( String name )
        { super( name ); graphClass = null; }
        
    protected final Class<? extends Graph> graphClass;
    
    public TestReifier( Class<? extends Graph> graphClass, String name) 
        {
        super( name );
        this.graphClass = graphClass;
        }
        
    public static TestSuite suite()
        { 
        TestSuite result = new TestSuite();
        result.addTest( MetaTestGraph.suite( TestReifier.class, GraphMem.class ) );
        result.setName(TestReifier.class.getSimpleName());
        return result; 
        }   
        
    @Override public Graph getGraph( ) 
        {
        try
            {
            Constructor<?> cons = getConstructor( graphClass, new Class[] {} );
            if (cons != null) return (Graph) cons.newInstance();
            Constructor<?> cons2 = getConstructor( graphClass, new Class [] {this.getClass()} );
            if (cons2 != null) return (Graph) cons2.newInstance( this );
            throw new JenaException( "no suitable graph constructor found for " + graphClass );
            }
        catch (RuntimeException e)
            { throw e; }
        catch (Exception e)
            { throw new JenaException( e ); }
        }        
    }
