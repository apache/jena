/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sse_dev.builders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sse_dev.builders.BuilderUtils;
import sse_dev.builders.ExprBuilder;
import sse_dev.builders.OpBuilder;
import sse_dev.Item;
import sse_dev.ItemList;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;


public class OpBuilder extends BuilderUtils
{
    public static Op build(Item item)
    {
        if (item.isNode() )
            broken(item, "Attempt to build op structure from a plain node") ;

        if (item.isWord() )
            broken(item, "Attempt to build op structure from a bare word") ;

        OpBuilder b = new OpBuilder();
        return b.build(item.getList()) ;
    }

    protected Map dispatch = new HashMap() ;
    
    public OpBuilder()
    {
        dispatch.put(symBGP, buildBGP) ;
        //dispatch.put(symQuadPattern, buildQuadPattern) ;
        dispatch.put(symFilter, buildFilter) ;
        dispatch.put(symGraph, buildGraph) ;
        dispatch.put(symJoin, buildJoin) ;
        dispatch.put(symLeftJoin, buildLeftJoin) ;
        dispatch.put(symUnion, buildUnion) ;

        dispatch.put(symToList, buildToList) ;
        dispatch.put(symOrderBy, buildOrderBy) ;
        dispatch.put(symProject, buildProject) ;
        dispatch.put(symDistinct, buildDistinct) ;
        dispatch.put(symReduced, buildReduced) ;
        dispatch.put(symSlice, buildSlice) ;
    }

    // The main recursive build operation.
    public Op build(ItemList list)
    {
        if ( list == null )
            list = null ;
    
        Item head = list.get(0) ;
        String tag = head.getWord() ;
    
        Build bob = (Build)dispatch.get(tag) ;
        if ( bob != null )
            return bob.make(list) ;
        else
            broken(head, "Unrecognized: "+tag) ;
        return null ;
    }

    protected Op build(ItemList list, int idx)
    {
        return build(list.get(idx).getList()) ;
    }

    static protected final String symBase         = "" ;
    static protected final String symBGP          = symBase + "BGP" ;
    static protected final String symQuadPattern  = symBase + "Quad" ;
    static protected final String symFilter       = symBase + "Filter" ;
    static protected final String symGraph        = symBase + "Graph" ;
    static protected final String symJoin         = symBase + "Join" ;
    static protected final String symLeftJoin     = symBase + "LeftJoin" ;
    static protected final String symUnion        = symBase + "Union" ;
    // Diff

    static protected final String symToList       = symBase + "ToList" ;
    static protected final String symOrderBy      = symBase + "OrderBy" ;
    static protected final String symProject      = symBase + "Project" ;
    static protected final String symDistinct     = symBase + "Distinct" ;
    static protected final String symReduced      = symBase + "Reduced" ;
    static protected final String symSlice        = symBase + "Slice" ;

    static public interface Build { Op make(ItemList list) ; }

    final protected Build buildBGP = new Build()
    {
        public Op make(ItemList list)
        {
            BasicPattern triples = new BasicPattern() ;
            for ( int i = 1 ; i < list.size() ; i++ )
            {
                Item item = list.get(i) ;
                if ( ! item.isList() )
                    broken(item, "Not a triple structure") ;
                Triple t = buildTriple(item.getList()) ;
                triples.add(t) ; 
            }
            return new OpBGP(triples) ;
        }
    } ;

    final protected Build buildQuadPattern = new Build()
    {
        public Op make(ItemList list)
        {
            broken(null, "Quad pattern not implemented") ;
            return null ;
//          Node g = null ;
//          QuadPattern quads = new QuadPattern() ;
//          for ( int i = 1 ; i < list.size() ; i++ )
//          {
//          Item item = list.get(i) ;
//          if ( ! item.isList() )
//          broken(item, "Not a quad structure") ;
//          Quad quad = buildQuad(item.getList()) ;
//          if ( g == null )
//          g = quad.getGraph() ;
//          else
//          {
//          if ( !g.equals(quad.getGraph()) )
//          broken(item, "Quad pattern is not using the same graph node everywhere") ;
//          }

//          quads.add(quad) ; 
//          }
//          return new OpQuadPattern(quads., g) ;
        }
    } ;

    final protected Build buildFilter = new Build()
    {
        public Op make(ItemList list)
        {
            checkLength(3, list, "Malformed filter") ;
            Item itemOp = list.get(1) ;
            Item itemExpr = list.get(2) ;

            Op op = build(itemOp.getList()) ;

            if ( ! itemExpr.isList() )
                broken(itemExpr, "List expected for expression") ;

            if ( itemExpr.getList().size() == 0 )
            {
                warning(itemExpr, "Empty List for expression") ;
                return OpFilter.filter(new ExprList(), op) ;
            }

            Expr expr = buildExpr(itemExpr) ;
            return OpFilter.filter(expr, op) ;
        }
    } ;

