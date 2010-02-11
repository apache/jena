/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.core.VarAlloc;


/** Map from _:* form to bNodes
 * 
 * @author Andy Seaborne
 */

public class LabelToNodeMap
{
    Map<String, Node> bNodeLabels = new HashMap<String, Node>() ;
    
    // Variables or bNodes?
    // True means variables (query pattern)
    // False means blank node (construct template)
    boolean generateVars = false ;
    VarAlloc allocator = null ;
    
    /** Create blank nodes, with the same blank node returned for thre same label.  
     * 
     * @return LabelToNodeMap
     */

    public static LabelToNodeMap createBNodeMap()
    { return new LabelToNodeMap(false, null) ; }
    
    /** Create variables (Var), starting from zero each time
     * This means that parsing a query string will generate
     * the same variable names for bNode variables each time,
     * making Query.equals and Query.hashCode work.  
     * 
     * @return LabelToNodeMap
     */
    
    public static LabelToNodeMap createVarMap()
    { return new LabelToNodeMap(true, new VarAlloc(ARQConstants.allocParserAnonVars) ) ; }
    
    private LabelToNodeMap(boolean genVars, VarAlloc allocator) 
    {
        generateVars = genVars ;
        this.allocator = allocator ;
    }
    
    public Set<String> getLabels()  { return bNodeLabels.keySet() ; }
    
    public Node asNode(String label)
    {
        Node n = bNodeLabels.get(label) ;
        if ( n != null )
            return n ;
        n = allocNode() ;
        bNodeLabels.put(label, n) ;
        return n ;
    }
    
    public Node allocNode()
    {
        if ( generateVars )
            return allocAnonVariable() ;
        return Node.createAnon() ;
    }
    
    private Node allocAnonVariable()
    {
        return allocator.allocVar() ;
    }
    
    public void clear()
    {
        bNodeLabels.clear() ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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