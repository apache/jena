/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.serializer;

import com.hp.hpl.jena.shared.PrefixMapping;

import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.*;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

/** Output expressions in the syntax that ARQ expects them */

public class FmtExpr
{
    static final int INDENT = 2 ;
    // Rename as ExprWriter, move to expr.
    
    FmtExprARQVisitor visitor ; 

//    public FmtExprARQ(IndentedWriter writer, PrefixMapping pmap)
//    {
//        visitor = new FmtExprARQVisitor(writer, pmap) ;
//    }

    public FmtExpr(IndentedWriter writer, SerializationContext cxt)
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
        FmtExpr fmt = new FmtExpr(out, cxt) ;
        fmt.format(expr) ;
    }

//    
//    // temporary workaround - need to rationalise FmtExpr
//    public ExprVisitor getVisitor() { return visitor ; }
    
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


        public void startVisit() { }

        private void visitFunction1(ExprFunction1 expr)
        {
            out.print("( ") ;
            out.print( expr.getOpName() ) ;
            out.print(" ") ;
            expr.getArg().visit(this) ;
            out.print(" )");
        }

        private void visitFunction2(ExprFunction2 expr)
        {
            out.print("( ") ;
            expr.getArg1().visit(this) ;
            out.print(" ") ;
            out.print( expr.getOpName() ) ;
            out.print(" ") ;
            expr.getArg2().visit(this) ;
            out.print(" )");
        }

        public void visit(ExprFunction func)
        {
            if ( func.getOpName() != null && func instanceof ExprFunction2 )
            {
                visitFunction2((ExprFunction2)func) ;
                return ;
            }

            if ( func.getOpName() != null && func instanceof ExprFunction1 )
            {
                visitFunction1((ExprFunction1)func) ;
                return ;
            }
            
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
        
        public void visit(ExprFunctionOp funcOp)
        {
            FormatterElement fmtElt = new FormatterElement(out, context) ;
            out.print(funcOp.getFunctionName(context)) ;
            out.print(" ") ;
            Element el = funcOp.getElement() ; 
            if ( el == null )
                el = OpAsQuery.asQuery(funcOp.getOp()).getQueryPattern() ;
            el.visit(fmtElt) ;
        }

        public void visit(NodeValue nv)
        {
            out.print(nv.asQuotedString(context)) ;
        }

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

        public void finishVisit() { out.flush() ; }
    }


}
/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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