    final protected Build buildJoin = new Build()
    {
        public Op make(ItemList list)
        {
            checkLength(3, list, "Join") ;
            Op right = build(list, 1) ;
            Op left  = build(list, 2) ;
            Op op = OpJoin.create(left, right) ;
            return op ;
        }
    } ;

    final protected Build buildLeftJoin = new Build()
    {
        public Op make(ItemList list)
        {
            checkLength(3, list, "leftJoin") ;
            Op right = build(list, 1) ;
            Op left  = build(list, 2) ;
            Op op = OpLeftJoin.create(left, right, null) ;
            return op ;
        }
    } ;

    final protected Build buildUnion = new Build()
    {
        public Op make(ItemList list)
        {
            checkLength(3, list, "Union") ;
            Op right = build(list, 1) ;
            Op left  = build(list, 2) ;
            Op op = new OpUnion(left, right) ;
            return op ;
        }
    } ;

    final protected Build buildGraph = new Build()
    {
        public Op make(ItemList list)
        {
            checkLength(3, list, "Graph") ;
            Node graph = buildNode(list.get(1)) ;
            Op sub  = build(list, 2) ;
            return new OpGraph(graph, sub) ;
        }
    } ;



    final protected Build buildToList = new Build()
    {
        public Op make(ItemList list)
        {
            checkLength(2, list, "ToList") ;
            Op sub = build(list, 1) ;
            Op op = new OpList(sub) ;
            return op ;
        }
    } ;

    final protected Build buildOrderBy = new Build()
    {
        public Op make(ItemList list)
        {
            //checkList(list, 3, "OrderBy") ;
            Op sub = build(list, 1) ;
            List x = buildExpr(list, 2) ;
            Op op = new OpOrder(sub, x) ;
            return op ;
        }
    } ;

    final protected Build buildProject = new Build()
    {
        public Op make(ItemList list)
        {
            checkLength(3, list, "Project") ;
            Op sub = build(list, list.size()-1) ;
            List x = buildVars(list.get(1).getList(), 0) ;
            return new OpProject(sub, x) ;
        }
    } ;

    final protected Build buildDistinct = new Build()
    {
        public Op make(ItemList list)
        {
            checkLength(2, list, "Project") ;
            Op sub = build(list, 1) ;
            return new OpDistinct(sub) ;
        }
    } ;

    final protected Build buildReduced = new Build()
    {
        public Op make(ItemList list)
        {
            checkLength(2, list, "Reduced") ;
            Op sub = build(list, 1) ;
            return new OpReduced(sub) ;
        }
    } ;

    final protected Build buildSlice = new Build()
    {
        public Op make(ItemList list)
        {
            checkLength(4, list, "Slice") ;
            Op sub = build(list, 1) ;
            int start = buildInt(list, 2) ;
            int length = buildInt(list, 3) ;
            return new OpSlice(sub, start, length) ;
        }
    } ;

    protected static Expr buildExpr(Item item)
    {
        return ExprBuilder.build(item) ;
    }

    protected static Triple buildTriple(ItemList list)
    {
        checkLength(4, list, "triple") ;
        Node s = buildNode(list.get(1)) ;
        Node p = buildNode(list.get(2)) ;
        Node o = buildNode(list.get(3)) ;
        return new Triple(s, p, o) ; 
    }

    protected static Quad buildQuad(ItemList list)
    {
        checkLength(5, list, "quad") ;
        Node g = buildNode(list.get(1)) ;
        Node s = buildNode(list.get(2)) ;
        Node p = buildNode(list.get(3)) ;
        Node o = buildNode(list.get(4)) ;
        return new Quad(g, s, p, o) ; 
    }

    protected static List buildExpr(ItemList list, int start)
    {
        List x = new ArrayList() ;
        for ( int i = start ; i < list.size() ; i++ )
        {
            Item itemExpr = list.get(i) ;
            Expr expr = buildExpr(itemExpr) ;
            x.add(expr) ;
        }
        return x ;
    }

    protected static Node buildNode(Item item)
    {
        if ( !item.isNode() )
            broken(item, "Not a node: "+item) ;
        return item.getNode() ;
    }

    protected static List buildVars(ItemList list, int start)
    {
        List x = new ArrayList() ;
        for ( int i = start ; i < list.size() ; i++ )
        {
            Item item = list.get(i) ;
            if ( ! item.isNode() || ! Var.isNamedVar(item.getNode()) )
                broken(item, "Not a variable") ;
            x.add(Var.alloc(item.getNode()));
        }
        return x ;
    }

    protected static int buildInt(ItemList list, int idx)
    {
        Item item = list.get(idx) ;
        if ( !item.isNode() )
            broken(item, "Not an integer: "+item) ;
        Node node = item.getNode() ;
        if ( ! node.isLiteral() )
            broken(item, "Not an integer: "+item) ;

        NodeValue nv = NodeValue.makeNode(node) ;
        if ( ! nv.isInteger() )
            broken(item, "Not an integer: "+item) ;
        return nv.getInteger().intValue() ;
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
