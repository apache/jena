/*
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdql.parser;

import com.hp.hpl.jena.rdql.* ;
import java.io.PrintWriter;

//import org.apache.oro.text.* ;
import org.apache.oro.text.regex.* ;
//import org.apache.oro.text.perl.Perl5Util ;
//import org.apache.oro.text.perl.MalformedPerl5PatternException ;


public class Q_StringMatch extends SimpleNode implements Expr, ExprBoolean
{
    Expr left ;
    Expr right ;
    Q_PatternLiteral regex = null ;
    //Perl5Util matcher = new Perl5Util() ;
    PatternCompiler compiler = new Perl5Compiler();
    PatternMatcher matcher = new Perl5Matcher();
    
    // Cache the compiled regular expression.
    
    private String printName = "strMatch" ;
    private String opSymbol = "=~" ;
    Pattern pattern = null ;
    
    Q_StringMatch(int id)
    { super(id); }
    
    Q_StringMatch(RDQLParser p, int id)
    { super(p, id); }
    
    
    public Value eval(Query q, ResultBinding env)
    {
        // There is a decision here : do we allow anything to be
        // tested as string or do restrict ourselves to things
        // that started as strings.  Example: A URI is not string
        // so should be it be possible to have:
        //      ?x ne <uri>
        // Decision here is to allow string tests on anything.
        
        Value x = left.eval(q, env) ;
        //Value y = right.eval(q, env) ;    // Must be a pattern literal
        
        // Allow anything to be forced to be a string.
        
        String xx = x.valueString() ;
        // Had better be the pattern string!
        //String yy = y.toString() ;
        
        Settable result = new WorkingVar() ;
        
        // Actually do it!
        boolean b = matcher.contains(xx, pattern) ;
        result.setBoolean(b) ;
        return result ;
    }
    
    public void jjtClose()
    {
        int n = jjtGetNumChildren() ;
        if ( n != 2 )
            throw new QueryException("Q_StringMatch: Wrong number of children: "+n) ;
        
        left = (Expr)jjtGetChild(0) ;
        right = (Expr)jjtGetChild(1) ;    // Must be a pattern literal
        if ( ! ( right instanceof Q_PatternLiteral ) )
            throw new EvalFailureException("Q_StringMatch: Pattern error") ;
        
        regex = (Q_PatternLiteral)right ;
        
        try
        {
            pattern = compiler.compile(regex.patternString, regex.mask) ;
        } catch (MalformedPatternException pEx)
        {
            throw new EvalFailureException("Q_StringMatch: Pattern exception: "+pEx) ;
        }
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
 *  (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
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
