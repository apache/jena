/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.out;

import java.util.HashMap ;
import java.util.Map ;

import org.openjena.riot.system.MapWithScope ;
import org.openjena.riot.system.SyntaxLabels ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Node_Literal ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

/** Map nodes to string (usually, blank nodes to labels).
 * See {@link SyntaxLabels#createNodeToLabel} for getting a default setup.
 */

public class NodeToLabel extends MapWithScope<Node, String, Node>
{
    /** Allocation from a single scope; just the label matters. */
    static public NodeToLabel createScopeByDocument()
    { return new NodeToLabel(new SingleScopePolicy(), new AllocatorIncLabel()) ; }

//    /** Allocation scoped by graph and label. */
//    public static NodeToLabel createScopeByGraph() 
//    { return new NodeToLabel(new GraphScopePolicy(), new AllocatorIncLabel()) ; }

    /** Allocation as per internal label, with an encoded safe label. */
    public static NodeToLabel createBNodeByLabelEncoded() 
    { return new NodeToLabel(new SingleScopePolicy(), new AllocatorInternalSafe()) ; }

    /** Allocation as per internal label */
    public static NodeToLabel createBNodeByLabelAsGiven() 
    { return new NodeToLabel(new SingleScopePolicy(), new AllocatorInternalRaw()) ; }

    /** Allocation as per internal label */
    public static NodeToLabel createBNodeByIRI() 
    { return new NodeToLabel(new SingleScopePolicy(), new AllocatorBNodeAsIRI()) ; }

    private static final NodeToLabel _internal = createBNodeByLabelEncoded() ;
    public static NodeToLabel labelByInternal() { return _internal ; }  
    

    private NodeToLabel(ScopePolicy<Node, String, Node> scopePolicy, Allocator<Node, String> allocator)
    {
        super(scopePolicy, allocator) ;
    }
    // ======== Scope Policies
    
    /** Single scope */
    private static class SingleScopePolicy implements ScopePolicy<Node, String, Node>
    { 
        private Map<Node, String> map = new HashMap<Node, String>() ;
        public Map<Node, String> getScope(Node scope) { return map ; }
        public void clear() { map.clear(); }
    }
    
    /** One scope for labels per graph */
    private static class GraphScopePolicy implements ScopePolicy<Node, String, Node>
    { 
        private Map<Node, String> dftMap = new HashMap<Node, String>() ;
        private Map<Node, Map<Node, String>> map = new HashMap<Node, Map<Node, String>>() ;
        public Map<Node, String> getScope(Node scope)
        {
            if ( scope == null )
                return dftMap ;
            
            Map<Node, String> x = map.get(scope) ;
            if ( x == null )
            {
                x = new HashMap<Node, String>() ;
                map.put(scope, x) ;
            }
            return x ;
        }
        public void clear() { map.clear(); }
    }
    
    // ======== Allocators 

    /** Allocator and some default policies. */
    private abstract static class AllocatorBase implements Allocator<Node, String>
    {
        // abstract to make you think about the policy!
        private long counter = 0 ;
        
        public final String create(Node node)
        {
            if ( node.isURI() )         return labelForURI(node) ;
            if ( node.isLiteral() )     return labelForLiteral(node) ;
            if ( node.isBlank() )       return labelForBlank(node) ;
            if ( node.isVariable() )    return labelForVar(node) ;
            
            // Other??
            return Long.toString(counter++) ;
        }

        protected String labelForURI(Node node)
        {
            return "<"+node.getURI()+">" ;
        }

        protected abstract String labelForBlank(Node node) ;

        protected String labelForLiteral(Node node)
        {
            // TODO Better literal output.
            return FmtUtils.stringForLiteral((Node_Literal)node, null) ;
        }

        protected String labelForVar(Node node)
        {
            return "?"+node.getName() ;
        }
        public void reset()     {}
    }
    
    private static class AllocatorInternalRaw extends AllocatorBase
    {
        @Override
        protected String labelForBlank(Node node)
        {
            return "_:"+node.getBlankNodeLabel() ;
        }
    }
    
    private static class AllocatorInternalSafe extends AllocatorBase
    {
        @Override
        protected String labelForBlank(Node node)
        {
            // NodeFmtLib.safeBNodeLabel adds a "B"
            return "_:"+NodeFmtLib.encodeBNodeLabel(node.getBlankNodeLabel()) ;
        }
    }
    
    private static class AllocatorIncLabel extends AllocatorBase
    {
        private int X = 0 ;
        
        AllocatorIncLabel() {}

        @Override
        protected String labelForBlank(Node node)
        {
            return "_:b"+Integer.toString(X++) ;
        }
    } ;
    
    private static class AllocatorBNodeAsIRI extends AllocatorBase
    {
        @Override
        protected String labelForBlank(Node node)
        {
            // Needs to be safe?
            //String str = NodeFmtLib.safeBNodeLabel(node.getBlankNodeLabel()) ;
            String str = node.getBlankNodeLabel() ;
            return "<_:"+str+">" ;
        }
    } ;

}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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