/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestFileGraph.java,v 1.2 2003-05-03 16:53:21 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;

import java.io.*;

import junit.framework.*;

/**
    Test FileGraph by seeing if we can make some file graphs and then read
    them back.
    
 	@author hedgehog
*/

public class TestFileGraph extends GraphTestBase
    {
    public TestFileGraph( String name )
        { super( name ); }
        
    public static TestSuite suite()
        { return new TestSuite( TestFileGraph.class ); }
        
    /**
        Test that the language code is guessed "correctly".
    */
    public void testGuessLang()
        {
        assertEquals( "N3", FileGraph.guessLang( "hello.there.n3") );
        assertEquals( "N-TRIPLE", FileGraph.guessLang( "whats.up.nt" ) );
        assertEquals( "RDF/XML", FileGraph.guessLang( "dotless" ) );
        }
        
    public void testA()
        { testReadback( "x /R y", "xxxA", ".rdf" ); }
        
    public void testB()
        { testReadback( "x /R y", "xxxB", ".n3" ); }
        
    public void testC()
        { testReadback( "x /R y", "xxxC", ".nt" ); }
    
    public void testD()
        { testReadback( "x /R y; p /R q", "xxxD", ".rdf" ); }
    
    public void testE()
        { testReadback( "x /R y; p /R q", "xxxE", ".n3" ); }
    
    public void testF()
        { testReadback( "x /R y; p /R q", "xxxF", ".nt" ); }
    
    public void testG()
        { testReadback( "http://domain/S ftp:ftp/P O", "xxxG", ".rdf" ); }
    
    public void testH()
        { testReadback( "http://domain/S ftp:ftp/P O", "xxxH", ".nt" ); }
    
    public void testI()
        { testReadback( "http://domain/S ftp:ftp/P O", "xxxI", ".n3" ); }
    
    /**
        Test that the graph encoded as the test-string content can be
        written out to a temporary file generated from the prefix and suffix,
        and then read back correctly. The temporary files are marked as
        delete-on-exit to try and avoid cluttering the user's filespace ...
               
    	@param content a graph encoded in GraphTestBase format
    	@param prefix the prefix for File.createTempFile
    	@param suffix the suffix for File.createTempFile
     */
    public void testReadback( String content, String prefix, String suffix )
        {
        File foo = tempFileName( prefix, suffix );
        Graph original = graphWith( content );
        Graph g = new FileGraph( foo, true );
        g.getBulkUpdateHandler().add( original );
        g.close();
        Graph g2 = new FileGraph( foo, false );
        assertEquals( "", original, g2 );
        g2.close();
        }
        

    }

/*
    (c) Copyright Hewlett-Packard Company 2003
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