/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.builders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.expr.E_Aggregator;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemList;


public class BuilderOp
{
    public static Op build(Item item)
    {
        if (item.isNode() )
            BuilderBase.broken(item, "Attempt to build op structure from a plain node") ;

        if (item.isSymbol() )
            BuilderBase.broken(item, "Attempt to build op structure from a bare symbol") ;

        BuilderOp b = new BuilderOp();
        return b.build(item.getList()) ;
    }

    protected Map dispatch = new HashMap() ;

    public BuilderOp()
    {
        dispatch.put(Tags.tagBGP, buildBGP) ;
        dispatch.put(Tags.tagQuadPattern, buildQuadPattern) ;
        dispatch.put(Tags.tagFilter, buildFilter) ;
        dispatch.put(Tags.tagGraph, buildGraph) ;
        dispatch.put(Tags.tagService, buildService) ;
        dispatch.put(Tags.tagJoin, buildJoin) ;
        dispatch.put(Tags.tagLeftJoin, buildLeftJoin) ;
        dispatch.put(Tags.tagDiff, buildDiff) ;
        dispatch.put(Tags.tagUnion, buildUnion) ;

        dispatch.put(Tags.tagToList, buildToList) ;
        dispatch.put(Tags.tagGroupBy, buildGroupBy) ;
        dispatch.put(Tags.tagOrderBy, buildOrderBy) ;
        dispatch.put(Tags.tagProject, buildProject) ;
        dispatch.put(Tags.tagDistinct, buildDistinct) ;
        dispatch.put(Tags.tagReduced, buildReduced) ;
        dispatch.put(Tags.tagAssign, buildAssign) ;
        dispatch.put(Tags.symAssign, buildAssign) ;
        dispatch.put(Tags.tagSlice, buildSlice) ;

        dispatch.put(Tags.tagTable, buildTable) ;
        dispatch.put(Tags.tagNull, buildNull) ;
    }

    // The main recursive build operation.
    public Op build(ItemList list)
    {
        if ( list == null )
            list = null ;

        Item head = list.get(0) ;
        String tag = head.getSymbol() ;

        Build bob = findBuild(tag) ;
        if ( bob != null )
            return bob.make(list) ;
        else
            BuilderBase.broken(head, "Unrecognized algebra operation: "+tag) ;
        return null ;
    }

    public static Expr buildExpr(Item item)
    {
        return BuilderExpr.buildExpr(item) ;
    }

    // Build a list of expressions.
    public static List buildExpr(ItemList list)
    {
        List x = new ArrayList() ;
        for ( int i = 0 ; i < list.size() ; i++ )
        {
            Item itemExpr = list.get(i) ;
            Expr expr = buildExpr(itemExpr) ;
            x.add(expr) ;
        }
        return x ;
    }


    protected Op build(ItemList list, int idx)
    {
        return build(list.get(idx).getList()) ;
    }

    protected Build findBuild(String str)
    {
        for ( Iterator iter = dispatch.keySet().iterator() ; iter.hasNext() ; )
        {
            String key = (String)iter.next() ; 
            if ( str.equalsIgnoreCase(key) )
                return (Build)dispatch.get(key) ;
        }
        return null ;
    }

    static public interface Build { Op make(ItemList list) ; }

    final protected Build buildTable = new Build()
    {
        public Op make(ItemList list)
        {
            Item t = Item.createList(list) ;
            Table table = BuilderTable.build(t) ; 
            return OpTable.create(table) ;
        }
    } ;

    final protected Build buildBGP = new Build()
    {
        public Op make(ItemList list)
        {
            BasicPattern triples = new BasicPattern() ;
            for ( int i = 1 ; i < list.size() ; i++ )
            {
                Item item = list.get(i) ;
                if ( ! item.isList() )
                    BuilderBase.broken(item, "Not a triple structure") ;
                Triple t = BuilderGraph.buildTriple(item.getList()) ;
                triples.add(t) ; 
            }
            return new OpBGP(triples) ;
        }
    } ;

    final protected Build buildQuadPattern = new Build()
    {
        public Op make(ItemList list)
        {
            Node g = null ;
            BasicPattern bp = new BasicPattern() ;
            for ( int i = 1 ; i < list.size() ; i++ )
            {
                Item item = list.get(i) ;
                if ( ! item.isList() )
                    BuilderBase.broken(item, "Not a quad structure") ;
                Quad q = BuilderGraph.buildQuad(item.getList()) ;
                if ( g == null )
                    g = q.getGraph() ;
                else
                {
                    if ( ! g.equals(q.getGraph()) )
                        BuilderBase.broken(item, "Quad has different graph node in quadapttern: "+q) ;
                }
                bp.add(q.getTriple()) ;
                
            }
            
            OpQuadPattern op = new OpQuadPattern(g, bp) ;
            return op ;
        }
    } ;

    final protected Build buildFilter = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderBase.checkLength(3, list, "Malformed filter") ;
            Item itemExpr = list.get(1) ;
            Item itemOp = list.get(2) ;

