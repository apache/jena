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

import junit.framework.TestSuite ;

import com.hp.hpl.jena.graph.Factory ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.impl.ReificationWrapperGraph ;
import com.hp.hpl.jena.mem.GraphMem ;
import com.hp.hpl.jena.shared.ReificationStyle ;

/**
    Tests for ReificationWrapper and hence ReificationWrapperGraph.
*/
public class TestReificationWrapper extends AbstractTestReifier
    {
    protected final Class<? extends Graph> graphClass;
    protected final ReificationStyle style;
    
    public TestReificationWrapper( Class<? extends Graph> graphClass, String name, ReificationStyle style ) 
        {
        super( name );
        this.graphClass = graphClass;
        this.style = style;
        }
        
    public static TestSuite suite()
        { 
        TestSuite result = new TestSuite();
        result.addTest( MetaTestGraph.suite( TestReificationWrapper.class, ReificationWrapperGraph.class, ReificationStyle.Standard ) );
        result.addTestSuite( TestReificationWrapperGraph.class );
        result.setName(TestReificationWrapper.class.getSimpleName());
        return result; 
        }       
    
    public static class TestReificationWrapperGraph extends AbstractTestGraph
        {
        public TestReificationWrapperGraph( String name )
            { super( name ); }
    
        @Override
        public Graph getGraph()
            {
            Graph base = Factory.createDefaultGraph();            
            return new ReificationWrapperGraph( base, ReificationStyle.Standard ); 
            }
        }

    @Override
    public Graph getGraph()
        { return getGraph( style );  }

    @Override
    public Graph getGraph( ReificationStyle style )
        { return new ReificationWrapperGraph( new GraphMem( Standard ), style );  }
    }
