/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.writers;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprFunctionOp;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.ExprVisitor;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedLineBuffer;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

public class WriterExpr
{
    
    public static String asString(Expr expr)
    {
        IndentedLineBuffer b = new IndentedLineBuffer() ;
        output(b, expr, null) ;
        return b.asString() ;
    }
    
    public static void output(IndentedWriter out, ExprList exprs, SerializationContext sCxt)
    {
        output(out, exprs, true, true, sCxt) ;
    }
    
    public static void output(IndentedWriter out, ExprList exprs, boolean withTag, boolean unlist, SerializationContext sCxt)
    {
        if ( exprs.size() == 0 )
        {
            out.print("()") ;
            return ;
        }
        
        if ( exprs.size() == 1 && unlist )
        {
            output(out, exprs.get(0), sCxt) ;
            return ;
        }
        
        if ( withTag )
            out.print("(exprlist ") ;
        else
            out.print("(") ;
        
        for ( int i = 0 ; i < exprs.size() ;  i++ )
        {
            if ( i != 0 ) out.print(" ") ;
            output(out, exprs.get(i), sCxt) ;
        }
        out.print(")") ;
    }
    
    private static void outputTail(IndentedWriter out, ExprList exprs, SerializationContext sCxt)
    {
        for ( int i = 0 ; i < exprs.size() ;  i++ )
        {
            out.print(" ") ;
            output(out, exprs.get(i), sCxt) ;
        }
        out.print(")") ;
    }
    
    public static void output(IndentedWriter out, Expr expr, SerializationContext sCxt)
    {
        FmtExprPrefixVisitor fmt = new FmtExprPrefixVisitor(out, sCxt) ;
        expr.visit(fmt) ;
    }

    // ----
    static final boolean ONELINE = true ;
    static class FmtExprPrefixVisitor implements ExprVisitor
    {
        IndentedWriter out ;
        SerializationContext context ;
        
        public FmtExprPrefixVisitor(IndentedWriter writer, SerializationContext cxt)
        {
            out = writer ;
            context = cxt ;
        }

        public void startVisit() {}

        public void visit(ExprFunction func)
        {
            out.print("(") ;

            String n = null ;

            if ( func.getOpName() != null )
                n = func.getOpName() ;

            if ( n == null )
                n = func.getFunctionPrintName(context) ;

            out.print(n) ;

            out.incIndent() ;
            for ( int i = 1 ; ; i++ )
            {
                Expr expr = func.getArg(i) ;
                if ( expr == null )
                    break ; 
                // endLine() ;
                out.print(' ') ;
                expr.visit(this) ;
            }
            out.print(")") ;
            out.decIndent() ;
        }

        public void visit(ExprFunctionOp funcOp)
        {
            out.print("(") ;
            
            // How far we are from current indent to current location
            // (beginning of operator name)
            int x = out.getCurrentOffset() ;
            // Indent to "("
            out.incIndent(x) ;
            
            out.print(funcOp.getFunctionName(context)) ;
            out.incIndent() ;
            
            Op op = funcOp.getOp() ;
            if ( oneLine(op) )
                out.print(" ") ;
            else
                out.ensureStartOfLine() ;
            
            //Ensures we are unit indent under the (operator ...)
            
            //Without trappings.
            WriterOp.outputNoPrologue(out, funcOp.getOp(), context) ;
            out.decIndent() ;
            out.decIndent(x) ;
            out.print(")") ;
            return ;
        }
        
        private static boolean oneLine(Op op)
        {
            if ( OpBGP.isBGP(op) )
            {
                BasicPattern bgp = ((OpBGP)op).getPattern() ;
                if ( bgp.getList().size() <= 1 )
                    return true ;
            }
            return false ;
        }
                               
        
        public void visit(NodeValue nv)
        {
            out.print(nv.asQuotedString(context)) ;
        }

        public void visit(ExprVar nv)
        {
            out.print(nv.toPrefixString()) ;
        }

        public void finishVisit() { out.flush() ; }

        private void endLine()
        {
            if ( ONELINE )
                out.print(' ') ;
            else
                out.println() ;
        }
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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