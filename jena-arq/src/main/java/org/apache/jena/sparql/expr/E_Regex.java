/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.sparql.expr;

import java.util.List ;

import org.apache.commons.lang3.StringUtils ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions ;
import org.apache.jena.sparql.sse.Tags ;
import org.apache.jena.sparql.util.FmtUtils ;
import org.apache.jena.sparql.util.Symbol ;

/** Indirect to the chosen regular expression implementation */

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
    
    private static final String name = Tags.tagRegex ;
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
        if ( ! ARQ.isStrictMode() ) {
            try {
                if ( pattern.isConstant() && pattern.getConstant().isString() && ( flags==null || flags.isConstant() ) )
                    regexEngine = makeRegexEngine(pattern.getConstant(), (flags==null)?null:flags.getConstant()) ;
            } catch (ExprEvalException ex) {
                // Here, we are doing static compilation of the pattern.
                // ExprEvalException does not have a stacktrace. 
                // We could throw a non-eval exception.
                throw ex; //new ExprException(ex.getMessage(), ex.getCause());
            }
        }
    }

    private String currentFailMessage = null;
    
    @Override
    public NodeValue eval(List<NodeValue> args)
    {
        Node arg = NodeFunctions.checkAndGetStringLiteral("REGEX", args.get(0)) ;
        NodeValue vPattern = args.get(1) ;
        NodeValue vFlags = ( args.size() == 2 ? null : args.get(2) ) ;
        
        RegexEngine regex = regexEngine ;
        if ( regex == null  ) {
            // Execution time regex compile (not a constant pattern).
            try {
                regex = makeRegexEngine(vPattern, vFlags) ;
            } catch (ExprEvalException ex) {
                // Avoid multiple logging of the same message (at least if adjacent) 
                String m = ex.getMessage();
                if ( m != null && ! m.equals(currentFailMessage) )
                    Log.warn(this, m);
                currentFailMessage = m;
                // This becomes an eval error, the FILTER is false and the current row rejected. 
                throw ex;
            }
        }
        boolean b = regex.match(arg.getLiteralLexicalForm()) ;
        return b ? NodeValue.TRUE : NodeValue.FALSE ; 
    }

    public static RegexEngine makeRegexEngine(NodeValue vPattern, NodeValue vFlags)
    {
        if ( ! vPattern.isString() )
            throw new ExprException("REGEX: Pattern is not a string: "+vPattern) ;
        if ( vFlags != null && ! vFlags.isString() )
            throw new ExprException("REGEX: Pattern flags are not a string: "+vFlags) ;
        String s = (vFlags==null)?null:vFlags.getString() ;
        checkFlags(s);
        
        return makeRegexEngine(vPattern.getString(), s) ;
    }
    
    private static void checkFlags(String flags) {
        if ( flags == null )
            return;
        // F&O spec defines regex: Can only contain s, m, i, x, q
        // Not all are supported by all regex engines.
        if ( ! StringUtils.containsOnly(flags, "smixq") )
            throw new ExprEvalException("REGEX: Only 'smixq' are legal as pattern flags: got \""+FmtUtils.stringEsc(flags)+"\"");
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
    public Expr copy(ExprList newArgs)
    {
        if ( newArgs.size() == 2 )
            return new E_Regex(newArgs.get(0), newArgs.get(1), null) ; 
        return new E_Regex(newArgs.get(0), newArgs.get(1), newArgs.get(2)) ;   
    }
}
