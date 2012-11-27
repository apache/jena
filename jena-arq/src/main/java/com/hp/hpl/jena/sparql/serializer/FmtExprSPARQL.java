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

package com.hp.hpl.jena.sparql.serializer;

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.expr.* ;
import com.hp.hpl.jena.sparql.syntax.Element ;

/** Output expressions in SPARQL syntax */

public class FmtExprSPARQL
{
    static final int INDENT = 2 ;
    
    FmtExprARQVisitor visitor ; 

    public FmtExprSPARQL(IndentedWriter writer, SerializationContext cxt)
    {
        visitor = new FmtExprARQVisitor(writer, cxt) ;
    }
    
    // Top level writing of an expression.
    public void format(Expr expr)
    { expr.visit(visitor) ; } 
    
    public static void format(IndentedWriter out,Expr expr)
    { format(out, expr, null) ; }
    
    public static void format(IndentedWriter out, Expr expr, SerializationContext cxt)
    {
        FmtExprSPARQL fmt = new FmtExprSPARQL(out, cxt) ;
        fmt.format(expr) ;
    }

    private static class FmtExprARQVisitor implements ExprVisitor
    {
        IndentedWriter out ;
        SerializationContext context ;

        public FmtExprARQVisitor(IndentedWriter writer, PrefixMapping pmap)
        {
            this(writer, new SerializationContext(pmap , null)) ;
        }

        public FmtExprARQVisitor(IndentedWriter writer, SerializationContext cxt)
        {
            out = writer ;
            context = cxt ;
            if ( context == null )
                context = new SerializationContext() ;
        }

        @Override
        public void startVisit() { }

        @Override
        public void visit(ExprFunction0 expr)
        {
            if ( expr.getOpName() == null )
            {
                printInFunctionForm(expr) ;
                return ;
            }
            out.print("( ") ;
            out.print( expr.getOpName() ) ;
            out.print(" ") ;
        }

        
        @Override
        public void visit(ExprFunction1 expr)
        {
            if ( expr.getOpName() == null )
            {
                printInFunctionForm(expr) ;
                return ;
            }
            out.print("( ") ;
            out.print( expr.getOpName() ) ;
            out.print(" ") ;
            expr.getArg().visit(this) ;
            out.print(" )");
        }

        @Override
        public void visit(ExprFunction2 expr)
        {
            if ( expr.getOpName() == null )
            {
                printInFunctionForm(expr) ;
                return ;
            }
            out.print("( ") ;
            expr.getArg1().visit(this) ;
            out.print(" ") ;
            out.print( expr.getOpName() ) ;
            out.print(" ") ;
            expr.getArg2().visit(this) ;
            out.print(" )");
        }

        @Override
        public void visit(ExprFunction3 expr)
        {
            printInFunctionForm(expr) ;
        }


        @Override
        public void visit(ExprFunctionN func)
        {
            if ( func instanceof E_OneOf )
            {
                E_OneOf oneOf = (E_OneOf)func ;
                out.print("( ") ;
                oneOf.getLHS().visit(this) ;
                out.print(" IN ") ;
                printExprList(oneOf.getRHS()) ;
                out.print(" )") ;
                return ;
            }

            if ( func instanceof E_NotOneOf )
            {
                E_NotOneOf oneOf = (E_NotOneOf)func ;
                out.print("( ") ;
                oneOf.getLHS().visit(this) ;
                out.print(" NOT IN ") ;
                printExprList(oneOf.getRHS()) ;
                out.print(" )") ;
                return ;
            }
            printInFunctionForm(func) ;
        }

        private void printInFunctionForm(ExprFunction func)
        {
            out.print( func.getFunctionPrintName(context) ) ;
            printExprList(func.getArgs()) ;
        }
        
        private void printExprList(Iterable<Expr> exprs)
        {
            out.print("(") ;
            boolean first = true ;
            for ( Expr expr : exprs )
            {
                if ( expr == null )
                    break ; 
                if ( ! first )
                    out.print(", ") ;
                first = false ;
                expr.visit(this) ;
            }  
            out.print(")");
        }
        
        @Override
        public void visit(ExprFunctionOp funcOp)
        {
            String fn = funcOp.getFunctionPrintName(context) ;
            if ( funcOp instanceof E_NotExists )
                fn = "NOT EXISTS" ;
            else if ( funcOp instanceof E_Exists )
                fn = "EXISTS" ;
            else
                throw new ARQInternalErrorException("Unrecognized ExprFunctionOp: "+fn) ;
            
            FormatterElement fmtElt = new FormatterElement(out, context) ;
            out.print(fn) ;
            out.print(" ") ;
            Element el = funcOp.getElement() ; 
            if ( el == null )
                el = OpAsQuery.asQuery(funcOp.getGraphPattern()).getQueryPattern() ;
            el.visit(fmtElt) ;
        }

        @Override
        public void visit(NodeValue nv)
        {
            out.print(nv.asQuotedString(context)) ;
        }

        @Override
        public void visit(ExprVar nv)
        {
            String s = nv.getVarName() ;
            if ( Var.isBlankNodeVarName(s) )
            {
                // Return to a bNode via the bNode mapping of a variable.
                Var v = Var.alloc(s) ;
                out.print(context.getBNodeMap().asString(v) ) ;
            }
            else
            {
                // Print in variable form or as an aggregator expression
                out.print(nv.asSparqlExpr()) ;
            }
        }

        @Override
        public void visit(ExprAggregator eAgg)
        {
            out.print(eAgg.asSparqlExpr()) ;
        }
        
        
        @Override
        public void finishVisit() { out.flush() ; }
    }
}
