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

package com.hp.hpl.jena.mem.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.util.iterator.*;

public class TestStoreSpeed extends GraphTestBase
    {
    private long began;
    
    public TestStoreSpeed( String name )
        {
        super( name );
        }

    public static void main( String [] args )
        {
        new TestStoreSpeed( "vladimir taltos" ) .gonzales( "subject StoreMem", Factory.createGraphMem() );
        new TestStoreSpeed( "vladimir taltos" ) .gonzales( "normal StoreMem", Factory.createGraphMem() );
        new TestStoreSpeed( "vladimir taltos" ) .gonzales( "GraphMem", Factory.createGraphMem() );
        new TestStoreSpeed( "vladimir taltos" ) .gonzales( "subject StoreMem", Factory.createGraphMem() );
        new TestStoreSpeed( "vladimir taltos" ) .gonzales( "normal StoreMem", Factory.createGraphMem() );
        new TestStoreSpeed( "vladimir taltos" ) .gonzales( "GraphMem", Factory.createGraphMem() );
        new TestStoreSpeed( "vladimir taltos" ) .gonzales( "subject StoreMem", Factory.createGraphMem() );
        new TestStoreSpeed( "vladimir taltos" ) .gonzales( "normal StoreMem", Factory.createGraphMem() );
        new TestStoreSpeed( "vladimir taltos" ) .gonzales( "GraphMem", Factory.createGraphMem() );
        }
        
    private void mark()
        {
        began = System.currentTimeMillis();
        }
        
    static final int COUNT = 100000;
    
    private Triple newt( int i )
        { return new Triple( node("s" + (i % 1000)), node("p" + ((i + 11) % 20)), node("s" + ((i + 131) % 1001) ) ); }
        
    private void makeTriples()
        { for (int i = 0; i < COUNT; i += 1) newt( i ); }
        
    private void fillGraph( Graph g )
        {
        for (int i = 0; i < COUNT; i += 1) g.add( newt( i ) );
        }
        
    private long ticktock( String title )
        {
        long ticks = System.currentTimeMillis() - began;
        System.err.println( "+ " + title + " took: " + ticks + "ms." );
        mark();
        return ticks;
        }
        
    private void consumeAll( Graph g )
        {
        int count = 0;
        ClosableIterator<Triple> it = g.find( node("s500"), null, null );
        while (it.hasNext()) { it.next(); count += 1; /* if (count %1000 == 0) System.err.print( (count / 1000) %10 ); */}
        // System.err.println( "| we have " + count + " triples." );
        // assertEquals( g.size(), count );
        }
        
    private void gonzales( String title, Graph g )
        {
        System.err.println( "" );
        System.err.println( "| gonzales: " + title );
        mark(); 
        makeTriples(); ticktock( "generating triples" );
        makeTriples(); ticktock( "generating triples again" );
        makeTriples(); long gen = ticktock( "generating triples yet again" );
        fillGraph( g ); long fill = ticktock( "filling graph" );
        System.err.println( "> insertion time: " + (fill - gen) );
        fillGraph( g ); ticktock( "adding the same triples again" );
        consumeAll( g ); ticktock( "counting s500 triples" );
        consumeAll( g ); ticktock( "counting s500 triples again" );
        consumeAll( g ); ticktock( "counting s500 triples yet again" );
        }
    }
