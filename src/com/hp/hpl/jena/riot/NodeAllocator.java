/*
 * (c) Copyright 2010 Talis Information Ltd
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot;
import static java.lang.String.format; 
import java.util.HashMap ;
import java.util.Map ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.rdf.model.AnonId ;

// Replaces LabelToNodeMap
// Generalises it for different policies : per file, per scope. 
public abstract class NodeAllocator
{
    /** Allocation from a single scope; just the label matters. */
    public static NodeAllocator createOneScope()
    { return new NodeAllocator.OneScope(); }

    /** Allocation scoped by graph and label. */
    public static NodeAllocator createScopeByGraph()
    { return new NodeAllocator.ScopeByGraph(); }

    /** Allocation using syntax label. */
    public static NodeAllocator createUseLabelAsGiven()
    { return new NodeAllocator.LabelAsGiven(); }

    /** Allocation, globallay scoped, that uses a incrementing field to create new nodes */  
    public static NodeAllocator createIncremental()
    { return new NodeAllocator.Deterministic(); }
    
    /** Create a node that is guaranteed to be fresh */ 
    public abstract Node create() ;
    
    /** Return a node that correspons to the scope and label.
     *  Create a node if needed */
    
    public abstract Node get(Graph scope, String label) ;
    public abstract void clear() ;

    private static Node _create() { return Node.createAnon() ; }
    
    private static Allocator<Node> maker = new Allocator<Node>()
    {
        public Node create()
        { return Node.createAnon() ; }
    } ;
    
    static private abstract class NodeAlloc extends NodeAllocator
    {
        protected Allocator<Node> alloc ;
        protected NodeAlloc(Allocator<Node> alloc)
        {
            this.alloc = alloc ;
        }
        
        @Override
        final
        public Node create() { return alloc.create() ; }
    }
    
    static private class OneScope extends NodeAlloc
    {
        protected OneScope() { this(maker) ; }
        public OneScope(Allocator<Node> alloc) { super(alloc)  ; }

        private Map<String, Node> map = new HashMap<String, Node>() ;
        
        @Override
        public void clear()
        {
            map.clear();
        }

        @Override
        public Node get(Graph scope, String label)
        {
            Node n = map.get(label) ;
            if ( n == null )
            {
                n = _create() ;
                map.put(label, n) ;
            }
            return n ;
        }
    }

    static private class LabelAsGiven extends NodeAlloc
    {
        protected LabelAsGiven() { super(maker) ; }

        @Override
        public void clear()
        { }

        @Override
        public Node get(Graph scope, String label)
        {
            return Node.createAnon(new AnonId(label)) ;
        }
    }
    
    static private class ScopeByGraph extends NodeAlloc
    {
        protected ScopeByGraph() { super(maker) ; }

        private Map<Graph, Map<String, Node>> map = new HashMap<Graph, Map<String, Node>>() ;
        
        @Override
        public void clear()
        {
            map.clear();
        }

        @Override
        public Node get(Graph scope, String label)
        {
            Map<String, Node> x = map.get(label) ;
            if ( x == null )
            {
                x =  new HashMap<String, Node>() ;
                map.put(scope, x) ;
            }
            Node n = x.get(label) ;
            if ( n == null )
            {
                n = _create() ;
                x.put(label, n) ;
            }
            return n ;
        }
    }
    
    static private class Deterministic extends OneScope
    {
        protected Deterministic()
        {
            super(new Allocator<Node>() {
                private long counter = 0 ;

                public Node create()
                {
                    String $ = format("B%0x04X", ++counter) ;
                    return Node.createAnon(new AnonId($)) ;
                }
            }) ;
        }
        
        @Override
        public void clear()
        {}
    }
}

/*
 * (c) Copyright 2010 Talis Information Ltd
 * All rights reserved.
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
 *
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