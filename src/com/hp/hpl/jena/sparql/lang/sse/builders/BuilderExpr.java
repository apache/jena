/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lang.sse.builders;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.*;
import com.hp.hpl.jena.sparql.lang.sse.Item;
import com.hp.hpl.jena.sparql.lang.sse.ItemList;
import com.hp.hpl.jena.sparql.lang.sse.builders.Builder;
import com.hp.hpl.jena.sparql.lang.sse.builders.BuilderExpr;

public class BuilderExpr
{
    public static Expr build(Item item)
    {
        BuilderExpr bob = new BuilderExpr() ;
        return bob.buildItem(item) ;
    }

    protected Map dispatch = new HashMap() ;
    public Expr buildItem(Item item)
    {
        Expr expr = null ;
    
        if ( item.isList() )
        {
            Item head = item.getList().get(0) ;
            if ( head.isNode() )
                return buildFunctionCall(item.getList()) ;
            else if ( head.isWord() )
                return buildKnownFunction(item.getList()) ;
            else if ( head.isList() )
                Builder.broken(item, "Head is a list") ;
        }
    
        if ( item.isNode() )
        {
            if ( Var.isVar(item.getNode()) )
                return new NodeVar(Var.alloc(item.getNode())) ;
            return NodeValue.makeNode(item.getNode()) ;
        }
    
        String w = item.getWord() ;
        if ( w.equalsIgnoreCase(symTrue) )
            return NodeValue.TRUE ;
        if ( w.equalsIgnoreCase(symFalse) )
            return NodeValue.FALSE ;
        
        Builder.broken(item, "Not a list or a node or recognized word: "+item) ;
        return null ;
    }

    public BuilderExpr()
    {
        dispatch.put(symRegex, buildRegex) ;
        
        dispatch.put(symEQ, buildEQ) ;
        dispatch.put(symNE, buildNE) ;
        dispatch.put(symGT, buildGT) ;
        dispatch.put(symLT, buildLT) ;
        dispatch.put(symLE, buildLE) ;
        dispatch.put(symGE, buildGE) ;
        dispatch.put(symOrSym, buildOrSym) ;
        dispatch.put(symOr, buildOr) ;
        dispatch.put(symAndSym, buildAndSym) ;
        dispatch.put(symAnd, buildAnd) ;
        dispatch.put(symPlus, buildPlus) ;
        dispatch.put(symMinus, buildMinus) ;
        dispatch.put(symMult, buildMult) ;
        dispatch.put(symDiv, buildDiv) ;
        dispatch.put(symNot, buildNot) ;
        dispatch.put(symNotSym, buildNotSym) ;
        dispatch.put(symStr, buildStr) ;
        dispatch.put(symLang, buildLang) ;
        dispatch.put(symLangMatches, buildLangMatches) ;
        dispatch.put(symSameTerm, buildSameTerm) ;
        dispatch.put(symDatatype, buildDatatype) ;
        dispatch.put(symBound, buildBound) ;
        dispatch.put(symIRI, buildIRI) ;
        dispatch.put(symURI, buildURI) ;
        dispatch.put(symIsBlank, buildIsBlank) ;
        dispatch.put(symIsLiteral, buildIsLiteral) ;
    }

    // See exprbuilder.rb
    final static String symEQ = "=" ;
    final static String symNE = "!=" ;
    final static String symGT = ">" ;
    final static String symLT = "<" ;
    final static String symLE = "<=" ;
    final static String symGE = ">=" ;
    final static String symOr = "||" ;
    final static String symOrSym = "or" ;
    final static String symAnd = "&&" ;
    final static String symAndSym = "and" ;
    final static String symPlus = "+" ;
    final static String symMinus = "-" ;
    final static String symMult = "*" ;
    final static String symDiv = "/" ;
    final static String symNot = "!" ;
    final static String symNotSym = "!" ;
    final static String symStr = "str" ;
    final static String symLang = "lang" ;
    final static String symLangMatches = "langmatches" ;
    final static String symSameTerm = "sameterm" ;
    final static String symDatatype = "datatype" ;
    final static String symBound = "bound" ;
    final static String symIRI = "isIRI" ;
    final static String symURI = "isURI" ;
    final static String symIsBlank = "isBlank" ;
    final static String symIsLiteral = "isLiteral" ;
    final static String symRegex = "regex" ;
    
