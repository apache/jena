/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: Rewrite.java,v 1.2 2004-07-21 13:45:12 chris-dollin Exp $
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
        String pattern = Rewrite.getPattern( R );
        if (pattern == null)
            return e;
        else if (isStartsWith( pattern ))
            return startsWith( L, pattern.substring( 1 ));
        else if (isContains( pattern ))
            return contains( L, pattern );
        else if (isEndsWith( pattern ))
            return endsWith( L, pattern.substring( 0, pattern.length() - 1 ) );
        return e;
        }

    public static String getPattern( Expression E )
        {
        if (E.isConstant())
            {
            Object R = E.getValue();
            if (R instanceof String)
                {
                String s = (String) R;
                if (s.startsWith( "/") && s.endsWith( "/" ))
                    return s.substring( 1, s.length() - 1 );
                }
            }
        return null;
        }

    public static Expression endsWith( final Expression L, final String S )
        {
        final Expression R = new Expression.Fixed( S );
        return new Dyadic( L, ExpressionFunctionURIs.prefix + "J_endsWith", R )
            {            
            public boolean evalBool( Object l, Object r )
                { return l.toString().endsWith( r.toString() ); }
            };            
        }

    public static Expression startsWith( final Expression L, final String S )
        {
        final Expression R = new Expression.Fixed( S );
        return new Dyadic( L, ExpressionFunctionURIs.prefix + "J_startsWith", R )
            { 
            public boolean evalBool( Object l, Object r )
                { return l.toString().startsWith( r.toString() ); }
            };            
        }

    public static Expression contains( final Expression L, final String S )
        {
        final Expression R = new Expression.Fixed( S );
        return new Dyadic( L, ExpressionFunctionURIs.prefix + "J_contains", R )
            { 
            public boolean evalBool( Object l, Object r )
                { return l.toString().indexOf( r.toString() ) > -1; }
            };            
        }
    
    public static boolean notSpecial( String pattern )
        {
        return pattern.matches( "[A-Za-z0-9-_ ]*" );
        }

    public static boolean isContains( String pattern )
        { return notSpecial( pattern ); }
    
    public static boolean isStartsWith( String pattern )
        {
        return pattern.startsWith( "^" ) && notSpecial( pattern.substring( 1 ) );
        }

    public static boolean isEndsWith( String pattern )
        {
        return pattern.endsWith( "$" ) && notSpecial( pattern.substring( 0, pattern.length() - 1 ) );
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