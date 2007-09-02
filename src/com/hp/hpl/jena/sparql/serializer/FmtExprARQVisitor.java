/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.serializer;

import com.hp.hpl.jena.shared.PrefixMapping;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.*;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

public class FmtExprARQVisitor implements ExprVisitor
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

    public static void format(IndentedWriter out, SerializationContext cxt, Expr expr)
    {
        ExprVisitor fmt = new FmtExprARQVisitor(out, cxt) ;
        fmt.startVisit() ;
        expr.visit(fmt) ;
        fmt.finishVisit() ;
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

        out.print( func.getFunctionPrintName(context) ) ;
        out.print("(") ;
        for ( int i = 1 ; ; i++ )
        {
            Expr expr = func.getArg(i) ;
            if ( expr == null )
                break ; 
            if ( i != 1 )
                out.print(", ") ;
            expr.visit(this) ;
        }
        out.print(")");
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
            out.print("?") ;
            out.print(nv.getVarName()) ;
        }
    }

    public void finishVisit() { out.flush() ; }
}


/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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