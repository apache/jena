/*
	(c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
	[See end of file]
	$Id: TestSmallGraphMem.java,v 1.1 2004-07-09 07:59:53 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem.test;

import java.util.Set;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.graph.test.AbstractTestGraph;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.mem.GraphMemBulkUpdateHandler;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.HashUtils;
import com.hp.hpl.jena.util.iterator.*;


/**
 @author hedgehog
*/
public class TestSmallGraphMem extends AbstractTestGraph
    {
    public TestSmallGraphMem( String name ) 
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestSmallGraphMem.class ); }
        
    public Graph getGraph()
        { return new SmallGraphMem(); }
    
    public static class SmallGraphMem extends GraphMem
    	{
        protected Set triples = HashUtils.createSet();
        
        public SmallGraphMem()
            { this( ReificationStyle.Minimal ); }
        
        public SmallGraphMem( ReificationStyle style )
            { super( style ); }
        
        public void performAdd( Triple t )
            {
            if (getReifier().handledAdd( t )) return;
            else triples.add( t );
            }

        public void performDelete( Triple t )
            {
            if (getReifier().handledRemove( t )) return;
            else triples.remove( t );
            }

        public int size()  
            {
            checkOpen();
            return triples.size();
            }

        public boolean isEmpty()
            {
            checkOpen();
            return triples.isEmpty();
            }
        
        public BulkUpdateHandler getBulkUpdateHandler()
            {
            if (bud == null) bud = new GraphMemBulkUpdateHandler( this )
            	{
                protected void clearComponents()
            	    {
            	    SmallGraphMem g = (SmallGraphMem) graph;
            	    g.triples.clear();
            	    }
            	};
            return bud;
            }
        
	        public ExtendedIterator find( TripleMatch m ) 
	            {
	            checkOpen();
	            return WrappedIterator.create( triples.iterator() ) .filterKeep ( new TripleMatchFilter( m.asTriple() ) );
	    	}
    	}
    }

/*
	 *  (c) Copyright 2004 Hewlett-Packard Development Company, LP
	 *  All rights reserved.
	 *
	 * Redistribution and use in source and binary forms, with or without
	 * modification, are permitted provided that the following conditions
	 * are met:
	 * 1. Redistributions of source code must retain the above copyright
	 *    notice, this list of conditions and the following disclaimer.
	 * 2. Redistributions in binary form must reproduce the above copyright
	 *    notice, this list of conditions and the following disclaimer in the
	 *    documentation and/or other materials provided with the distribution.
	 * 3. The name of the author may not be used to endorse or promote products
	 *    derived from this software without specific prior written permission.
	
	 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
	 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
	 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
	 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
	 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
	 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
	 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
	 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
	 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */