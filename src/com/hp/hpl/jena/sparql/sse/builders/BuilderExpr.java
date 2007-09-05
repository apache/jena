/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.builders;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.*;
import com.hp.hpl.jena.sparql.expr.aggregate.* ;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemList;

public class BuilderExpr
{
    // Build an expr list when they may also be a singe expression. 
    public static ExprList buildExprOrExprList(Item item)
    {
        if ( item.isTagged(Tags.tagExprList) )
            return buildExprList(item) ;
        Expr expr = buildExpr(item) ; 
        ExprList exprList = new ExprList(expr) ;
        return exprList ;
    }

    public static ExprList buildExprList(Item item)
    {
        if ( ! item.isTagged(Tags.tagExprList) )
            BuilderBase.broken(item, "Not Tags.tagged exprlist") ;
        ItemList list = item.getList() ;
        list = list.cdr();
        ExprList exprList = new ExprList() ;
        for ( Iterator iter = list.iterator() ; iter.hasNext() ; )
        {
            Item elt = (Item)iter.next() ;
            Expr expr = buildExpr(elt) ;
            exprList.add(expr) ;
        }
        return exprList ;
    }

    public static Expr buildExpr(Item item)
    {
        // If this (bob) is stateless, we can have one and use always.
        BuilderExpr bob = new BuilderExpr() ;
        return bob.buildItem(item) ;
    }

    public static NamedExprList buildNamedExprList(ItemList list)
    {
        NamedExprList x = new NamedExprList() ;

        for ( Iterator iter = list.iterator() ; iter.hasNext() ; )
        {
            Item item = (Item)iter.next() ;
            if ( item.isNode() )
            {
                Var v = BuilderNode.buildVar(item) ;
                x.add(v) ;
                continue ;
            }
            
            if ( !item.isList() || item.getList().size() != 2 )
                    BuilderBase.broken(item, "Not a var or var/expression pair") ;
            
            
            Var var = BuilderNode.buildVar(item.getList().get(0)) ;
            Expr expr = BuilderExpr.buildExpr(item.getList().get(1)) ;
            // XXX HACK
            if ( expr instanceof E_Aggregator )
                ((E_Aggregator)expr).setVar(var) ;
            x.add(var, expr) ;
            }
        return x ;
    }
    
    protected Map dispatch = new HashMap() ;
    public Expr buildItem(Item item)
    {
        Expr expr = null ;
    
        if ( item.isList() )
        {
            ItemList list = item.getList() ;
            
            if ( list.size() == 0 )
                BuilderBase.broken(item, "Empty list for expression") ;
            
            Item head = list.get(0) ;
            
            if ( head.isNode() )
                return buildFunctionCall(list) ;
            else if ( head.isList() )
                BuilderBase.broken(item, "Head is a list") ;
            else if ( head.isSymbol() )
            {
                if ( item.isTagged(Tags.tagExpr) )
                {
                    BuilderBase.checkLength(2, list, "Wrong length: "+item.shortString()) ;
                    item = list.get(1) ;
                    return buildItem(item) ;
                }
                
                return buildKnownFunction(list) ;                
            }
            throw new ARQInternalErrorException() ;
        }
    
        if ( item.isNode() )
        {
            if ( Var.isVar(item.getNode()) )
                return new ExprVar(Var.alloc(item.getNode())) ;
            return NodeValue.makeNode(item.getNode()) ;
        }
    
        if ( item.isSymbolIgnoreCase(Tags.tagTrue) )
            return NodeValue.TRUE ;
        if ( item.isSymbolIgnoreCase(Tags.tagFalse) )
            return NodeValue.FALSE ;
        
        BuilderBase.broken(item, "Not a list or a node or recognized symbol: "+item) ;
        return null ;
    }

