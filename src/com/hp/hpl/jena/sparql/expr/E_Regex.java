/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.query.ARQ ;
import org.openjena.atlas.logging.Log ;
import com.hp.hpl.jena.sparql.util.Symbol ;

//import java.util.regex.* ;

/** Indirect to the choosen regular expression implementation. 
 * 
 * @author Andy Seaborne
 */

public class E_Regex extends ExprFunction3
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
            Log.warn(E_Regex.class, "Regex implementation some weird setting: default to Java") ;
            regexImpl = ARQ.javaRegex;
        }
        if ( !regexImpl.equals(ARQ.javaRegex) &&
             !regexImpl.equals(ARQ.xercesRegex) )
        {
            Log.warn(E_Regex.class, "Regex implementation not recognized : default to Java") ;
            regexImpl = ARQ.javaRegex;
        }  
    }
    
    private static final String name = "regex" ;
    private RegexEngine regexEngine = null ;
    
//    private final Expr expr ;
//    private final Expr pattern ;
//    private final Expr flags ;
    
    public E_Regex(Expr _expr, Expr _pattern, Expr _flags)
    {
        super(_expr, _pattern, _flags, name) ;
//        // Better names.
//        expr = _expr ;
//        pattern = _pattern ;
//        flags = _flags ;
        init(_pattern, _flags) ;
    }

    // Not used by parser
    public E_Regex(Expr _expr, String _pattern, String _flags)
    {
        super(_expr, NodeValue.makeString(_pattern), NodeValue.makeString(_flags), name) ;
//        expr = expr1 ;
//        pattern = expr2 ;
//        flags = expr3 ;
        init(expr2, expr3) ;
    }
    
    private void init(Expr pattern, Expr flags)
    {
        if ( pattern.isConstant() && pattern.getConstant().isString() && ( flags==null || flags.isConstant() ) )
            regexEngine = makeRegexEngine(pattern.getConstant(), (flags==null)?null:flags.getConstant()) ;
    }
    

    @Override
    public NodeValue eval(NodeValue v, NodeValue vPattern, NodeValue vFlags)
    {
        if ( ! v.isString() )
            throw new ExprEvalException("REGEX: "+v+" is not a string") ;

        RegexEngine regex = regexEngine ;
        if ( regex == null  )
        {
//            NodeValue vPattern = pattern.eval(binding, env) ;
//            NodeValue vFlags = (flags==null) ? null : flags.eval(binding, env) ;
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
    public int numArgs()
    {
        if ( expr3 != null )
            return 3 ;
        return 2 ;
    }

    /** @return Returns the expr of the regex */
    public final Expr getRegexExpr() { return expr1 ; }

    /** @return Returns the pattern. */
    public final Expr getPattern()  { return expr2 ; }

    /** @return Returns the flags. */
    public final Expr getFlags() { return expr3 ; }

    @Override
    public Expr copy(Expr arg1, Expr arg2, Expr arg3)
    {
        return new E_Regex(arg1, arg2, arg3) ;
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
