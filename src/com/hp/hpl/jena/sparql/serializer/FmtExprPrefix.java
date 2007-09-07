/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.serializer;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.expr.*;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

/** Printing of an expr expression in prefix notation */

public class FmtExprPrefix  implements FmtExpr 
{
    static final boolean ONELINE = true ;
    IndentedWriter out ;
    SerializationContext context ;
    
    FmtExprPrefixVisitor visitor ; 
    
    public FmtExprPrefix(IndentedWriter writer, PrefixMapping pmap)
    {
        visitor = new FmtExprPrefixVisitor(writer, pmap) ;
    }

    public FmtExprPrefix(IndentedWriter writer, SerializationContext cxt)
    {
        visitor = new FmtExprPrefixVisitor(writer, cxt) ;

    }
    
//    public ExprVisitor getVisitor()
//    {
//        return this ;
//    }

    public void format(Expr expr)
    { expr.visit(visitor) ; }
    
    public static void format(IndentedWriter out, PrefixMapping pmap, Expr expr)
    {
        FmtExpr fmt = new FmtExprPrefix(out, new SerializationContext(pmap)) ;
        fmt.format(expr) ;
    }

    public static void format(IndentedWriter out, SerializationContext cxt, Expr expr)
    {
        FmtExpr fmt = new FmtExprPrefix(out, cxt) ;
        fmt.format(expr) ;
    }
    

    class FmtExprPrefixVisitor implements ExprVisitor
    {
        public FmtExprPrefixVisitor(IndentedWriter writer, PrefixMapping pm)
        {
            this(writer, new SerializationContext(pm , null)) ;
        }

        public FmtExprPrefixVisitor(IndentedWriter writer, SerializationContext qCxt)
        {
            out = writer ;
            context = qCxt ;
            if ( context == null )
                context = new SerializationContext() ;
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

            out.incIndent(INDENT) ;
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
            out.decIndent(INDENT) ;
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
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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