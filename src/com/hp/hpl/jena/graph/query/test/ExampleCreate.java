/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: ExampleCreate.java,v 1.4 2003-10-10 10:36:20 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;

/**
    Test code for creating some expressions.
	ExampleCreate

	@author kers
 */
public class ExampleCreate
    {
    public static abstract class Dyadic extends BaseExampleExpression
        {
        protected Expression L;
        protected Expression R;
        
        public Dyadic( Node L, Node R )
            { this.L = asExpression( L ); this.R = asExpression( R ); }

        public Object eval( IndexValues iv )
            { return evalBool( iv ) ? Boolean.TRUE : Boolean.FALSE; }
            
        public Object eval( VariableValues iv )
            { return evalBool( iv ) ? Boolean.TRUE : Boolean.FALSE; }
                                
        public Expression prepare( VariableIndexes vi )
            {
            L = L.prepare( vi );
            R = R.prepare( vi );
            return this;    
            }    
            
        public Expression asExpression( final Node x )
            {
            return new Expression()
                {
                int xi = -1;
                
                public Object eval( IndexValues iv )
                    { return xi < 0 ? x : iv.get( xi ); }
                    
                public Object eval( VariableValues iv )
                    { return iv.get( x.getName() ); }
                    
                public boolean evalBool( VariableValues vv )
                    {
                    if (x.isVariable()) return ((Boolean) vv.get( x.getName() )).booleanValue();
                    else return false;    
                    }
                    
                public Expression prepare( VariableIndexes vi )
                    { if (x.isVariable()) xi = vi.indexOf( x.getName() ); 
                    return this; }    
                    
                public boolean evalBool( IndexValues iv )
                    { return ((Boolean) iv.get( xi )).booleanValue(); }
                };    
            }
        }
        
    public static BaseExampleExpression NE( final Node x, final Node y )
        {
        return new Dyadic( x, y ) 
            {
            public boolean evalBool( VariableValues vv )
                { return !L.eval( vv ).equals( R.eval( vv ) ); }
                
            public boolean evalBool( IndexValues iv )
                { 
                    return !L.eval( iv ).equals( R.eval( iv ) ); }
            };    
        }    
    
    public static BaseExampleExpression EQ( Node x, Node y )
        {
        return new Dyadic( x, y ) 
            {
            public boolean evalBool( VariableValues vv )
                { return L.eval( vv ).equals( R.eval( vv ) ); }
                
            public boolean evalBool( IndexValues iv )
                { return L.eval( iv ).equals( R.eval( iv ) ); }
            };    
        }         
        
    public static BaseExampleExpression MATCHES( Node x, Node y )
        {
        return new Dyadic( x, y ) 
            {
            private String asString( Object n )
                {
                if (n instanceof Node_Literal) return ((Node) n).getLiteral().getLexicalForm();
                else return n.toString();    
                }
        
            public boolean matches( Object L, Object R )
                { String x = asString( L ), y = asString( R );
                return x.indexOf( y ) > -1; }       
                         
            public boolean evalBool( VariableValues vv )
                { return matches( L.eval( vv ), R.eval( vv ) ); }
                
            public boolean evalBool( IndexValues vv )
                { return matches( L.eval( vv ), R.eval( vv ) ); }
            };    
        }
    }
/*
    (c) Copyright 2003, Hewlett-Packard Development Company, LP
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