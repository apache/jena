/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: Rewrite.java,v 1.8 2004-08-13 19:23:00 chris-dollin Exp $
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

    /*
         This following code for the evaluators is horrid - there's too much uncaptured
         variation. Perhaps it will all go away when I understand how to use the
         regex package (note, not the Java 1.4 package, because we still support
         Java 1.3; the ORO package that RDQL uses).
         
         TODO clean this up. 
         
     	@author hedgehog
    */
    public static abstract class DyadicLiteral extends Dyadic
        {
        public DyadicLiteral( Expression L, String F, String R )
            { super( L, F, new Expression.Fixed( R ) ); }
        
        public boolean evalBool( Object l, Object r )
            { return evalBool( l.toString(), r.toString() ); }
        
        protected abstract boolean evalBool( String l, String r );
        }
    
    public static abstract class DyadicLower extends DyadicLiteral
        {
        public DyadicLower( Expression L, String F, String R )
            { super( L, F, R.toLowerCase() ); }
        }
    
    public static Expression endsWith( Expression L, String content, String modifiers )
        {
        if (modifiers.equals( "i" ))
            {
            return new DyadicLower( L, ExpressionFunctionURIs.J_endsWithInsensitive, content )
                {            
                public boolean evalBool( String l, String r )
                    { return l.toLowerCase().endsWith( r ); }
                };
            }
        else
            {
            return new DyadicLiteral( L, ExpressionFunctionURIs.J_EndsWith, content )
                {            
                public boolean evalBool( String l, String r )
                    { return l.endsWith( r ); }
                };    
            }
        }

    public static Expression startsWith( Expression L, String content, String modifiers )
        {
        if (modifiers.equals( "i" ))
            {      
            return new DyadicLower( L, ExpressionFunctionURIs.J_startsWithInsensitive, content )
                { 
                public boolean evalBool( String l, String r )
                    { return l.toLowerCase().startsWith( r ); }
                };  
            }
        else
            {                
            return new DyadicLiteral( L, ExpressionFunctionURIs.J_startsWith, content )
                { 
                public boolean evalBool( String l, String r )
                    { return l.startsWith( r ); }
                };  
            }          
        }

    public static Expression contains( Expression L, String content, String modifiers )
        {
        if (modifiers.equals( "i" ))
            {
            return new DyadicLower( L, ExpressionFunctionURIs.J_containsInsensitive, content )
                { 
                public boolean evalBool( String l, String r )
                    { return l.toLowerCase().indexOf( r ) > -1; }
                };      
            }
        else
            {
            return new DyadicLiteral( L, ExpressionFunctionURIs.J_contains, content )
                { 
                public boolean evalBool( String l, String r )
                    { return l.indexOf( r ) > -1; }
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