/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.PrefixMapping;

import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.*;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.lang.arq.* ;
import com.hp.hpl.jena.sparql.serializer.FmtExpr;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.sse.SSEParseException;
import com.hp.hpl.jena.sparql.sse.builders.ExprBuildException;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;


/** Misc support for Expr
 * @author Andy Seaborne
 */

public class ExprUtils
{
 
    public static Expr nodeToExpr(Node n)
    {
        if ( n.isVariable() )
            return new ExprVar(n) ;
        return NodeValue.makeNode(n) ;
    }
    
    public static String joinList(List<Expr> args, String sep)
    {
        if ( args == null )
            return "<<Null list>>" ;
        
        if ( args.size() == 0 )
            return "<<Empty list>>" ;
        
        StringBuffer s = new StringBuffer() ;
        
        boolean first = true ;

        for ( Expr ex :  args )
        {
            if ( ! first )
                s.append(sep) ;
            // Values are printed withquoting.
            if ( ex instanceof NodeValue )
            {
                NodeValue nv =(NodeValue)ex ;
                s.append(nv.asQuotedString()) ;
            }
            else
                s.append(ex.toString()) ;
            first = false ;
        }
        return s.toString() ;
    }
    
    public static Expr parse(String s)
    {
        return parse(s, ARQConstants.getGlobalPrefixMap()) ;
    }
    
    public static Expr parse(String s, PrefixMapping pmap)
    { 
        Query query = QueryFactory.make() ;
        query.setPrefixMapping(pmap) ;
        return parse(query, s, true) ;
    }

    public static Expr parse(Query query, String s, boolean checkAllUsed)
    {
        try {
            Reader in = new StringReader(s) ;
            ARQParser parser = new ARQParser(in) ;
            parser.setQuery(query) ;
            Expr expr = parser.Expression() ;
            
            if ( checkAllUsed )
            {
                Token t = parser.getNextToken() ;
                if ( t.kind != ARQParserTokenManager.EOF )
                    throw new QueryParseException("Extra tokens beginning \""+t.image+"\" starting line "+t.beginLine+", column "+t.beginColumn,
                                                  t.beginLine, t.beginColumn) ;
            }
            return expr ;
        } catch (ParseException ex)
        { throw new QueryParseException(ex.getMessage(),
                                        ex.currentToken.beginLine,
                                        ex.currentToken.beginLine) ;
        }
        catch (TokenMgrError tErr)
        {
            throw new QueryParseException(tErr.getMessage(), -1, -1) ;
        }
        catch (Error err)
        {
            // The token stream can throw java.lang.Error's 
            String tmp = err.getMessage() ;
            if ( tmp == null )
                throw new QueryParseException(err,-1, -1) ;
            throw new QueryParseException(tmp,-1, -1) ;
        }
    }

    public static NodeValue parseNodeValue(String s)
    {
        Node n = NodeFactory.parseNode(s) ;
        NodeValue nv = NodeValue.makeNode(n) ;
        return nv ;
    }
    
    public static void fmtSPARQL(IndentedWriter iOut, Expr expr, SerializationContext sCxt)
    {
        FmtExpr v = new FmtExpr(iOut, sCxt) ;
        v.format(expr) ;
    }
    
    public static void fmtSPARQL(IndentedWriter iOut, Expr expr)
    {
        fmtSPARQL(iOut, expr, FmtUtils.sCxt()) ;
    }
    
    public static String fmtSPARQL(Expr expr)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        fmtSPARQL(buff, expr) ;
        return buff.toString() ; 
    }
    
    // ExprLists
    
    public static void fmtSPARQL(IndentedWriter iOut, ExprList exprs, SerializationContext pmap)
    {
        FmtExpr fmt = new FmtExpr(iOut, pmap) ;
        String sep = "" ;
        for (Expr expr : exprs)
        {
            iOut.print(sep) ;
            sep = " , " ;
            fmt.format(expr) ;
        }
    }

    public static void fmtSPARQL(IndentedWriter iOut, ExprList exprs)
    {
        fmtSPARQL(iOut, exprs, FmtUtils.sCxt()) ;
    }

    public static String fmtSPARQL(ExprList exprs)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        fmtSPARQL(buff, exprs) ;
        return buff.toString() ; 
    }

    public static String strComparison(int cmp)
    {
        switch (cmp)
        {
            case Expr.CMP_GREATER:   return "GT" ;
            case Expr.CMP_EQUAL:     return "EQ" ;
            case Expr.CMP_LESS:      return "LT" ;
            case Expr.CMP_INDETERMINATE:      return "indeterminate" ;
            default:            return "??" ;
        }
    }
    
    // --- Debugging : evaluate and print
    
    public static void expr(String exprStr) { expr(exprStr, null) ; }
    public static void expr(String exprStr, Binding binding)
    {
        try {
            Expr expr = ExprUtils.parse(exprStr, ARQConstants.getGlobalPrefixMap()) ;
            evalPrint(expr, binding) ;
        }
        catch (QueryParseException ex)
        {
            System.err.println("Parse error: "+ex.getMessage()) ;
            return ;
        }
    }
    
    public static void exprPrefix(String exprStr)
    {
        exprPrefix(exprStr, null) ;
    }

    public static void evalPrint(Expr expr, Binding binding)
    {
        try {
            Context context = ARQ.getContext().copy() ;
            context.set(ARQConstants.sysCurrentTime, NodeFactory.nowAsDateTime()) ;
            FunctionEnv env = new ExecutionContext(context, null, null, null) ; 
            NodeValue r = expr.eval(binding, env) ;
            //System.out.println(r.asQuotedString()) ;
            Node n = r.asNode() ;
            String s = FmtUtils.stringForNode(n) ;
            System.out.println(s) ;
        } catch (ExprEvalException ex)
        {
            System.out.println("Exception: "+ex.getMessage()) ;
            return ;
        }
        catch (ExprBuildException ex)
        {
            System.err.println("Build exception: "+ex.getMessage()) ;
            return ;
        }
    }

    public static void exprPrefix(String string, Binding binding)
    {
        try {
            Expr expr = SSE.parseExpr(string) ;
            evalPrint(expr, binding) ;
        }
        catch (SSEParseException ex)
        {
            System.err.println("Parse error: "+ex.getMessage()) ;
            return ;
        }  
    }
}

/*
 *  (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
