/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.Quad;

/** Get vars for a pattern  */ 

public class OpVars
{
    public static Set patternVars(Op op)
    {
        Set acc = new HashSet() ;
        patternVars(op, acc) ;
        return acc ; 
    }
    
    public static void patternVars(Op op, Set acc)
    {
        OpWalker.walk(op, new OpVarsPattern(acc)) ;
    }
    
    public static Set allVars(Op op)
    {
        Set acc = new HashSet() ;
        allVars(op, acc) ;
        return acc ;
    }

    public static void allVars(Op op, Set acc)
    {
        OpWalker.walk(op, new OpVarsQuery(acc)) ;
    }

    private static class OpVarsPattern extends OpVisitorBase
    {
        // The possibly-set-vars
        protected Set acc ;

        OpVarsPattern(Set acc) { this.acc = acc ; }
        

        public void visit(OpBGP opBGP)
        {
            for ( Iterator iter = opBGP.getPattern().iterator() ; iter.hasNext() ; )
            {
                    Triple t = (Triple)iter.next() ;
                    addVarsFromTriple(acc, t) ;
            }
        }

        public void visit(OpQuadPattern quadPattern)
        {
            for ( Iterator iter = quadPattern.getQuads().iterator() ; iter.hasNext() ; )
            {
                Quad quad = (Quad)iter.next() ;
                addVarsFromQuad(acc, quad) ;
            }
        }

        public void visit(OpGraph opGraph)
        {
            addVar(acc, opGraph.getNode()) ;
        }

        public void visit(OpDatasetNames dsNames)
        {
            addVar(acc, dsNames.getGraphNode()) ;
        }
    }
    
    private static class OpVarsQuery extends OpVarsPattern
    {
        OpVarsQuery(Set acc) { super(acc) ; }

        public void visit(OpFilter opFilter)
        {
            opFilter.getExprs().varsMentioned(acc);
        }

        public void visit(OpOrder opOrder)
        {
            for ( Iterator iter = opOrder.getConditions().iterator() ; iter.hasNext(); )
            {
                SortCondition sc = (SortCondition)iter.next();
                Set x = sc.getExpression().getVarsMentioned() ;
                acc.addAll(x) ;
            }
        }

        public void visit(OpProject opProject)
        {
            acc.addAll(opProject.getVars()) ;
        }


    }

    private static void addVarsFromTriple(Set acc, Triple t)
    {
        addVar(acc, t.getSubject()) ;
        addVar(acc, t.getPredicate()) ;
        addVar(acc, t.getObject()) ;
    }
    
    private static void addVarsFromQuad(Set acc, Quad q)
    {
        addVar(acc, q.getSubject()) ;
        addVar(acc, q.getPredicate()) ;
        addVar(acc, q.getObject()) ;
        addVar(acc, q.getGraph()) ;
    }
    
    private static void addVar(Set acc, Node n)
    {
        if ( n == null )
            return ;
        
        if ( n.isVariable() )
            acc.add(n) ;
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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