    public BuilderExpr()
    {
        dispatch.put(Tags.tagRegex, buildRegex) ;
        dispatch.put(Tags.symEQ, buildEQ) ;
        dispatch.put(Tags.symNE, buildNE) ;
        dispatch.put(Tags.symGT, buildGT) ;
        dispatch.put(Tags.symLT, buildLT) ;
        dispatch.put(Tags.symLE, buildLE) ;
        dispatch.put(Tags.symGE, buildGE) ;
        dispatch.put(Tags.symOr, buildOr) ;     // Same builders for (or ..) and (|| ..)
        dispatch.put(Tags.tagOr, buildOr) ;
        dispatch.put(Tags.symAnd, buildAnd) ;   // Same builders for (and ..) and (&& ..)
        dispatch.put(Tags.tagAnd, buildAnd) ;
        dispatch.put(Tags.symPlus, buildPlus) ;
        dispatch.put(Tags.symMinus, buildMinus) ;
        dispatch.put(Tags.symMult, buildMult) ;
        dispatch.put(Tags.symDiv, buildDiv) ;
        dispatch.put(Tags.tagNot, buildNot) ;   // Same builders for (not ..) and (! ..)
        dispatch.put(Tags.symNot, buildNot) ;
        dispatch.put(Tags.tagStr, buildStr) ;
        dispatch.put(Tags.tagLang, buildLang) ;
        dispatch.put(Tags.tagLangMatches, buildLangMatches) ;
        dispatch.put(Tags.tagSameTerm, buildSameTerm) ;
        dispatch.put(Tags.tagDatatype, buildDatatype) ;
        dispatch.put(Tags.tagBound, buildBound) ;
        dispatch.put(Tags.tagIRI, buildIRI) ;
        dispatch.put(Tags.tagURI, buildURI) ;
        dispatch.put(Tags.tagIsBlank, buildIsBlank) ;
        dispatch.put(Tags.tagIsLiteral, buildIsLiteral) ;
        dispatch.put(Tags.tagCount, buildCount) ;
    }

    // See exprbuilder.rb

    static public interface Build { Expr make(ItemList list) ; }
    
    protected Build findBuild(String str)
    {
        for ( Iterator iter = dispatch.keySet().iterator() ; iter.hasNext() ; )
        {
            String key = (String)iter.next() ; 
            if ( str.equalsIgnoreCase(key) )    // ???
                return (Build)dispatch.get(key) ;
        }
        return null ;
    }
    
    protected Expr buildKnownFunction(ItemList list)
    {
        if ( list.size() == 0 )
            BuilderBase.broken(list, "Empty list for expression") ;
    
        Item item = list.get(0) ;
        String tag = item.getSymbol() ;
        if ( tag == null )
            BuilderBase.broken(item, "Null tag") ;
    
        Build b = findBuild(tag) ;
        if ( b == null )
            BuilderBase.broken(item, "No known symbol for "+tag) ;
        return b.make(list) ;
    }

    protected static Expr buildFunctionCall(ItemList list)
    {
        Item head = list.get(0) ;
        Node node = head.getNode() ;
        if ( node.isBlank() )
            BuilderBase.broken(head, "Blank node for function call!") ;
        if ( node.isLiteral() )
            BuilderBase.broken(head, "Literal node for function call!") ;
        ExprList args = buildArgs(list, 1) ;
        // Args
        return new E_Function(node.getURI(), args) ;
    }

    protected static ExprList buildArgs(ItemList list, int idx)
    {
        ExprList exprList = new ExprList() ;
        for ( int i = idx ; i < list.size() ; i++ )
        {
            Item item = list.get(i) ;
            exprList.add(buildExpr(item)) ;
        }
        return exprList ;
    }

    // ---- Dispatch objects
    // Can assume the tag is right (i.e. dispatched correctly) 
    // Specials
    
    final protected Build buildRegex = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(3, 4, list, "Regex: wanted 2 or 3 arguments") ;
            Expr expr = buildExpr(list.get(1)) ;
            Expr pattern = buildExpr(list.get(2)) ;
            Expr flags = null ;
            if ( list.size() != 3 )
                flags = buildExpr(list.get(3)) ;
            
