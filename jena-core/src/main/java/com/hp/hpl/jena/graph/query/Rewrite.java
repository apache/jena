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

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.Node_Literal;

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
        
        @Override
        public boolean evalBool( Object l, Object r )
            { return evalBool( nodeAsString( l ), r.toString() ); }
        
        protected String nodeAsString( Object x )
            {
            return x instanceof Node_Literal
                ? ((Node_Literal) x).getLiteralLexicalForm()
                : x.toString()
                ;
            }
        
        protected abstract boolean evalBool( String l, String r );
        }
    
    public static Expression endsWith( Expression L, final String content, String modifiers )
        {
        if (modifiers.equals( "i" ))
            {
            return new DyadicLiteral( L, ExpressionFunctionURIs.J_endsWithInsensitive, content )
                {          
                protected final String lowerContent = content.toLowerCase();
                
                @Override
                public boolean evalBool( String l, String r )
                    { return l.toLowerCase().endsWith( lowerContent ); }
                };
            }
        else
            {
            return new DyadicLiteral( L, ExpressionFunctionURIs.J_EndsWith, content )
                {            
                @Override
                public boolean evalBool( String l, String r )
                    { return l.endsWith( r ); }
                };    
            }
        }

    public static Expression startsWith( Expression L, final String content, String modifiers )
        {
        if (modifiers.equals( "i" ))
            {      
            return new DyadicLiteral( L, ExpressionFunctionURIs.J_startsWithInsensitive, content )
                { 
                protected final String lowerContent = content.toLowerCase();
                
                @Override
                public boolean evalBool( String l, String r )
                    { return l.toLowerCase().startsWith( lowerContent ); }
                };  
            }
        else
            {                
            return new DyadicLiteral( L, ExpressionFunctionURIs.J_startsWith, content )
                { 
                @Override
                public boolean evalBool( String l, String r )
                    { return l.startsWith( r ); }
                };  
            }          
        }

    public static Expression contains( Expression L, final String content, String modifiers )
        {
        if (modifiers.equals( "i" ))
            {
            return new DyadicLiteral( L, ExpressionFunctionURIs.J_containsInsensitive, content )
                { 
                protected final String lowerContent = content.toLowerCase();
                
                @Override
                public boolean evalBool( String l, String r )
                    { return l.toLowerCase().indexOf( lowerContent ) > -1; }
                };      
            }
        else
            {
            return new DyadicLiteral( L, ExpressionFunctionURIs.J_contains, content )
                { 
                @Override
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
