/*
      (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
      [See end of file]
      $Id: TestGraphExtract.java,v 1.2 2004-08-07 12:02:33 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

import java.util.Iterator;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

import junit.framework.TestSuite;

/**
 @author hedgehog
 */
public class TestGraphExtract extends ModelTestBase
	{
	public TestGraphExtract(String name)
		{ super( name ); }
	
	public static TestSuite suite()
		{ return new TestSuite( TestGraphExtract.class ); }

    public void testExtractNothing()
        {
        testExtract( "", "x", "" );
        testExtract( "", "x", "a R b" );
        testExtract( "", "x", "a R x" );
        testExtract( "","x", "a x y" );
        }
    
    public void testExtractOneLevel()
        {
        testExtract( "a R b", "a", "a R b" );
        testExtract("a R b; a R c", "a", "a R b; a R c" );
        testExtract( "a R b; a S d", "a", "a R b; a S d" );
        }
    
    public void testNoJunk()
        {
        testExtract( "a R b", "a", "a R b; x R y" );
        }

    public void testExtractTwoLevels()
        {
        testExtract( "a R b; b S c", "a", "a R b; b S c" );
        testExtract( "a R b; b S c", "a", "a R b; b S c; x P y" );
        testExtract( "a R b; b S c; b T d", "a", "a R b; b S c; b T d" );
        testExtract( "a R b; b S c; a T d", "a", "a R b; a T d; b S c" );
        }
    
    public void testExtractSeveralLevels()
        {
        testExtract( "a R b; b S c; c T d; d U e", "a", "a R b; b S c; c T d; d U e" );
        }
    
    public void testExtractNoLoop()
        {
        testExtract( "a R a", "a", "a R a" );
        testExtract( "a R b; b R a", "a", "a R b; b R a; z P a" );
        testExtract( "a R b; b S c; c T a", "a", "a R b; b S c; c T a; junk P junk" );
        }
    
    public void testTripleFilter()
        {
        assertTrue( stopAtAnonObject.stopAt( triple( "a R _b" ) ) );
        assertFalse( stopAtAnonObject.stopAt( triple( "a R b" ) ) );
        assertFalse( stopAtAnonObject.stopAt( triple( "a _R b" ) ) );
        assertFalse( stopAtAnonObject.stopAt( triple( "_a R b" ) ) );
        }
    
    protected static final TripleBoundary stopAtAnonObject = new TripleBoundary()
        {
        public boolean stopAt( Triple t ) { return t.getObject().isBlank(); }
        };
        
    protected static final TripleBoundary stopNowhere = new TripleBoundary()
        {
        public boolean stopAt( Triple t ) { return false; }
        };
    
    interface TripleBoundary
        {
        boolean stopAt( Triple t );
        }   
    
    public void testExtractBoundary()
        {
        testExtract( "a R b; b S _c", "a", "a R b; b S _c; _c T d", stopAtAnonObject );
        }

    public void testExtract( String wanted, String node, String source )
        {
        testExtract( wanted, node, source, stopNowhere );
        }
        
    /**
    */
    private void testExtract( String wanted, String node, String source, TripleBoundary b )
        {
        assertIsomorphic( graphWith( wanted ), extract( node( node ), b, graphWith( source ) ) );
        }

    /**
    	@param node
    	@param graph
    	@return
    */
    private Graph extract( Node node, TripleBoundary b, Graph graph )
        { return extractInto( graphWith( "" ), node, b, graph ); }
    
    private Graph extractInto( Graph toUpdate, Node root, TripleBoundary b, Graph extractFrom )
        {
        Iterator it = extractFrom.find( root, Node.ANY, Node.ANY );
        while (it.hasNext())
            {
            Triple t = (Triple) it.next();
            Node subRoot = t.getObject();
            toUpdate.add( t );
            if (toUpdate.contains( subRoot, Node.ANY, Node.ANY ) == false && b.stopAt( t ) == false)
                extractInto( toUpdate, subRoot, b, extractFrom );
            }
        return toUpdate;
        }
    }

/*
    (c) Copyright 2004, Hewlett-Packard Development Company, LP
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