            return new E_Regex(expr, pattern, flags) ;
        }
    };

    // Special
    final protected Build buildPlus = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(2, 3, list, "+: wanted 1 or 2 arguments") ;
            if ( list.size() == 2 )
            {
                Expr ex = buildExpr(list.get(1)) ;
                return new E_UnaryPlus(ex) ;
            }
            
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_Add(left, right) ;
        }
    };

    final protected Build buildMinus = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(2, 3, list, "-: wanted 1 or 2 arguments") ;
            if ( list.size() == 2 )
            {
                Expr ex = buildExpr(list.get(1)) ;
                return new E_UnaryMinus(ex) ;
            }
            
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_Subtract(left, right) ;
        }
    };
    
    final protected Build buildEQ = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(3, list, "=: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_Equals(left, right) ;
        }
    };

    final protected Build buildNE = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(3, list, "!=: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_NotEquals(left, right) ;
        }
    };

    final protected Build buildGT = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(3, list, ">: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_GreaterThan(left, right) ;
        }
    };

    final protected Build buildLT = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(3, list, "<: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_LessThan(left, right) ;
        }
    };

    final protected Build buildLE = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(3, list, "<=: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_LessThanOrEqual(left, right) ;
        }
    };

    final protected Build buildGE = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(3, list, ">=: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_GreaterThanOrEqual(left, right) ;
        }
    };

    final protected Build buildOr = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(3, list, "||: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_LogicalOr(left, right) ;
        }
    };

    final protected Build buildAnd = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(3, list, "&&: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_LogicalAnd(left, right) ;
        }
    };

    final protected Build buildMult = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(3, list, "*: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_Multiply(left, right) ;
        }
    };

    final protected Build buildDiv = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(3, list, "/: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_Divide(left, right) ;
        }
    };

    final protected Build buildNot = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(2, list, "!: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = buildExpr(list.get(1)) ;
            return new E_LogicalNot(ex) ;
        }
    };

    final protected Build buildStr = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(2, list, "str: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = buildExpr(list.get(1)) ;
            return new E_Str(ex) ;
        }
    };

    final protected Build buildLang = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(2, list, "lang: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = buildExpr(list.get(1)) ;
            return new E_Lang(ex) ;
        }
    };

    final protected Build buildLangMatches = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(3, list, "langmatches: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_LangMatches(left, right) ;
        }
    };

    final protected Build buildSameTerm = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(3, list, "sameterm: wanted 2 arguments: got :"+list.size()) ;
            Expr left = buildExpr(list.get(1)) ;
            Expr right = buildExpr(list.get(2)) ;
            return new E_SameTerm(left, right) ;
        }
    };

    final protected Build buildDatatype = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(2, list, "datatype: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = buildExpr(list.get(1)) ;
            return new E_Datatype(ex) ;
        }
    };

    final protected Build buildBound = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(2, list, "bound: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = buildExpr(list.get(1)) ;
            return new E_Bound(ex) ;
        }
    };

    final protected Build buildIRI = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(2, list, "isIRI: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = buildExpr(list.get(1)) ;
            return new E_IsIRI(ex) ;
        }
    };

    final protected Build buildURI = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(2, list, "isURI: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = buildExpr(list.get(1)) ;
            return new E_IsURI(ex) ;
        }
    };

    final protected Build buildIsBlank = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(2, list, "isBlank: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = buildExpr(list.get(1)) ;
            return new E_IsBlank(ex) ;
        }
    };

    final protected Build buildIsLiteral = new Build()
    {
        public Expr make(ItemList list)
        {
            BuilderBase.checkLength(2, list, "isLiteral: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = buildExpr(list.get(1)) ;
            return new E_IsLiteral(ex) ;
        }
    };
    
    // ---- Aggregate functions
    // (count)
    // (count distinct)
    // (count ?var)
    // (count distinct ?var)
    // Need a canonical name for the variable that wil be set by the aggregation.
    // Aggregator.getVarName.
    
    final protected Build buildCount = new Build()
    {
        public Expr make(final ItemList list)
        {
            ItemList x = list.cdr();    // drop "count"
            boolean distinct = false ;
            if ( x.size() > 0 && x.car().isSymbol(Tags.tagDistinct) )
            {
                distinct = true ;
                x = x.cdr();
            }
            
            AggregateFactory agg = null ;
            
            if ( x.size() > 1 )
                BuilderBase.broken(list, "Broken syntax: "+list.shortString()) ;
            
            if ( x.size() == 0 )
            {
                
                if ( ! distinct )
                    agg = AggCount.get() ;
                else
                    agg = AggCountDistinct.get() ;
            }
            else
            {
                Var v = BuilderNode.buildVar(x.get(0)) ;
                if ( ! distinct )
                    agg = new AggCountVar(v) ;
                else
                    agg = new AggCountVarDistinct(v) ;
            }
            return new E_Aggregator((Var)null, agg.create()) ; 
        }
    };
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