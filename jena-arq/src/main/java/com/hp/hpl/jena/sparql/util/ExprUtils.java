/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sparql.util;

import java.io.Reader ;
import java.io.StringReader ;
import java.util.List ;

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.expr.ExprVar ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.lang.arq.ARQParser ;
import com.hp.hpl.jena.sparql.lang.arq.ARQParserTokenManager ;
import com.hp.hpl.jena.sparql.lang.arq.ParseException ;
import com.hp.hpl.jena.sparql.lang.arq.Token ;
import com.hp.hpl.jena.sparql.lang.arq.TokenMgrError ;
import com.hp.hpl.jena.sparql.serializer.FmtExprSPARQL ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.SSEParseException ;
import com.hp.hpl.jena.sparql.sse.builders.ExprBuildException ;


/** Misc support for Expr */

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
        
        StringBuilder s = new StringBuilder() ;
        
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
        Node n = NodeFactoryExtra.parseNode(s) ;
        NodeValue nv = NodeValue.makeNode(n) ;
        return nv ;
    }
    
    public static void fmtSPARQL(IndentedWriter iOut, Expr expr, SerializationContext sCxt)
    {
        FmtExprSPARQL v = new FmtExprSPARQL(iOut, sCxt) ;
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
        FmtExprSPARQL fmt = new FmtExprSPARQL(iOut, pmap) ;
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
            NodeValue r = eval(expr, binding) ;
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

    public static NodeValue eval(Expr expr)
    {
        return eval(expr, BindingRoot.create()) ; 
    }
    
    public static NodeValue eval(Expr expr, Binding binding)
    {
        Context context = ARQ.getContext().copy() ;
        context.set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime()) ;
        FunctionEnv env = new ExecutionContext(context, null, null, null) ; 
        NodeValue r = expr.eval(binding, env) ;
        return r ;
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