            Op op = build(itemOp.getList()) ;
            ExprList exprList = BuilderExpr.buildExprOrExprList(itemExpr) ;
            return OpFilter.filter(exprList, op) ;
        }
    } ;

    final protected Build buildJoin = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderBase.checkLength(3, list, "Join") ;
            Op left = build(list, 1) ;
            Op right  = build(list, 2) ;
            Op op = OpJoin.create(left, right) ;
            return op ;
        }
    } ;

    final protected Build buildLeftJoin = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderBase.checkLength(3, 4, list, "leftjoin: wanted 2 or 3 arguments") ;
            Op left = build(list, 1) ;
            Op right  = build(list, 2) ;
            Expr expr = null ;
            if ( list.size() == 4 ) 
                expr = buildExpr(list.get(3)) ;
            Op op = OpLeftJoin.create(left, right, expr) ;
            return op ;
        }
    } ;

    final protected Build buildDiff = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderBase.checkLength(3, 4, list, "diff: wanted 2 arguments") ;
            Op left = build(list, 1) ;
            Op right  = build(list, 2) ;
            Op op = OpDiff.create(left, right) ;
            return op ;
        }
    } ;

    final protected Build buildUnion = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderBase.checkLength(3, list, "union") ;
            Op left = build(list, 1) ;
            Op right  = build(list, 2) ;
            Op op = new OpUnion(left, right) ;
            return op ;
        }
    } ;

    final protected Build buildGraph = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderBase.checkLength(3, list, "graph") ;
            Node graph = BuilderNode.buildNode(list.get(1)) ;
            Op sub  = build(list, 2) ;
            return new OpGraph(graph, sub) ;
        }
    } ;

    final protected Build buildService = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderBase.checkLength(3, list, "service") ;
            Node service = BuilderNode.buildNode(list.get(1)) ;
            if ( ! service.isURI() && ! service.isVariable() )
                BuilderBase.broken(list, "Service must provide a URI or variable") ;
            Op sub  = build(list, 2) ;
            return new OpService(service, sub) ;
        }
    } ;

    final protected Build buildToList = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderBase.checkLength(2, list, "tolist") ;
            Op sub = build(list, 1) ;
            Op op = new OpList(sub) ;
            return op ;
        }
    } ;


    final protected Build buildGroupBy = new Build()
    {
        // See buildProject
        public Op make(ItemList list)
        {
            BuilderBase.checkLength(3, 4, list,  "Group") ;
            // GroupBy
            VarExprList vars = BuilderExpr.buildNamedExprList(list.get(1).getList()) ;
            List aggregators = new ArrayList() ;
            
            if ( list.size() == 4 )
            {
                // Aggregations : assume that the exprs are legal.
                VarExprList y = BuilderExpr.buildNamedExprList(list.get(2).getList()) ;
                aggregators.addAll(y.getExprs().values()) ;
                for ( Iterator iter = aggregators.iterator() ; iter.hasNext() ; )
                {
                    Expr expr = (Expr)iter.next() ;
                    if ( ! ( expr instanceof E_Aggregator ) )
                        BuilderBase.broken(list, "Not a aggregate expression: "+expr) ;
                }
            }
            Op sub = build(list, list.size()-1) ;
            Op op = new OpGroupAgg(sub,vars, aggregators) ;
            return op ;
        }
    } ;


    final protected Build buildOrderBy = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderBase.checkLength(3, list,  "Order") ;
            ItemList conditions = list.get(1).getList() ;
            
            // Maybe tagged (asc, (desc or a raw expression)
            List x = new ArrayList() ;
            
            for ( int i = 0 ; i < conditions.size() ; i++ )
            {
                int direction = Query.ORDER_DEFAULT ;
                Item item = conditions.get(i) ;
                SortCondition sc = scBuilder(item) ;
                x.add(sc) ;
            }
            Op sub = build(list, 2) ;
            Op op = new OpOrder(sub, x) ;
            return op ;
        }
    } ;

    SortCondition scBuilder(Item item)
    {
        int direction = Query.ORDER_DEFAULT ;
        if ( item.isTagged("asc") || item.isTagged("desc") )
        {
            
            BuilderBase.checkList(item) ;
            BuilderBase.checkLength(2, item.getList(), "Direction corrupt") ;
            if ( item.isTagged("asc") )
                direction = Query.ORDER_ASCENDING ;
            else
                direction = Query.ORDER_DESCENDING ;
            item = item.getList().get(1) ; 
        }
        Expr expr = BuilderExpr.buildExpr(item) ;
        if ( expr.isVariable() )
            return  new SortCondition(expr.getNodeVar().asVar(), direction) ;
        else
            return new SortCondition(expr, direction) ;
    }
    
    
    final protected Build buildProject = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderBase.checkLength(3, list, "project") ;
            List x = BuilderNode.buildVars(list.get(1).getList()) ; 
            Op sub = build(list, 2) ;
            return new OpProject(sub, x) ;
        }
    } ;

    
    final protected Build buildDistinct = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderBase.checkLength(2, list, "distinct") ;
            Op sub = build(list, 1) ;
            return new OpDistinct(sub) ;
        }
    } ;

    final protected Build buildReduced = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderBase.checkLength(2, list, "reduced") ;
            Op sub = build(list, 1) ;
            return new OpReduced(sub) ;
        }
    } ;

    final protected Build buildAssign = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderBase.checkLength(3, list, "assign") ;
            VarExprList x = BuilderExpr.buildNamedExprList(list.get(1).getList()) ; 
            Op sub = build(list, 2) ;
            return new OpAssign(sub, x) ;
        }
    } ;

    

    final protected Build buildSlice = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderBase.checkLength(4, list, "slice") ;
            long start = BuilderNode.buildInt(list, 1, -1) ;
            long length = BuilderNode.buildInt(list, 2, -1) ;

            if ( start == -1 )
                start = Query.NOLIMIT ;
            if ( length == -1 )
                length = Query.NOLIMIT ;

            Op sub = build(list, 3) ;
            return new OpSlice(sub, start, length) ;
        }
    } ;

    final protected Build buildNull = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderBase.checkLength(1, list, "null") ;
            return new OpNull() ;
        }
    } ;

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
