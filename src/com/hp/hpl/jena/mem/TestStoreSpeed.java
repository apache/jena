/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestStoreSpeed.java,v 1.3 2003-07-24 15:29:31 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem;

/**
	@author kers
*/

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
        new TestStoreSpeed( "vladimir taltos" ) .gonzales( "subject StoreMem", new GraphMem() );
        new TestStoreSpeed( "vladimir taltos" ) .gonzales( "normal StoreMem", new GraphMem() );
        new TestStoreSpeed( "vladimir taltos" ) .gonzales( "GraphMem", new GraphMem() );
        new TestStoreSpeed( "vladimir taltos" ) .gonzales( "subject StoreMem", new GraphMem() );
        new TestStoreSpeed( "vladimir taltos" ) .gonzales( "normal StoreMem", new GraphMem() );
        new TestStoreSpeed( "vladimir taltos" ) .gonzales( "GraphMem", new GraphMem() );
        new TestStoreSpeed( "vladimir taltos" ) .gonzales( "subject StoreMem", new GraphMem() );
        new TestStoreSpeed( "vladimir taltos" ) .gonzales( "normal StoreMem", new GraphMem() );
        new TestStoreSpeed( "vladimir taltos" ) .gonzales( "GraphMem", new GraphMem() );
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
        ClosableIterator it = g.find( node("s500"), null, null );
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

/*
    (c) Copyright Hewlett-Packard Company 2002
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
