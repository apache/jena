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

import java.util.*;

/**
	Expression - the interface for expressions that is expected by Query for 
    constraints. An Expression can be evaluated (given a name->value mapping);
    it can be prepared into a Valuator (given a name->index mapping); and it can
    be analysed into its components.
<p>
    An Expression can be a variable, an application, or a literal value. If an access
    method (eg getName) is applied to an Expression for which it is not appropriate
    (eg an application), <em>the result is unspecified</em>; an implementation is
    free to throw an exception, deliver a null result, deliver a misleading value,
    whatever is convenient.
<p>
    The nested class <code>Util</code> provides some expression utility
    methods, including a generic version of <code>prepare</code>. The nested 
    abstract class <code>Base</code> and its sub-classes <code>Literal</code>, 
    <code>Variable</code>, and <code>Application</code> provide a framework 
    for developers to implement Expressions over.

	@author kers
*/
public interface Expression 
    { 
    /**
        Answer a Valuator which, when run with a set of index-to-value bindings, 
        evaluates this expression in the light of the given variable-to-index bindings
        [ie as though the variables were bound to the corresponding values]
    */
    public Valuator prepare( VariableIndexes vi );
    
    /**
    	Answer true iff this Expression represents a variable.
    */
    public boolean isVariable();
    
    /**
        If this Expression is a variable, answer a [non-null] String which is its name.
        Otherwise the behaviour is unspecified.
    */
    public String getName();
    
    /**
        Answer true iff this Expression represents a literal [Java object] value.
    */
    public boolean isConstant();
    
    /**
        If this Expression is a literal, answer the value of that literal. Otherwise the
        behaviour is unspecified.
    */
    public Object getValue();
    
    /**
        Answer true iff this Expression represents the application of some function
        [or operator] to some arguments [or operands].
    */
    public boolean isApply();
    
    /**
         If this Expression is an application, return the string identifying the function,
         which should be a URI. Otherwise the behaviour is unspecified.
     */
    public String getFun();
    
    /**
    	If this Expression is an application, answer the number of arguments that
        it has. Otherwise the behaviour is unspecified.    
    */
    public int argCount();
    
    /**
        If this Expression is an application, and 0 &lt;= i &lt; argCount(), answer the
        <code>i</code>th argument. Otherwise the behaviour is unspecified. 
    */
    public Expression getArg( int i );

    /**
    	An Expression which always evaluates to <code>true</code>.
    */
    public static Expression TRUE = new BoolConstant( true );
    
    /**
        An Expression which always evaluates to <code>false</code>.
    */
    public static Expression FALSE = new BoolConstant( false );
    
    /**
        An abstract base class for Expressions; over-ride as appropriate. The
        sub-classes may be more useful. 
    */
    public static abstract class Base implements Expression
        {        
        @Override
        public boolean isVariable() { return false; }
        @Override
        public boolean isApply() { return false; }
        @Override
        public boolean isConstant() { return false; }
        @Override
        public String getName() { return null; }
        @Override
        public Object getValue() { return null; }
        @Override
        public int argCount() { return 0; }
        @Override
        public String getFun() { return null; }
        @Override
        public Expression getArg( int i ) { return null; }
        
        @Override
        public boolean equals( Object other )
            { return other instanceof Expression && Expression.Util.equals( this, (Expression) other ); }
        }
    
    /**
        An abstract base class for literal nodes; subclasses implement getValue().
    */
    public static abstract class Constant extends Base
        {
        @Override
        public boolean isConstant() { return true; }
        @Override
        public abstract Object getValue();
        }
    
    /**
     	A concrete class for representing fixed constants; each instance
     	can hold a separate value and its valuator returns that value.
    */
    public static class Fixed extends Constant
    	{
        protected Object value;
        
        public Fixed( Object value )
            { this.value = value; }
    	
        @Override
        public Object getValue()
            { return value; }
        
        @Override
        public Valuator prepare( VariableIndexes vi ) 
            { return new FixedValuator( value ); }
        
        @Override
        public String toString()
            { return value.toString(); }
    	}
    
    /**
        An abstract base class for variable nodes; subclasses implement getName().
    */
    public static abstract class Variable extends Base
        {
        @Override
        public boolean isVariable() { return true; }
        @Override
        public abstract String getName();
        }
    
    /**
        An abstract base class for apply nodes; subclasses implement getFun(),
        argCount(), and getArg().
    */
    public static abstract class Application extends Base
        {
        @Override
        public boolean isApply() { return true; }
        @Override
        public abstract int argCount();
        @Override
        public abstract String getFun();
        @Override
        public abstract Expression getArg( int i );
        }
    
    /**
        Utility methods for Expressions, captured in a class because they can't be
        written directly in the interface.
    */
    public static class Util
        {
        /**
            Answer a set containing exactly the names of variables within 
            <code>e</code>.
        */
        public static Set<String> variablesOf( Expression e )
            { return addVariablesOf( new HashSet<String>(), e ); }
        
        /**
            Add all the variables of <code>e</code> to <code>s</code>, and answer
            <code>s</code>.
        */
        public static Set<String> addVariablesOf( Set<String> s, Expression e )
            {
            if (e.isVariable()) 
                s.add( e.getName() );
            else if (e.isApply())
                for (int i = 0; i < e.argCount(); i += 1)
                    addVariablesOf( s, e.getArg( i ) );
            return s;
            }

		public static boolean containsAllVariablesOf( Set<String> variables, Expression e ) 
    		{
    		if (e.isConstant()) 
    		    return true;
    		if (e.isVariable()) 
    		    return variables.contains( e.getName() );
    		if (e.isApply())
    		    {
    		    for (int i = 0; i < e.argCount(); i += 1)
    		        if (containsAllVariablesOf( variables, e.getArg(i) ) == false) return false;
    		    return true;
    		    }
    		return false;
    		}      
        
		public static boolean equals( Expression L, Expression R )
            {
            return
                L.isConstant() ? R.isConstant() && L.getValue().equals( R.getValue() )
                : L.isVariable() ? R.isVariable() && R.getName().equals( R.getName() )
                : L.isApply() ? R.isApply() && sameApply( L, R )
                : false
                ;
            }
        
        public static boolean sameApply( Expression L, Expression R )
            {
            return 
                L.argCount() == R.argCount() && L.getFun().equals( R.getFun() )
                && sameArgs( L, R )
                ;
            }
        
        public static boolean sameArgs( Expression L, Expression R )
            {
            for (int i = 0; i < L.argCount(); i += 1)
                if (!equals( L.getArg( i ), R.getArg( i ) )) return false;
            return true;
            }
        }
     
    /**
    	Valof provides an implementation of VariableValues which composes the
        "compile-time" VariableIndexes map with the "run-time" IndexValues map
        to produce a VariableValues map. A Valof has mutable state; the setDomain
        operation changes the IndexValues mapping of the Valof.
    
    	@author kers
     */
    static class Valof implements VariableValues
        {
        private VariableIndexes map;
        private IndexValues dom;
        
        public Valof( VariableIndexes map ) { this.map = map; }
        
        @Override
        public final Object get( String name )
             { return dom.get( map.indexOf( name ) );  }
                 
        public final Valof setDomain( IndexValues d ) { dom = d; return this; }  
        }
               
    /**
    	Base class used to implement <code>TRUE</code> and <code>FALSE</code>.
     */
    public static class BoolConstant extends Base implements Expression, Valuator
        {
        private boolean value;
        public BoolConstant( boolean value ) { this.value = value; }
        @Override
        public boolean isConstant() { return true; }
        // TODO when moving to Jave 1.4 can use Boolean.valueOf( value )
        @Override
        public Object getValue() { return value ? Boolean.TRUE : Boolean.FALSE; }
        @Override
        public Valuator prepare( VariableIndexes vi ) { return this; }   
        public boolean evalBool( VariableValues vv ) { return value; }
        @Override
        public boolean evalBool( IndexValues vv ) { return value; }
        @Override
        public Object evalObject( IndexValues iv ) { return getValue(); }
        }    
    }
