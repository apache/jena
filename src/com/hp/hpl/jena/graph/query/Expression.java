/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: Expression.java,v 1.15 2003-12-02 16:17:04 chris-dollin Exp $
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
    (eg an application), <em>the result is unspecified</code>; an implementation is
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
    	Answer true iff the Expression evaluates to true given the variable bindings.
        
    	@param vv the mapping from variable[ name]s to their values
    */
    public boolean evalBool( VariableValues vv );
    
    /**
        Answer a Valuator which does the same evaluation as this Expression when
        supplied with a mapping from variable indexes to their values. The 
        composition of the variable->index map <code>vi</code> and the 
        index->value map given to the valuator is equivalent to the VariableValues
        map that would be supplied to <code>evalBool</code>.
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
    public boolean isLiteral();
    
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
        sub-classes may be more useful. Base provides an implementation of
        <code>prepare</code> which produces a slow Valuator that relies
        on the Base's <code>evalBool</code>.
    */
    public static abstract class Base implements Expression
        {        
        public boolean isVariable() { return false; }
        public boolean isApply() { return false; }
        public boolean isLiteral() { return false; }
        public String getName() { return null; }
        public Object getValue() { return null; }
        public int argCount() { return 0; }
        public String getFun() { return null; }
        public Expression getArg( int i ) { return null; }
        public Valuator prepare( VariableIndexes vi ) { return Util.prepare( this, vi ); }
        }
    
    /**
        An abstract base class for literal nodes; subclasses implement getValue().
    */
    public static abstract class Literal extends Base
        {
        public boolean isLiteral() { return true; }
        public abstract Object getValue();
        }
    
    /**
        An abstract base class for variable nodes; subclasses implement getName().
    */
    public static abstract class Variable extends Base
        {
        public boolean isVariable() { return true; }
        public abstract String getName();
        }
    
    /**
        An abstract base class for apply nodes; subclasses implement getFun(),
        argCount(), and getArg().
    */
    public static abstract class Application extends Base
        {
        public boolean isApply() { return true; }
        public abstract int argCount();
        public abstract String getFun();
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
        public static Set variablesOf( Expression e )
            { return addVariablesOf( new HashSet(), e ); }
        
        /**
            Add all the variables of <code>e</code> to <code>s</code>, and answer
            <code>s</code>.
        */
        public static Set addVariablesOf( Set s, Expression e )
            {
            if (e.isVariable()) 
                s.add( e.getName() );
            else if (e.isApply())
                for (int i = 0; i < e.argCount(); i += 1)
                    addVariablesOf( s, e.getArg( i ) );
            return s;
            }           
        
        /**
            Answer a Valuator v such that <code>v.evalBool(IndexValues iv)</code>
            will return
        <br>
            <code>
            e.evalBool( vv ) where vv.get(name) = iv.get(vi.indexOf(name))
            </code>
         */
        public static Valuator prepare( final Expression e, VariableIndexes vi )
            {
            final Valof valof = new Valof( vi );
            return new Valuator()
                {
                public boolean evalBool( IndexValues iv )
                    { return e.evalBool( valof.setDomain( iv ) ); }
                };
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
        public boolean isLiteral() { return true; }
        // TODO when moving to Jave 1.4 can use Boolean.valueOf( value )
        public Object getValue() { return value ? Boolean.TRUE : Boolean.FALSE; }
        public Valuator prepare( VariableIndexes vi ) { return this; }   
        public boolean evalBool( VariableValues vv ) { return value; }
        public boolean evalBool( IndexValues vv ) { return value; }
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
