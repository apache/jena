/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdql.parser;



import java.io.PrintWriter;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.query.IndexValues;
import com.hp.hpl.jena.graph.query.Expression;
import com.hp.hpl.jena.rdql.*;

public class Q_StringEqual extends ExprNode implements Expr, ExprBoolean
{
    Expr left ;
    Expr right ;

    static protected boolean enableRDFLiteralSameValueAs = true ;

    protected static String printName = "str=" ;
    protected static String opSymbol = "eq" ;

    Q_StringEqual(int id) { super(id); }

    Q_StringEqual(RDQLParser p, int id) { super(p, id); }

    protected boolean rawEval(NodeValue x, NodeValue y)
    {
        // There is a decision here : do we allow anything to be
        // tested as string or do restrict ourselves to things
        // that started as strings.  Example: A URI is not string
        // so should be it be possible to have:
        //      ?x ne <uri>
        // Decision here is to allow string tests on anything.
        
        // Jena2 - another decision point.
        // If we know left and right are types/lang-tagged literals,
        // do we apply a stricter test of "equal"?

        if ( enableRDFLiteralSameValueAs )
        {
            if ( x.isNode() && x.getNode().isLiteral() &&
                 y.isNode() && y.getNode().isLiteral() )
            {
                Node xNode = x.getNode() ;
                Node yNode = y.getNode() ;
                return xNode.sameValueAs(yNode) ;
            }
        }

        // Allow anything to be forced to be a string.
        String xx = x.valueString() ;
        String yy = y.valueString() ;

        return (xx.equals(yy)) ;
    }

    public NodeValue eval(Query q, IndexValues env)
    {
        NodeValue x = left.eval(q, env) ;
        NodeValue y = right.eval(q, env) ;
        
        boolean b = rawEval(x, y) ;
                
        NodeValueSettable result ;
        if ( x instanceof NodeValueSettable )
            result = (NodeValueSettable)x ;
        else if ( y instanceof NodeValueSettable )
            result = (NodeValueSettable)y ;
        else
            result = new WorkingVar() ;

        result.setBoolean(b) ;
        return result ;
    }

    public void jjtClose()
    {
        int n = jjtGetNumChildren() ;
        if ( n != 2 )
            throw new QueryException("Q_StringEqual: Wrong number of children: "+n) ;

        left = (Expr)jjtGetChild(0) ;
        right = (Expr)jjtGetChild(1) ;
    }

    // graph.query.Expression

    public boolean isApply()         { return true ; }
    public String getFun()           { return this.getClass().getName() ; } // For URI of the function
    public int argCount()            { return 2; }
    public Expression getArg(int i)  
    {
        if ( i == 0 && left instanceof Expression )
            return (Expression)left ;
        if ( i == 1 && right instanceof Expression )
            return (Expression)right ;
        return null;
    }

    // ----
    
    public String asInfixString()
    {
        return QueryPrintUtils.asInfixString2(left, right, printName, opSymbol) ;
    }

    public String asPrefixString()
    {
        return QueryPrintUtils.asPrefixString(left, right, printName, opSymbol) ;
    }

    public void print(PrintWriter pw, int level)
    {
        QueryPrintUtils.print(pw, left, right, printName, opSymbol, level) ;
    }

    public String toString()
    {
        return asInfixString() ;
    }
}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
