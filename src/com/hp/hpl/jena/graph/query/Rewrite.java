/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: Rewrite.java,v 1.7 2004-08-13 17:11:56 chris-dollin Exp $
*/
package com.hp.hpl.jena.graph.query;

/**
     Rewrite - class which does expression rewrites for Query
     @author kers
*/
public class Rewrite
    {
    public static Expression rewriteStringMatch( Expression e )
        {
        Expression L = e.getArg(0), R = e.getArg(1);
        PatternLiteral pattern = Rewrite.getPattern( R );
        if (pattern == null)
            return e;
        else if (isStartsWith( pattern ))
            return startsWith( L, pattern.getPatternString().substring(1), pattern.getPatternModifiers() );
        else if (isContains( pattern ))
            return contains( L, pattern.getPatternString(), pattern.getPatternModifiers() );
        else if (isEndsWith( pattern ))
            return endsWith( L, front( pattern.getPatternString() ), pattern.getPatternModifiers() ); 
        return e;
        }
    
    protected static String front( String s )
        { return s.substring( 0, s.length() - 1 ); }

    public static PatternLiteral getPattern( Expression E )
        {
        if (E instanceof PatternLiteral) 
            {
            PatternLiteral L = (PatternLiteral) E;
            if (L.getPatternLanguage().equals( PatternLiteral.rdql )) return L;
            } 
        return null;
        }

    public static Expression endsWith( Expression L, String content, String modifiers )
        {
        if (modifiers.equals( "i" ))
            {
            final Expression R = new Expression.Fixed( content.toLowerCase() );
            return new Dyadic( L, ExpressionFunctionURIs.J_endsWithInsensitive, R )
                {            
                public boolean evalBool( Object l, Object r )
                    { return l.toString().toLowerCase().endsWith( r.toString() ); }
                };
            }
        else
            {
            final Expression R = new Expression.Fixed( content );
            return new Dyadic( L, ExpressionFunctionURIs.J_EndsWith, R )
                {            
                public boolean evalBool( Object l, Object r )
                    { return l.toString().endsWith( r.toString() ); }
                };    
            }
        }

    public static Expression startsWith( Expression L, String content, String modifiers )
        {
        if (modifiers.equals( "i" ))
            {
            final Expression R = new Expression.Fixed( content.toLowerCase() );        
            return new Dyadic( L, ExpressionFunctionURIs.J_startsWithInsensitive, R )
                { 
                public boolean evalBool( Object l, Object r )
                    { return l.toString().toLowerCase().startsWith( r.toString() ); }
                };  
            }
        else
            {            
            final Expression R = new Expression.Fixed( content );        
            return new Dyadic( L, ExpressionFunctionURIs.J_startsWith, R )
                { 
                public boolean evalBool( Object l, Object r )
                    { return l.toString().startsWith( r.toString() ); }
                };  
            }          
        }

    public static Expression contains( Expression L, String content, String modifiers )
        {
        if (modifiers.equals( "i" ))
            {
            final Expression R = new Expression.Fixed( content.toLowerCase() );
            return new Dyadic( L, ExpressionFunctionURIs.J_containsInsensitive, R )
                { 
                public boolean evalBool( Object l, Object r )
                    { return l.toString().toLowerCase().indexOf( r.toString() ) > -1; }
                };      
            }
        else
            {
            final Expression R = new Expression.Fixed( content );
            return new Dyadic( L, ExpressionFunctionURIs.J_contains, R )
                { 
                public boolean evalBool( Object l, Object r )
                    { return l.toString().indexOf( r.toString() ) > -1; }
                };     
            }
        }
    
    public static boolean notSpecial( String pattern )
        { return pattern.matches( "[A-Za-z0-9-_:/ ]*" ); }

    public static boolean isContains( PatternLiteral pattern )
        { return notSpecial( pattern.getPatternString() ) && iOnly( pattern.getPatternModifiers() ); }
    
    protected static boolean iOnly( String modifiers )
        { return modifiers.equals( "" ) || modifiers.equals( "i" ); }
    
    public static boolean isStartsWith( PatternLiteral pattern )
        {
        String s = pattern.getPatternString();
        return s.startsWith( "^" ) && notSpecial( s.substring( 1 ) );
        }

    public static boolean isEndsWith( PatternLiteral pattern )
        {
        String s = pattern.getPatternString();
        return s.endsWith( "$" ) && notSpecial( s.substring( 0, s.length() - 1 ) );
        }

    }


/*
(c) Copyright 2004, Hewlett-Packard Development Company, LP
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

3. The name of the author may not be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/