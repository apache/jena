/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: ExampleCreate.java,v 1.12 2004-07-22 10:11:47 chris-dollin Exp $
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
    public static Expression NE( final Node x, final Node y )
        {
        return new Dyadic( asExpression( x ), "http://jena.hpl.hp.com/constraints/NE", asExpression( y ) )
        	{
            public boolean evalBool( Object x, Object y )
                { return !x.equals( y ); }
        	};    
        }    
    
    public static Expression EQ( Node x, Node y )
        {
        return new Dyadic( asExpression( x ), "http://jena.hpl.hp.com/constraints/EQ", asExpression( y ) ) 
            {            
            public boolean evalBool( Object x, Object y )
                { return x.equals( y ); }
            };    
        }         
        
    public static Expression MATCHES( Node x, Node y )
        {
        return new Dyadic( asExpression( x ), "http://jena.hpl.hp.com/constraints/MATCHES", asExpression( y ) ) 
            {
            public boolean evalBool( Object L, Object R )
                { return L.toString().indexOf( R.toString() ) > -1; }       
            };    
        }

    public static Expression asExpression( final Node x )
	    {
	    if( x.isVariable()) return new Expression.Variable()
		    {
            public String getName()
                { return x.getName(); }

            public Valuator prepare( VariableIndexes vi )
                { return new SlotValuator( vi.indexOf( x.getName() ) ); }
		    };
		return new Expression.Fixed( x );
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