/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: Expression.java,v 1.1 2003-09-26 11:53:51 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.Node;

/**
	Expression - the interface for expressions that is expected by Query for constraints.

	@author kers
*/
public interface Expression 
    { 
    public boolean evalBool( Mapping map, Domain d );
    
    public Expression and( Expression e );
    
    public abstract class Base implements Expression
        {
        public Expression and( Expression e ) { return and( this, e ); }
        public static Expression and( final Expression L, final Expression R )
            {
            return new Base()
                {
                public boolean evalBool( Mapping map, Domain d )
                    { return L.evalBool( map, d ) && R.evalBool( map, d ); }    
                };     
            }
        }
        
    public static Expression TRUE = new Base() 
        { public boolean evalBool( Mapping map, Domain d ) { return true; }};
    
    public static Expression FALSE = new Base() 
        { public boolean evalBool( Mapping map, Domain d ) { return false; }};
       
    public class Create
        {
        public static Expression NE( final Node x, final Node y )
            {
            return new Base() 
                {
                private Object eval( Node x, Mapping map, Domain d )
                    {
                    if (x.isVariable()) return d.get( map.indexOf( x ) );
                    else return x;    
                    }
                    
                public boolean evalBool( Mapping map, Domain d )
                    { return !eval( x, map, d ).equals( eval( y, map, d ) ); }
                };    
            }    
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