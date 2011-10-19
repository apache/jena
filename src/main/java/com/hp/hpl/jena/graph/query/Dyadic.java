/*
  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: Dyadic.java,v 1.1 2009-06-29 08:55:45 castagna Exp $
*/
package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.query.Expression.Application;
import com.hp.hpl.jena.shared.JenaException;

/**
    A base class for dyadic expressions with a built-in Valuator; subclasses must
    define an evalObject or evalBool method which will be supplied with the
    evaluated operands.
    
    @author kers
*/
public abstract class Dyadic extends Application
    {
    protected Expression L;
    protected Expression R;
    protected String F;
    
    public Dyadic( Expression L, String F, Expression R )
        {
        this.L = L;
        this.F = F;
        this.R = R;
        }
    
    @Override
    public int argCount()
        { return 2; }
    
    @Override
    public Expression getArg( int i )
        { return i == 0 ? L : R; }
    
    @Override
    public String getFun()
        { return F; }
    
    /**
     	Answer the Object result of evaluating this dyadic expression with 
     	the given arguments <code>l</code> and <code>r</code>.
     	Either this method or <code>evalBool</code> <i>must</i> be
     	over-ridden in concrete sub-classes.
    */
    public Object evalObject( Object l, Object r )
        { return evalBool( l, r ) ? Boolean.TRUE : Boolean.FALSE; }
    
    /**
 		Answer the boolean result of evaluating this dyadic expression with 
 		the given arguments <code>l</code> and <code>r</code>.
 		Either this method or <code>evalObject</code> <i>must</i> be
 		over-ridden in concrete sub-classes.
 	*/
    public boolean evalBool( Object l, Object r )
        { Object x = evalObject( l, r );
        if (x instanceof Boolean) return ((Boolean) x).booleanValue();
        throw new JenaException( "not Boolean: " + x );
        }
    
    @Override
    public Valuator prepare( VariableIndexes vi )
        {
        final Valuator l = L.prepare( vi ), r = R.prepare( vi );
        return new Valuator()
            {
            @Override
            public boolean evalBool( IndexValues iv)
                {
                return ((Boolean) evalObject( iv )).booleanValue();
                }
    
            @Override
            public Object evalObject( IndexValues iv )
                {
                return Dyadic.this.evalObject( l.evalObject( iv ), r.evalObject( iv ) );
                }
                
            };
        }
    
    @Override
    public String toString()
        { return L.toString() + " " + F + " " + R.toString(); }

    public static Expression and( Expression L, Expression R )
    {
    return new Dyadic( L, ExpressionFunctionURIs.AND, R )
    	{
        @Override
        public boolean evalBool( Object x, Object y )
            { return ((Boolean) x).booleanValue() && ((Boolean) y).booleanValue(); }
    	};
    }
    }

/*
(c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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