    final static String symTrue = "true" ;
    final static String symFalse = "false" ;

    static public interface Build { Expr make(ItemList list) ; }
    
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
    
    // Specials
    
    final protected Build buildRegex = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(3, 4, list, "Regex") ;
            Expr expr = build(list.get(1)) ;
            Expr pattern = build(list.get(2)) ;
            Expr flags = null ;
            if ( list.size() != 3 )
                flags = build(list.get(3)) ;
            
            return new E_Regex(expr, pattern, flags) ;
        }
    };

    // Special
    final protected Build buildPlus = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(2, 3, list, "+") ;
            if ( list.size() == 2 )
            {
                Expr ex = build(list.get(1)) ;
                return new E_UnaryPlus(ex) ;
            }
            
            Expr left = build(list.get(1)) ;
            Expr right = build(list.get(2)) ;
            return new E_Add(left, right) ;
        }
    };

    final protected Build buildMinus = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(2, 3, list, "-") ;
            if ( list.size() == 2 )
            {
                Expr ex = build(list.get(1)) ;
                return new E_UnaryMinus(ex) ;
            }
            
            Expr left = build(list.get(1)) ;
            Expr right = build(list.get(2)) ;
            return new E_Subtract(left, right) ;
        }
    };


    
    final protected Build buildEQ = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(3, list, "=: wanted 2 arguments: got :"+list.size()) ;
            Expr left = build(list.get(1)) ;
            Expr right = build(list.get(2)) ;
            return new E_Equals(left, right) ;
        }
    };

    final protected Build buildNE = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(3, list, "!=: wanted 2 arguments: got :"+list.size()) ;
            Expr left = build(list.get(1)) ;
            Expr right = build(list.get(2)) ;
            return new E_NotEquals(left, right) ;
        }
    };

    final protected Build buildGT = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(3, list, ">: wanted 2 arguments: got :"+list.size()) ;
            Expr left = build(list.get(1)) ;
            Expr right = build(list.get(2)) ;
            return new E_GreaterThan(left, right) ;
        }
    };

    final protected Build buildLT = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(3, list, "<: wanted 2 arguments: got :"+list.size()) ;
            Expr left = build(list.get(1)) ;
            Expr right = build(list.get(2)) ;
            return new E_LessThan(left, right) ;
        }
    };

    final protected Build buildLE = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(3, list, "<=: wanted 2 arguments: got :"+list.size()) ;
            Expr left = build(list.get(1)) ;
            Expr right = build(list.get(2)) ;
            return new E_LessThanOrEqual(left, right) ;
        }
    };

    final protected Build buildGE = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(3, list, ">=: wanted 2 arguments: got :"+list.size()) ;
            Expr left = build(list.get(1)) ;
            Expr right = build(list.get(2)) ;
            return new E_GreaterThanOrEqual(left, right) ;
        }
    };

    final protected Build buildOrSym = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(3, list, "or: wanted 2 arguments: got :"+list.size()) ;
            Expr left = build(list.get(1)) ;
            Expr right = build(list.get(2)) ;
            return new E_LogicalOr(left, right) ;
        }
    };

    final protected Build buildOr = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(3, list, "||: wanted 2 arguments: got :"+list.size()) ;
            Expr left = build(list.get(1)) ;
            Expr right = build(list.get(2)) ;
            return new E_LogicalOr(left, right) ;
        }
    };

    final protected Build buildAndSym = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(3, list, "and: wanted 2 arguments: got :"+list.size()) ;
            Expr left = build(list.get(1)) ;
            Expr right = build(list.get(2)) ;
            return new E_LogicalAnd(left, right) ;
        }
    };

    final protected Build buildAnd = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(3, list, "&&: wanted 2 arguments: got :"+list.size()) ;
            Expr left = build(list.get(1)) ;
            Expr right = build(list.get(2)) ;
            return new E_LogicalAnd(left, right) ;
        }
    };

    final protected Build buildMult = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(3, list, "*: wanted 2 arguments: got :"+list.size()) ;
            Expr left = build(list.get(1)) ;
            Expr right = build(list.get(2)) ;
            return new E_Multiply(left, right) ;
        }
    };

    final protected Build buildDiv = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(3, list, "/: wanted 2 arguments: got :"+list.size()) ;
            Expr left = build(list.get(1)) ;
            Expr right = build(list.get(2)) ;
            return new E_Divide(left, right) ;
        }
    };

    final protected Build buildNot = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(2, list, "!: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = build(list.get(1)) ;
            return new E_LogicalNot(ex) ;
        }
    };

    final protected Build buildNotSym = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(2, list, "not: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = build(list.get(1)) ;
            return new E_LogicalNot(ex) ;
        }
    };

    final protected Build buildStr = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(2, list, "str: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = build(list.get(1)) ;
            return new E_Str(ex) ;
        }
    };

    final protected Build buildLang = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(2, list, "lang: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = build(list.get(1)) ;
            return new E_Lang(ex) ;
        }
    };

    final protected Build buildLangMatches = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(3, list, "langmatches: wanted 2 arguments: got :"+list.size()) ;
            Expr left = build(list.get(1)) ;
            Expr right = build(list.get(2)) ;
            return new E_LangMatches(left, right) ;
        }
    };

    final protected Build buildSameTerm = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(3, list, "sameterm: wanted 2 arguments: got :"+list.size()) ;
            Expr left = build(list.get(1)) ;
            Expr right = build(list.get(2)) ;
            return new E_SameTerm(left, right) ;
        }
    };

    final protected Build buildDatatype = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(2, list, "datatype: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = build(list.get(1)) ;
            return new E_Datatype(ex) ;
        }
    };

    final protected Build buildBound = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(2, list, "bound: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = build(list.get(1)) ;
            return new E_Bound(ex) ;
        }
    };

    final protected Build buildIRI = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(2, list, "isIRI: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = build(list.get(1)) ;
            return new E_IsIRI(ex) ;
        }
    };

    final protected Build buildURI = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(2, list, "isURI: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = build(list.get(1)) ;
            return new E_IsURI(ex) ;
        }
    };

    final protected Build buildIsBlank = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(2, list, "isBlank: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = build(list.get(1)) ;
            return new E_IsBlank(ex) ;
        }
    };

    final protected Build buildIsLiteral = new Build()
    {
        public Expr make(ItemList list)
        {
            Builder.checkLength(2, list, "isLiteral: wanted 1 arguments: got :"+list.size()) ;
            Expr ex = build(list.get(1)) ;
            return new E_IsLiteral(ex) ;
        }
    };


    protected Expr buildKnownFunction(ItemList list)
    {
        if ( list.size() == 0 )
            Builder.broken(list, "Empty list for expression") ;

        Item item = list.get(0) ;
        String tag = item.getWord() ;
        if ( tag == null )
            Builder.broken(item, "Null tag") ;

        Build b = findBuild(tag) ;
        if ( b == null )
            Builder.broken(item, "No known symbol for "+tag) ;
        return b.make(list) ;
    }

    protected static Expr buildFunctionCall(ItemList list)
    {
        Item head = list.get(0) ;
        Node node = head.getNode() ;
        if ( node.isBlank() )
            Builder.broken(head, "Blank node for function call!") ;
        if ( node.isLiteral() )
            Builder.broken(head, "Literal node for function call!") ;
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
            exprList.add(build(item)) ;
        }
        return exprList ;
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