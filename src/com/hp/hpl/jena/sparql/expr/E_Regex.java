/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.Symbol;

//import java.util.regex.* ;

/** Indirect to the choosen regular expression implementation. 
 * 
 * @author Andy Seaborne
 */

public class E_Regex extends ExprFunction
{
    private static Symbol regexImpl = null ;
    static
    {
        Object v = ARQ.getContext().get(ARQ.regexImpl, ARQ.javaRegex) ;
        if ( v instanceof Symbol )
            regexImpl = (Symbol)v ;
        if ( v instanceof String )
            regexImpl = Symbol.create((String)v) ;
        
        if ( regexImpl == null )
        {
            ALog.warn(E_Regex.class, "Regex implementation some weird setting: default to Java") ;
            regexImpl = ARQ.javaRegex;
        }
        if ( !regexImpl.equals(ARQ.javaRegex) &&
             !regexImpl.equals(ARQ.xercesRegex) )
        {
            ALog.warn(E_Regex.class, "Regex implementation not recognized : default to Java") ;
            regexImpl = ARQ.javaRegex;
        }  
    }
    
    private static final String name = "regex" ;
    private RegexEngine regexEngine = null ;
    
    private Expr expr ;
    private Expr pattern ;
    private Expr flags ;
    
    public E_Regex(Expr _expr, Expr _pattern, Expr _flags)
    {
        super(name) ;
        expr = _expr ;
        pattern = _pattern ;
        flags = _flags ;
        init() ;
    }

    // Not used by parser
    public E_Regex(Expr _expr, String _pattern, String _flags)
    {
        super(name) ;
        expr = _expr ;
        pattern = NodeValue.makeString(_pattern) ;
        flags = NodeValue.makeString(_flags) ;
        init() ;
    }
    
    private void init()
    {
        if ( pattern.isConstant() && pattern.getConstant().isString() && ( flags==null || flags.isConstant() ) )
            regexEngine = makeRegexEngine(pattern.getConstant(), (flags==null)?null:flags.getConstant()) ;
    }
    
    @Override
    public NodeValue eval(Binding binding, FunctionEnv env)
    {
        NodeValue v = expr.eval(binding, env) ;
        if ( ! v.isString() )
            throw new ExprEvalException("REGEX: "+expr+" evaluates to "+v+", which is not a string") ;

        RegexEngine regex = regexEngine ;
        if ( regex == null  )
        {
            NodeValue vPattern = pattern.eval(binding, env) ;
            NodeValue vFlags = (flags==null) ? null : flags.eval(binding, env) ;
            regex = makeRegexEngine(vPattern, vFlags) ;
        }
        
        boolean b = regex.match(v.getString()) ;
        
        return b ?  NodeValue.TRUE : NodeValue.FALSE ; 
    }

    public static RegexEngine makeRegexEngine(NodeValue vPattern, NodeValue vFlags)
    {
        if ( ! vPattern.isString() )
            throw new ExprException("REGEX: Pattern is not a string: "+vPattern) ;
        if ( vFlags != null && ! vFlags.isString() )
            throw new ExprException("REGEX: Pattern flags are not a string: "+vFlags) ;
        String s = (vFlags==null)?null:vFlags.getString() ;
        
        return makeRegexEngine(vPattern.getString(), s) ;
    }
    
    public static RegexEngine makeRegexEngine(String pattern, String flags)
    {
        if ( regexImpl.equals(ARQ.xercesRegex))
            return new RegexXerces(pattern, flags) ;
        return new RegexJava(pattern, flags) ;
    }
    
    @Override
    public Expr getArg(int i)
    {
        if ( i == 1 )
            return expr ;
        if ( i == 2 )
            return pattern ;
        if ( i == 3 )
            return flags ;
        
        return null ;
    }

    @Override
    public int numArgs()
    {
        if ( flags != null )
            return 3 ;
        return 2 ;
    }

    /** @return Returns the expr of the regex */
    public Expr getRegexExpr() { return expr ; }

    /** @return Returns the flags. */
    public Expr getFlags() { return flags ; }

    /**
     * @return Returns the pattern.
     */
    public Expr getPattern()
    {
        return pattern ;
    }

    @Override
    public Expr copySubstitute(Binding binding, boolean foldConstants)
    {
        Expr e = expr.copySubstitute(binding, foldConstants) ;
        Expr p = pattern.copySubstitute(binding, foldConstants) ;
        Expr f = (flags==null)? null : flags.copySubstitute(binding, foldConstants) ;
        
        return new E_Regex(e,p,f) ;
    }

}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
