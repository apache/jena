/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.pattern;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.compiler.QuadBlock;
import com.hp.hpl.jena.sdb.compiler.QuadBlockCompiler;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.util.alg.Filter;
import com.hp.hpl.jena.sparql.core.Quad;

/** A (description of a) table that holds a cached/optimized version of a pattern.
 *  P for pattern
 *  */ 

public class PTable
{
    Map <Node, String> cols = new HashMap<Node, String>() ;
    
    public PTable() {}
    
    public void add(Node property, String colname)
    {
        cols.put(property, colname) ;
    }
    
    // Accept a node if it is in the 
    static Filter<Node> f = new Filter<Node>()
    {
        public boolean accept(Node node)
        {
            return false ;
        }
        
    } ;
    
    public boolean match(QuadBlock quadBlock)
    {
        //QuadBlockMatch.match(pattern, quadBlock) ;
        
        Set<Node> predicates = new HashSet<Node>(cols.keySet()) ;
        
        //SetUtils.filter(cols.keySet(), f) ;
        
        
        for ( Node p : predicates )
        {
            if ( ! quadBlock.contains(null, null, p, null) )
                return false ;
        }
        return true ;
    }
//        for ( Quad q : quadBlock )
//        {
//            Node p = q.getPredicate() ;
//            predicates.remove(p) ;
////            if ( predicates.contains(p) )
////                predicates.remove(p) ;
//        }
    
    // Returns a stage list of a reduced quad block and this step.
    // Issue: placement of this step.  An SqlNode optimization problem?
    
    public SqlStageList modBlock(QuadBlockCompiler compiler, QuadBlock quadBlock)
    {
        Set<Node> predicates = new HashSet<Node>(cols.keySet()) ;
        SqlStageList sList = new SqlStageList() ;

        QuadBlock replacement = quadBlock.clone() ;

        for ( Node p : predicates )
        {
            int idx = quadBlock.findFirst(null, null, p, null) ;
            if ( idx < 0 )
                // No match.
                return null ;
            Quad q = quadBlock.get(idx) ;
            replacement.remove(q) ; // Not index as it might have moved.
        }
        
        SqlStagePTable stage = new SqlStagePTable() ;
        sList.add(stage) ;
        sList.add(new SqlStagePlain(compiler, replacement)) ;
        return sList ;
    }
    
    class SqlStagePTable implements SqlStage
    {
        public SqlNode build(SDBRequest request)
        {
            return null ;
        }

    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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