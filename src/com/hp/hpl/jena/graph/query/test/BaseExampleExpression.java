/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: BaseExampleExpression.java,v 1.3 2003-10-10 09:07:10 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query.test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.query.Expression;
import com.hp.hpl.jena.graph.query.*;

/**
	BaseExampleExpression - simple expressions
	@author kers
 */
public abstract class BaseExampleExpression implements Expression
    {
    public Expression and( Expression e ) { return and( this, e ); }
    
    public boolean evalBool( VariableValues vv )
        { throw new RuntimeException( "bleagh" ); }
        
    protected Object eval( Node x, VariableValues vv )
        {
        if (x.isVariable()) return vv.get( x.getName() );
        else return x;    
        }
        
    protected Object eval( Node x, IndexValues iv )
        { throw new RuntimeException( "rargh" ); }
                                                                
    public static Expression and( final Expression L, final Expression R )
        {
        return new BaseExampleExpression()
            {                
            public Expression prepare( VariableIndexes vi )
                {
                return and( L.prepare( vi ), R.prepare( vi ) );    
                }
                
            public boolean evalBool( IndexValues iv )
                { return L.evalBool( iv ) && R.evalBool( iv ); }
                
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