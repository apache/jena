/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.core.Constraint;

/** Block processor that finds nodes in blocks 
 * 
 * @author Andy Seaborne
 * @version $Id: BlockNodes.java,v 1.3 2006/03/15 17:45:40 andy_seaborne Exp $
 */

public class BlockNodes implements BlockProc
{
    public static List<Node> allConstants(Block block)
    {
        BlockNodes proc = new BlockNodes(false, true) ;
        block.apply(proc, true) ;
        return proc.getNodes() ;
    }
    
    public static List<Node> allVars(Block block)
    {
        BlockNodes proc = new BlockNodes(true, false) ;
        block.apply(proc, true) ;
        return proc.getNodes() ;
    }
    
    
    // A Set would be slightly better.
    // but predictable lists are easier to debug :-)
    List<Node> nodes = new ArrayList<Node>() ;
    boolean includeVars = true ;
    boolean includeConsts = true ;
    
    BlockNodes(boolean includeVars, boolean includeConsts)
    { this.includeVars = includeVars ; this.includeConsts = includeConsts ; }
    
    List<Node> getNodes() { return nodes ; }
    
    public void basicPattern(BasicPattern basicPattern)
    {
        for ( Triple t : basicPattern )
        {
            node(t.getSubject()) ;
            node(t.getPredicate()) ;
            node(t.getObject()) ;
        }
    }
    
    public void restriction(Constraint c)
    { 
        System.err.println("BlockNodes: Condition checking not done") ;
    }
    
    public void optional(Block optBlock)
    {}
    
    private void node(Node node)
    {
        if ( includeVars && node.isVariable() && ! nodes.contains(node) ) 
            nodes.add(node) ;
        
        if ( includeConsts && ! node.isVariable() && ! nodes.contains(node) )
            nodes.add(node) ;
    }

}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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