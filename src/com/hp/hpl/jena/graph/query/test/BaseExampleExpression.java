/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: BaseExampleExpression.java,v 1.8 2003-10-15 10:56:05 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query.test;

import com.hp.hpl.jena.graph.query.Expression;
import com.hp.hpl.jena.graph.query.*;

/**
	BaseExampleExpression - simple expressions
	@author kers
 */
public abstract class BaseExampleExpression 
    extends Expression.Base implements Expression
    {    
    public static abstract class BaseExampleValuator implements Valuator
        {
        public abstract boolean evalBool( IndexValues iv );      
        
        public static Valuator and( final Valuator L, final Valuator R )
            {
            return new Valuator()
                {
                public boolean evalBool( IndexValues iv ) 
                    { return L.evalBool( iv ) && R.evalBool( iv ); }
                };    
            }
            
        public Object eval( IndexValues iv )
            { return evalBool( iv ) ? Boolean.TRUE : Boolean.FALSE; }
        }    
        
    public Expression and( Expression e ) { return and( this, e ); }
        
    public Object eval( VariableValues vv )
        { return evalBool( vv ) ? Boolean.TRUE : Boolean.FALSE; }
 
    public static BaseExampleExpression and( final Expression L, final Expression R )
        {
        return new BaseExampleExpression()
            {                
            public Valuator prepare( VariableIndexes vi )
                {
                return BaseExampleValuator.and( L.prepare( vi ), R.prepare( vi ) );    
                }
                
            public boolean evalBool( VariableValues vv )
                { return L.evalBool( vv ) && R.evalBool( vv ); }
                 
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