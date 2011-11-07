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
