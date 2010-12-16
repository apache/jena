/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.syntax;

import java.util.Iterator ;
import java.util.Set ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.TriplePath ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.util.VarUtils ;

public class PatternVarsVisitor extends ElementVisitorBase
{
    protected Set<Var> acc ;
    public PatternVarsVisitor(Set<Var> s) { acc = s ; } 

    @Override
    public void visit(ElementTriplesBlock el)
    {
        for (Iterator<Triple> iter = el.patternElts() ; iter.hasNext() ; )
        {
            Triple t = iter.next() ;
            VarUtils.addVarsFromTriple(acc, t) ;
        }
    }

    @Override
    public void visit(ElementPathBlock el) 
    {
        for (Iterator<TriplePath> iter = el.patternElts() ; iter.hasNext() ; )
        {
            TriplePath tp = iter.next() ;
            // If it's triple-izable, then use the triple. 
            if ( tp.isTriple() )
                VarUtils.addVarsFromTriple(acc, tp.asTriple()) ;
            else
                VarUtils.addVarsFromTriplePath(acc, tp) ;
        }
    }
        
    // Variables here are non-binding.
    //@Override public void visit(ElementExists el)       { }
    //@Override public void visit(ElementNotExists el)    { }
    //@Override public void visit(ElementMinus el)        { }
    //@Override public void visit(ElementFilter el)       { }

    @Override
    public void visit(ElementNamedGraph el)
    {
        VarUtils.addVar(acc, el.getGraphNameNode()) ;
    }

    @Override
    public void visit(ElementSubQuery el)
    {
        el.getQuery().setResultVars() ;
        VarExprList x = el.getQuery().getProject() ;
        acc.addAll(x.getVars()) ;
        
        // And any GROUP BY 
        x = el.getQuery().getGroupBy() ;
        acc.addAll(x.getVars()) ;
        
    }

    @Override
    public void visit(ElementAssign el)
    {
        acc.add(el.getVar()) ;
    }

    @Override
    public void visit(ElementBind el)
    {
        acc.add(el.getVar()) ;
    }
        
//    @Override
//    public void visit(ElementService el)
//    {
//        // Although if this isn't defined elsewhere the query won't work.
//        VarUtils.addVar(acc, el.getServiceNode()) ;
//    }
    
}
/*
 * (c) Copyright 2010 Epimorphics Ltd.
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