/*
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.rdql.parser;

import com.hp.hpl.jena.rdql.* ;
import java.io.PrintWriter;

class Q_StringNotEqual extends SimpleNode implements Expr, ExprBoolean
{
    Expr left ;
    Expr right ;

    private String printName = "str!=" ;
    private String opSymbol = "ne" ;

    Q_StringNotEqual(int id) { super(id); }

    Q_StringNotEqual(RDQLParser p, int id) { super(p, id); }

    public Value eval(Query q, ResultBinding env)
    {
        // There is a decision here : do we allow anything to be
        // tested as string or do restrict ourselves to things
        // that started as strings.  Example: A URI is not string
        // so should be it be possible to have:
        //      ?x ne <uri>
        // Decision here is to allow string tests on anything.

        Value x = left.eval(q, env) ;
        Value y = right.eval(q, env) ;

        // Allow anything to be forced to be a string.
        /*
        if ( ! x.isString() )
            throw new EvalTypeException("Q_StringNotEqual: Wanted a string: "+x) ;
        if ( ! y.isString() )
            throw new EvalTypeException("Q_StringNotEqual: Wanted a string: "+y) ;
        String xx = x.getString() ;
        String yy = y.getString() ;
        */

        String xx = x.valueString() ;
        String yy = y.valueString() ;

        Settable result ;
        if ( x instanceof Settable )
            result = (Settable)x ;
        else if ( y instanceof Settable )
            result = (Settable)y ;
        else
            result = new WorkingVar() ;

        // The only difference with Q_StringEqual.eval
        boolean b = ! (xx.equals(yy)) ;

        result.setBoolean(b) ;
        return result ;

    }

    public void jjtClose()
    {
        int n = jjtGetNumChildren() ;
        if ( n != 2 )
            throw new QueryException("Q_StringNotEqual: Wrong number of children: "+n) ;

        left = (Expr)jjtGetChild(0) ;
        right = (Expr)jjtGetChild(1) ;
    }

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
 *  (c) Copyright Hewlett-Packard Company 2001
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
 *
 * This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/).
 *
 */
