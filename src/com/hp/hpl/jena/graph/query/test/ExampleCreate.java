/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: ExampleCreate.java,v 1.6 2003-10-14 15:45:12 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;

import java.util.*;

/**
    Test code for creating some expressions.
	ExampleCreate

	@author kers
 */
public class ExampleCreate
    {
    interface ObjectValuator extends Valuator
        {
        public Object eval( IndexValues iv );    
        }
        
    public static abstract class DyadicValuator 
        extends BaseExampleExpression.BaseExampleValuator
        implements ObjectValuator
        {
        ObjectValuator L;
        ObjectValuator R;
        DyadicValuator( ObjectValuator L, ObjectValuator R ) { this.L = L; this.R = R; }    
        public Object eval( IndexValues iv )
            { return Boolean.valueOf( evalBool( iv ) ); }
        }
    
    public static class FixedValuator implements ObjectValuator
        {
        private Object value;
        
        FixedValuator( Object value )
            { this.value = value; }
            
        public boolean evalBool( IndexValues iv )
            { return ((Boolean) eval( iv )).booleanValue(); }
                
        public Object eval( IndexValues iv )
            { return value; }
        }
        
    public static class SlotValuator implements ObjectValuator
        {
        private int index;
        
        SlotValuator( int index )
            { this.index = index; }
            
        public boolean evalBool( IndexValues iv )
            { return ((Boolean) eval( iv )).booleanValue(); }
                
        public Object eval( IndexValues iv )
            { return iv.get( index ); }
        }
        
    public static abstract class Dyadic extends BaseExampleExpression
        {
        protected BaseExampleExpression L;
        protected BaseExampleExpression R;
        protected List args;
        
        public Dyadic( Node L, Node R )
            { this.L = asExpression( L ); this.R = asExpression( R ); }
        
        public boolean isApply() 
            { return true; }
        
        public List getArgs()
            {
            if (args == null) 
                { args = new ArrayList(); args.add( L ); args.add( R ); }
            return args;
            }

        public BaseExampleExpression asExpression( final Node x )
            {
            return new BaseExampleExpression()
                {
                public boolean evalBool( VariableValues vv )
                    {
                    if (x.isVariable()) return ((Boolean) vv.get( x.getName() )).booleanValue();
                    else return false;    
                    }
                    
                public boolean isVariable()
                    { return x.isVariable(); }
                
                public String getName()
                    { return x.getName(); }
                
                public Object eval( VariableValues vv )
                    {
                    if (x.isVariable()) return vv.get( x.getName() );
                    else return x;    
                    }
                    
                public Valuator prepare( VariableIndexes vi )
                    { if (x.isVariable()) return new SlotValuator( vi.indexOf( x.getName() ) );
                    else return new FixedValuator( x ); }    
                    
                };    
            }
        }
        
    public static BaseExampleExpression NE( final Node x, final Node y )
        {
        return new Dyadic( x, y ) 
            {
            public String toString()
                { return "{" + x + " NE " + y + "}"; }
            
            public boolean evalBool( VariableValues vv )
                { return !L.eval( vv ).equals( R.eval( vv ) ); }
                
            public Valuator prepare( VariableIndexes vi )
                {
                return new DyadicValuator( (ObjectValuator) L.prepare( vi ), (ObjectValuator) R.prepare( vi ) )
                    {                    
                    public boolean evalBool( IndexValues iv )
                        { return !L.eval( iv ).equals( R.eval( iv ) ); }
                    };    
                }
                
            };    
        }    
    
    public static BaseExampleExpression EQ( Node x, Node y )
        {
        return new Dyadic( x, y ) 
            {
            public boolean evalBool( VariableValues vv )
                { return L.eval( vv ).equals( R.eval( vv ) ); }
                
            public Valuator prepare( VariableIndexes vi )
                {
                return new DyadicValuator( (ObjectValuator) L.prepare( vi ), (ObjectValuator) R.prepare( vi ) )
                    {                    
                    public boolean evalBool( IndexValues iv )
                        { return L.eval( iv ).equals( R.eval( iv ) ); }
                    };    
                }
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
                
            public Valuator prepare( VariableIndexes vi )
                {
                return new DyadicValuator( (ObjectValuator) L.prepare( vi ), (ObjectValuator) R.prepare( vi ) )
                    {                    
                    public String toString() { return super.toString() + " :: MATCHES()"; }
                    public boolean evalBool( IndexValues iv )
                        { return matches( L.eval( iv ), R.eval( iv ) ); }
                    };    
                }
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