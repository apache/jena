/*
 * (c) Copyright 2010 Talis Systems Ltd
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.lang;
import static java.lang.String.format ;

import java.util.HashMap ;
import java.util.Map ;

import org.openjena.riot.SysRIOT ;
import org.openjena.riot.system.MapWithScope ;
import org.openjena.riot.system.SyntaxLabels ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.rdf.model.AnonId ;

/** Allocation Nodes (Bnodes usually) based on the graph and label 
 * Various different policies.
 * See {@link SyntaxLabels#createLabelToNode} for getting a default setup.
 */  

public class LabelToNode extends MapWithScope<String, Node, Node>
{
    /** Allocation from a single scope; just the label matters. */
    public static LabelToNode createScopeByDocument()
    { return new LabelToNode(new SingleScopePolicy(), nodeMaker) ; }

    /** Allocation scoped by graph and label. */
    public static LabelToNode createScopeByGraph()
    { return new LabelToNode(new GraphScopePolicy(), nodeMaker) ; }

    /** Allocation using syntax label. */
    public static LabelToNode createUseLabelAsGiven()
    { return new LabelToNode(new SingleScopePolicy(), nodeMakerByLabel) ; }

    /** Allocation, globallay scoped, that uses a incrementing field to create new nodes */  
    public static LabelToNode createIncremental()
    { return new LabelToNode(new SingleScopePolicy(), nodeMakerDeterministic) ; } 
    
    private LabelToNode(ScopePolicy<String, Node, Node> scopePolicy, Allocator<String, Node> allocator)
    {
        super(scopePolicy, allocator) ;
    }

    // ======== Scope Policies
    
    /** Single scope */
    private static class SingleScopePolicy implements ScopePolicy<String, Node, Node>
    { 
        private Map<String, Node> map = new HashMap<String, Node>() ;
        public Map<String, Node> getScope(Node scope) { return map ; }
        public void clear() { map.clear(); }
    }
    
    /** One scope for labels per graph */
    private static class GraphScopePolicy  implements ScopePolicy<String, Node, Node>
    { 
        private Map<String, Node> dftMap = new HashMap<String, Node>() ;
        private Map<Node, Map<String, Node>> map = new HashMap<Node, Map<String, Node>>() ;
        public Map<String, Node> getScope(Node scope)
        {
            if ( scope == null )
                return dftMap ;
            
            Map<String, Node> x = map.get(scope) ;
            if ( x == null )
            {
                x = new HashMap<String, Node>() ;
                map.put(scope, x) ;
            }
            return x ;
        }
        public void clear() { map.clear(); }
    }

    // ======== Node Allocators 
    
    private static Allocator<String, Node> nodeMaker = new Allocator<String, Node>()
    {
        public Node create(String label)
        { return Node.createAnon() ; }

        public void reset()     {}
    } ;

    private static Allocator<String, Node> nodeMakerDeterministic = new Allocator<String, Node>()
    {
        private long counter = 0 ;

        public Node create(String label)
        {
            String $ = format("B0x%04X", ++counter) ;
            return Node.createAnon(new AnonId($)) ;
        }

        public void reset()     {}
    } ;
    
    private static Allocator<String, Node> nodeMakerByLabel = new Allocator<String, Node>()
    {
        private long counter = 0 ;
        
        public Node create(String label)
        {
            if ( label == null )
                label = SysRIOT.BNodeGenIdPrefix+counter++ ;
            return Node.createAnon(new AnonId(label)) ;
        }

        public void reset()     {}
    } ;
}

/*
 * (c) Copyright 2010 Talis Systems Ltd
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