/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyrght 2010 Epimorphics Ltd.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import java.util.List ;

import com.hp.hpl.jena.query.ARQ ;
import org.openjena.atlas.logging.Log ;
import com.hp.hpl.jena.sparql.util.Symbol ;

/** Indirect to the choosen regular expression implementation */

public class E_Regex extends ExprFunctionN
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
    
    public E_Regex(Expr expr, Expr pattern, Expr flags)
    {
        super(name, expr, pattern, flags) ;
        init(pattern, flags) ;
    }

    // Not used by parser
    public E_Regex(Expr expr, String pattern, String flags)
    {
        super(name, expr, NodeValue.makeString(pattern), NodeValue.makeString(flags)) ;
        init(getArg(2), getArg(3)) ;
    }
    
    private void init(Expr pattern, Expr flags)
    {
        if ( pattern.isConstant() && pattern.getConstant().isString() && ( flags==null || flags.isConstant() ) )
            regexEngine = makeRegexEngine(pattern.getConstant(), (flags==null)?null:flags.getConstant()) ;
    }
    

    @Override
    public NodeValue eval(List<NodeValue> args)
    {
        NodeValue v = args.get(0) ;
        NodeValue vPattern = args.get(1) ;
        NodeValue vFlags = ( args.size() == 2 ? null : args.get(2) ) ;
        
        if ( ! v.isString() )
            throw new ExprEvalException("REGEX: "+v+" is not a string") ;

        RegexEngine regex = regexEngine ;
        if ( regex == null  )
            regex = makeRegexEngine(vPattern, vFlags) ;
        
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
    
//    /** @return Returns the expr of the regex */
//    public final Expr getRegexExpr() { return expr1 ; }
//
//    /** @return Returns the pattern. */
//    public final Expr getPattern()  { return expr2 ; }
//
//    /** @return Returns the flags. */
//    public final Expr getFlags() { return expr3 ; }

    @Override
    protected Expr copy(ExprList newArgs)
    {
        if ( newArgs.size() == 2 )
            return new E_Regex(newArgs.get(0), newArgs.get(1), null) ; 
        return new E_Regex(newArgs.get(0), newArgs.get(1), newArgs.get(2)) ;   
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyrght 2010 Epimorphics Ltd.
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
