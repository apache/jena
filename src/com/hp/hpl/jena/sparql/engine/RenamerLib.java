/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine;

import java.util.ArrayList ;
import java.util.List ;

import static org.openjena.atlas.lib.Lib.equal ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.QuadPattern ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;

public class RenamerLib
{
    /** Do a node->node conversion of a BGP - return original BGP for "no change" */
    public static BasicPattern rename(Renamer renamer, BasicPattern pattern)  
    {
        BasicPattern bgp2 = new BasicPattern() ;
        boolean changed = false ;
        for ( Triple triple : pattern )
        {
            Triple t2 = rename(renamer, triple) ;
            bgp2.add(t2) ;
            if ( t2 != triple )
                changed = true ;
        }
        if ( ! changed )
            return pattern ;
        return bgp2 ;
    }

    /** Do a node->node conversion of a QuadPattern - return original QuadPattern for "no change" */
    public static QuadPattern rename(Renamer renamer, QuadPattern pattern)  
    {
        QuadPattern qp2 = new QuadPattern() ;
        boolean changed = false ;
        for ( Quad quad : pattern )
        {
            Quad q2 = rename(renamer, quad) ;
            qp2.add(q2) ;
            if ( q2 != quad )
                changed = true ;
        }
        if ( ! changed )
            return pattern ;
        return qp2 ;
    }

    /** Do a node->node conversion of a Triple - return original Triple for "no change" */
    public static Triple rename(Renamer renamer, Triple triple)  
    {
        boolean change = false ;
        Node s = triple.getSubject() ;
        Node p = triple.getPredicate() ;
        Node o = triple.getObject() ;
        
        Node s1 = renamer.rename(s) ;
        if ( s1 != s ) { change = true ; s = s1 ; }
        Node p1 = renamer.rename(p) ;
        if ( p1 != p ) { change = true ; p = p1 ; }
        Node o1 = renamer.rename(o) ;
        if ( o1 != o ) { change = true ; o = o1 ; }
    
        if ( ! change )
            return triple ;
        return new Triple(s,p,o) ;
    }

    /** Do a node->node conversion of a Quad - return original Quad for "no change" */
    public static Quad rename(Renamer renamer, Quad quad)  
    {
        boolean change = false ;
        Node s = quad.getSubject() ;
        Node p = quad.getPredicate() ;
        Node o = quad.getObject() ;
        Node g = quad.getGraph() ;
        
        Node g1 = renamer.rename(g) ;
        if ( g1 != g ) { change = true ; g = g1 ; }
        Node s1 = renamer.rename(s) ;
        if ( s1 != s ) { change = true ; s = s1 ; }
        Node p1 = renamer.rename(p) ;
        if ( p1 != p ) { change = true ; p = p1 ; }
        Node o1 = renamer.rename(o) ;
        if ( o1 != o ) { change = true ; o = o1 ; }
    
        if ( ! change )
            return quad ;
        return new Quad(g,s,p,o) ;
    }

    /** Do a node->node conversion of a List&lt;Quad&gt; - return original List&lt;Quad&gt; for "no change" */
    public static List<Quad> renameQuads(Renamer renamer, List<Quad> quads)
    {
        List<Quad> x = new ArrayList<Quad>() ;
        boolean changed = false ; 
        for ( Quad q : quads )
        {
            Quad q2 = RenamerLib.rename(renamer, q) ;
            if ( q != q2 )
                changed = true ;
            x.add(q2) ;
        }
        if ( ! changed )
            return quads ;
        return x ;
    }

    /** Do a node->node conversion of a VarExprList - return original VarExprList for "no change" */
    public static VarExprList rename(Renamer renamer, VarExprList varExprList)
    {
        VarExprList varExprList2 = new VarExprList() ;
        boolean changed = false ;
        for ( Var v : varExprList.getVars() )
        {
            Expr expr = varExprList.getExpr(v) ;
            Var v2 = (Var)renamer.rename(v) ;
            Expr expr2 = ( expr != null ) ? rename(renamer, expr) : null ;
            
            if ( ! equal(v, v2) || ! equal(expr, expr2) )
                changed = true ;
            varExprList2.add(v2, expr2) ;
        }
        if ( ! changed )
            return varExprList ; 
        return varExprList2 ;
    }

    public static List<Var> renameVars(Renamer renamer, List<Var> varList)
    {
        List<Var> varList2 = new ArrayList<Var>(varList.size()) ; 
        boolean changed = false ;
        for ( Var v : varList )
        {
            Var v2 = (Var)renamer.rename(v) ;
            varList2.add(v2) ;
            if ( !equal(v, v2) )
                changed = true ;
        }
        if ( ! changed )
            return varList ; 
        return varList2 ;
    }

    public static ExprList rename(Renamer renamer, ExprList exprList)
    {
          ExprList exprList2 = new ExprList() ;
          boolean changed = false ;
          for(Expr expr : exprList)
          {
              Expr expr2 = rename(renamer, expr) ;
              if ( expr != expr2 )
                  changed = true ;
              exprList2.add(expr2) ;
          }
          if ( ! changed ) return exprList ;
          return exprList2 ;
    }

    public static Expr rename(Renamer renamer, Expr expr)
    {
        return expr.copyNodeTransform(renamer) ;
    }

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