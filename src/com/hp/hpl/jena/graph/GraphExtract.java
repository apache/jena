/*
      (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
      [See end of file]
      $Id: GraphExtract.java,v 1.4 2004-11-19 14:38:10 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph;

import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.util.CollectionFactory;

/**
     GraphExtract offers a very simple recursive extraction of a subgraph with a
     specified root in some supergraph. The recursion is terminated by triples
     that satisfy some supplied boundary condition.
     
 	@author hedgehog
*/
public class GraphExtract
    {
    protected final TripleBoundary b;
    
    public GraphExtract( TripleBoundary b )
        { this.b = b; }
    
    /**
         Answer a new graph which is the reachable subgraph from <code>node</code>
         in <code>graph</code> with the terminating condition given by the
         TripleBoundary passed to the constructor.
    */
    public Graph extract( Node node, Graph graph )
        { return extractInto( new GraphMem(), node, graph ); }
    
    /**
         Answer the graph <code>toUpdate</code> augmented with the sub-graph of
         <code>extractFrom</code> reachable from <code>root</code> bounded
         by this instance's TripleBoundary.
    */
    public Graph extractInto( Graph toUpdate, Node root, Graph extractFrom )
        { new Extraction( b, toUpdate, extractFrom ).extractInto( root );
        return toUpdate; }
    
    /**
         This is the class that does all the work, in the established context of the
         source and destination graphs, the TripleBoundary that determines the
         limits of the extraction, and a local set <code>active</code> of nodes 
         already seen and hence not to be re-processed.
        @author kers
     */
    protected static class Extraction
        {
        protected Graph toUpdate;
        protected Graph extractFrom;
        protected Set active;
        protected TripleBoundary b;
        
        Extraction( TripleBoundary b, Graph toUpdate, Graph extractFrom )
            {
            this.toUpdate = toUpdate;
            this.extractFrom = extractFrom;
            this.active = CollectionFactory.createHashedSet();
            this.b = b;
            }
        
        public void extractInto( Node root  )
            {
            active.add( root );
            Iterator it = extractFrom.find( root, Node.ANY, Node.ANY );
            while (it.hasNext())
                {
                Triple t = (Triple) it.next();
                Node subRoot = t.getObject();
                toUpdate.add( t );
                if (! (active.contains( subRoot ) || b.stopAt( t ))) extractInto( subRoot );
                }
            }
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